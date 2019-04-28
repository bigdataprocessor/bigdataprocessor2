package de.embl.cba.bdp2.ui;

import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.loading.CachedCellImgReader;
import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.saving.SaveCentral;
import de.embl.cba.bdp2.saving.SavingSettings;
import de.embl.cba.bdp2.utils.DimensionOrder;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.bdp2.viewers.ImageViewer;
import ij.gui.GenericDialog;
import net.imglib2.FinalInterval;
import net.imglib2.FinalRealInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealRandomAccessible;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.converter.Converters;
import net.imglib2.converter.RealUnsignedByteConverter;
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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicBoolean;

import net.imglib2.interpolation.InterpolatorFactory;

public class BigDataProcessor2
{

    public FileInfos fileInfos;
    public static ExecutorService executorService;  //General thread pool
    public static ExecutorService trackerThreadPool; // Thread pool for tracking
    public int numThreads;
    public static int MAX_THREAD_LIMIT = Runtime.getRuntime().availableProcessors() * 2;
    public static Map<Integer, AtomicBoolean> saveTracker = new ConcurrentHashMap<>();
    public static Map<Integer, Integer> progressTracker = new ConcurrentHashMap<>();


    public BigDataProcessor2() {
        //TODO: have separate shutdown for the executorService. It will not shutdown when ui exeService is shut. --ashis (DONE but needs testing)
        //Ref: https://stackoverflow.com/questions/23684189/java-how-to-make-an-executorservice-running-inside-another-executorservice-not
        kickOffThreadPack( Runtime.getRuntime().availableProcessors() * 2 );
    }

    public void kickOffThreadPack(int numThreads) {
        this.numThreads = numThreads;
        if (executorService != null) return;
        executorService = Executors.newFixedThreadPool(numThreads);
    }

    public void openFromDirectory(
            String directory,
            String loadingScheme,
            String filterPattern,
            String h5DataSetName,
            boolean autoContrast,
            ImageViewer imageViewer )
    {
        directory = Utils.fixDirectoryFormat( directory );

        this.fileInfos =
				new FileInfos( directory, loadingScheme, filterPattern, h5DataSetName );

        if ( ! ensureCalibrationUI() ) return;

        CachedCellImg cachedCellImg =
				CachedCellImgReader.asCachedCellImg( this.fileInfos );

        imageViewer.show(
                new Image(
                        cachedCellImg,
                        FileInfos.IMAGE_NAME,
                        fileInfos.voxelSpacing,
                        fileInfos.unit )
                , autoContrast );

        imageViewer.addMenus(new BdvMenus());
    }

    private boolean ensureCalibrationUI()
    {
        final GenericDialog genericDialog = new GenericDialog( "Calibration" );
        genericDialog.addStringField( "Unit", fileInfos.unit, 12 );
        genericDialog.addNumericField( "Spacing X", fileInfos.voxelSpacing[ 0 ], 3 );
        genericDialog.addNumericField( "Spacing Y", fileInfos.voxelSpacing[ 1 ], 3 );
        genericDialog.addNumericField( "Spacing Z", fileInfos.voxelSpacing[ 2 ], 3 );
        genericDialog.showDialog();
        if ( genericDialog.wasCanceled() ) return false;
        fileInfos.unit = genericDialog.getNextString();
        fileInfos.voxelSpacing[ 0 ] = genericDialog.getNextNumber();
        fileInfos.voxelSpacing[ 1 ] = genericDialog.getNextNumber();
        fileInfos.voxelSpacing[ 2 ] = genericDialog.getNextNumber();
        return true;
    }


    public static < R extends RealType< R > & NativeType< R >> void saveImage(
            Image< R > image,
    		SavingSettings savingSettings ) {
        int nIOThread = Math.max(1, Math.min(savingSettings.nThreads, MAX_THREAD_LIMIT));
        ExecutorService saveExecutorService =  Executors.newFixedThreadPool(nIOThread);
        savingSettings.rai = image.getRai();
        SaveCentral.goSave(savingSettings, saveExecutorService, savingSettings.saveId);
    }

    public static void stopSave(Integer saveId) {
        AtomicBoolean stop = BigDataProcessor2.saveTracker.get(saveId);
        stop.set(true);
    }

    public static <T extends RealType<T> & NativeType<T>>
    RandomAccessibleInterval shearImage(RandomAccessibleInterval rai, ShearingSettings shearingSettings)
    {
        System.out.println("Shear Factor X " + shearingSettings.shearingFactorX);
        System.out.println("Shear Factor Y " + shearingSettings.shearingFactorY);

        List<RandomAccessibleInterval<T>> timeTracks = new ArrayList<>();
        int nTimeFrames = (int) rai.dimension( DimensionOrder.T );
        int nChannels = (int) rai.dimension( DimensionOrder.C );
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


        final IntervalView intervalView3D = Views.hyperSlice( Views.hyperSlice( rai5D, DimensionOrder.T, 0 ), DimensionOrder.C, 0 );
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
            RandomAccessibleInterval tStep = Views.hyperSlice(rai, DimensionOrder.T, t);
            for (int channel = 0; channel < nChannels; ++channel) {
                RandomAccessibleInterval cStep = Views.hyperSlice(tStep, DimensionOrder.C, channel);
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


    public static <T extends RealType<T>> RandomAccessibleInterval
    unsignedByteTypeConverter(
            RandomAccessibleInterval rai,
            DisplaySettings displaySettings)
    {
        RandomAccessibleInterval<UnsignedByteType> newRai;
        if (!(Util.getTypeFromInterval(rai) instanceof UnsignedByteType)){
            newRai = Converters.convert(
                    rai,
                    new RealUnsignedByteConverter<T>(
                            displaySettings.getMinValue(),
                            displaySettings.getMaxValue()),
                    new UnsignedByteType());
        }else{
            newRai = rai;
        }
        return newRai;
    }
}
