package de.embl.cba.bigDataTools2.dataStreamingGUI;

import de.embl.cba.bigDataTools2.CachedCellImageCreator;
import de.embl.cba.bigDataTools2.fileInfoSource.FileInfoConstants;
import de.embl.cba.bigDataTools2.fileInfoSource.FileInfoSource;
import de.embl.cba.bigDataTools2.saving.SaveCentral;
import de.embl.cba.bigDataTools2.saving.SavingSettings;
import de.embl.cba.bigDataTools2.utils.Utils;
import de.embl.cba.bigDataTools2.viewers.ImageViewer;
import net.imglib2.*;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.converter.Converters;
import net.imglib2.converter.RealUnsignedByteConverter;
import net.imglib2.interpolation.randomaccess.ClampingNLinearInterpolatorFactory;
import net.imglib2.interpolation.randomaccess.NearestNeighborInterpolatorFactory;
import net.imglib2.realtransform.AffineRandomAccessible;
import net.imglib2.realtransform.AffineTransform;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.realtransform.RealViews;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RecursiveTask;
import net.imglib2.interpolation.InterpolatorFactory;

public class BigDataConverter {

    public FileInfoSource fileInfoSource;
    public static ExecutorService executorService;  //General thread pool
    public static ExecutorService trackerThreadPool; // Thread pool for tracking
    public int numThreads;
    public static int MAX_THREAD_LIMIT = Runtime.getRuntime().availableProcessors() * 2;
    private ImageViewer imageViewer;// remove it

    public BigDataConverter() {
        //TODO: have separate shutdown for the executorService. It will not shutdown when ui exeService is shut. --ashis (DONE but needs testing)
        //Ref: https://stackoverflow.com/questions/23684189/java-how-to-make-an-executorservice-running-inside-another-executorservice-not
        System.out.println("Datastreaming constructor");
        kickOffThreadPack(Runtime.getRuntime().availableProcessors() * 2);
    }

    public void kickOffThreadPack(int nIOthreads) {
        this.numThreads = nIOthreads;
        if (executorService != null) {
            return;
        }
        executorService = Executors.newFixedThreadPool(nIOthreads);
    }

    public void shutdownThreadPack() {
        Utils.shutdownThreadPack(executorService, 10);
    }

    public void openFromDirectory(
            String directory,
            String namingScheme,
            String filterPattern,
            String h5DataSetName,
            boolean autoContrast,
            ImageViewer imageViewer ) {
        directory = Utils.fixDirectoryFormat(directory);
        this.fileInfoSource = new FileInfoSource(directory, namingScheme, filterPattern, h5DataSetName);
        CachedCellImg cachedCellImg = CachedCellImageCreator.create(this.fileInfoSource, this.executorService);
        this.imageViewer = imageViewer;
        imageViewer.show( cachedCellImg, fileInfoSource.voxelSize,  FileInfoConstants.IMAGE_NAME, autoContrast );
        imageViewer.addMenus(new BdvMenus());
    }

    public static RandomAccessibleInterval crop(RandomAccessibleInterval rai,FinalInterval interval){
        return Views.zeroMin(Views.interval(rai, interval));
    }


    public static void saveImage( SavingSettings savingSettings, ImageViewer imageViewer ) { //TODO: No need to get imageVieweer.Use only SavinfgSetting
        int nIOThread = Math.max(1, Math.min(savingSettings.nThreads, MAX_THREAD_LIMIT));
        ExecutorService saveExecutorService =  Executors.newFixedThreadPool(nIOThread);
        String streamName = imageViewer.getImageName();
        RandomAccessibleInterval rai = imageViewer.getRai();
        if (streamName.equalsIgnoreCase(FileInfoConstants.CROPPED_STREAM_NAME)) {
            rai = Views.zeroMin(rai);
        }
        savingSettings.image = rai;
        SaveCentral.interruptSavingThreads = false;
        SaveCentral.goSave(savingSettings, saveExecutorService);
    }

    public static void stopSave() {
        SaveCentral.interruptSavingThreads = true;
    }

    public static <T extends RealType<T> & NativeType<T>>
    RandomAccessibleInterval shearImage(RandomAccessibleInterval rai, ShearingSettings shearingSettings)
    {
        System.out.println("Shear Factor X " + shearingSettings.shearingFactorX);
        System.out.println("Shear Factor Y " + shearingSettings.shearingFactorY);

        List<RandomAccessibleInterval<T>> timeTracks = new ArrayList<>();
        int nTimeFrames = (int) rai.dimension( FileInfoConstants.T );
        int nChannels = (int) rai.dimension( FileInfoConstants.C );
        System.out.println("Shear Factor X " + shearingSettings.shearingFactorX);
        System.out.println("Shear Factor Y " + shearingSettings.shearingFactorY);
        AffineTransform3D affine = new AffineTransform3D();
        affine.set(shearingSettings.shearingFactorX, 0, 2);
        affine.set(shearingSettings.shearingFactorY, 1, 2);
        List<ApplyShearToRAI> tasks = new ArrayList<>();
       // long startTime = System.currentTimeMillis();
        for (int t = 0; t < nTimeFrames; ++t) {
            ApplyShearToRAI task = new ApplyShearToRAI(rai, t, nChannels, affine, shearingSettings.interpolationFactory);
            task.fork();
            tasks.add(task);
        }
        for (ApplyShearToRAI task : tasks) {
            timeTracks.add((RandomAccessibleInterval) task.join());
        }
        final RandomAccessibleInterval sheared = Views.stack( timeTracks );
//        System.out.println("Time elapsed (ms) " + (System.currentTimeMillis() - startTime));
        return sheared;
    }

    public static <T extends RealType<T> & NativeType<T>>
    RandomAccessibleInterval shearImage5D( RandomAccessibleInterval rai5D, ShearingSettings shearingSettings)
    {
        final AffineTransform affine5D = new AffineTransform( 5 );
        affine5D.set(shearingSettings.shearingFactorX, 0, 2);
        affine5D.set(shearingSettings.shearingFactorY, 1, 2);

        RealRandomAccessible rra = Views.interpolate(
                Views.extendZero( rai5D ),
                new NearestNeighborInterpolatorFactory());

        AffineRandomAccessible af = RealViews.affine( rra, affine5D );

        final FinalInterval interval5D = getInterval5DAfterShearing( rai5D, shearingSettings );

        RandomAccessibleInterval intervalView = Views.interval( af, interval5D );

        return intervalView;
    }

    private static FinalInterval getInterval5DAfterShearing(
            RandomAccessibleInterval rai5D,
            ShearingSettings shearingSettings )
    {
        AffineTransform3D affine3DtoEstimateBoundsAfterTransformation = new AffineTransform3D();
        affine3DtoEstimateBoundsAfterTransformation.set(shearingSettings.shearingFactorX, 0, 2);
        affine3DtoEstimateBoundsAfterTransformation.set(shearingSettings.shearingFactorY, 1, 2);


        final IntervalView intervalView3D = Views.hyperSlice( Views.hyperSlice( rai5D, FileInfoConstants.T, 0 ), FileInfoConstants.C, 0 );
        FinalRealInterval transformedRealInterval = affine3DtoEstimateBoundsAfterTransformation.estimateBounds( intervalView3D );

        final Interval interval = Intervals.largestContainedInterval( transformedRealInterval );
        final long[] min = new long[ 5 ];
        final long[] max = new long[ 5 ];
        rai5D.min( min );
        rai5D.max( max );
        interval.min( min );
        interval.max( max );
        return new FinalInterval( min, max );
    }


    private static class ApplyShearToRAI<T extends RealType<T> & NativeType<T>> extends RecursiveTask<RandomAccessibleInterval> {

        private RandomAccessibleInterval rai;
        private int t;
        private final int nChannels;
        private final AffineTransform3D affine;
        private InterpolatorFactory interpolatorFactory;

        public ApplyShearToRAI(RandomAccessibleInterval rai, int time, int nChannels, AffineTransform3D affine,InterpolatorFactory interpolatorFactory) {
            this.rai = rai;
            this.t = time;
            this.nChannels = nChannels;
            this.affine = affine;
            this.interpolatorFactory = interpolatorFactory;
        }

        @Override
        protected RandomAccessibleInterval<T> compute() {
            List<RandomAccessibleInterval<T>> channelTracks = new ArrayList<>();
            RandomAccessibleInterval tStep = Views.hyperSlice(rai, FileInfoConstants.T, t);
            for (int channel = 0; channel < nChannels; ++channel) {
                RandomAccessibleInterval cStep = Views.hyperSlice(tStep, FileInfoConstants.C, channel);
                RealRandomAccessible real = Views.interpolate(Views.extendZero(cStep),this.interpolatorFactory);
                AffineRandomAccessible af = RealViews.affine(real, affine);
                FinalRealInterval transformedRealInterval = affine.estimateBounds(cStep);
                FinalInterval transformedInterval = Utils.asIntegerInterval(transformedRealInterval);
                RandomAccessibleInterval intervalView = Views.interval(af, transformedInterval);
                channelTracks.add(intervalView);
            }
            return Views.stack(channelTracks);
        }
    }

    public ImageViewer getImageViewer()
    {
        return imageViewer;
    }

    public static <T extends RealType<T>> RandomAccessibleInterval unsignedByteTypeConverter(RandomAccessibleInterval rai,DisplaySettings displaySettings){
        RandomAccessibleInterval<UnsignedByteType> newRai;
        if (!(Util.getTypeFromInterval(rai) instanceof UnsignedByteType)){
            newRai = Converters.convert(rai, new RealUnsignedByteConverter<T>(displaySettings.getMinValue(),displaySettings.getMaxValue()), new UnsignedByteType());
        }else{
            newRai = rai;
        }
        return newRai;
    }
}
