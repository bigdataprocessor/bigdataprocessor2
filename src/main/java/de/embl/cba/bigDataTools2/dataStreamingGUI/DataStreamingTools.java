package de.embl.cba.bigDataTools2.dataStreamingGUI;

import de.embl.cba.bigDataTools2.CachedCellImageCreator;
import de.embl.cba.bigDataTools2.fileInfoSource.FileInfoConstants;
import de.embl.cba.bigDataTools2.fileInfoSource.FileInfoSource;
import de.embl.cba.bigDataTools2.saving.SaveCentral;
import de.embl.cba.bigDataTools2.saving.SavingSettings;
import de.embl.cba.bigDataTools2.utils.Utils;
import de.embl.cba.bigDataTools2.viewers.ImageViewer;
import net.imglib2.FinalInterval;
import net.imglib2.FinalRealInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealRandomAccessible;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.interpolation.randomaccess.ClampingNLinearInterpolatorFactory;
import net.imglib2.realtransform.AffineRandomAccessible;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.realtransform.RealViews;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DataStreamingTools {

    public static FileInfoSource fileInfoSource;
    public static ExecutorService executorService;  //General thread pool
    public static ExecutorService trackerThreadPool; // Thread pool for tracking
    public static ImageViewer selectedImageViewer;
    public static int numThreads;


    public DataStreamingTools() {
        //TODO: Determine Voxel Size to display in the Bdv --ashis
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

    public void openFromDirectory(String directory, String namingScheme, String filterPattern, String h5DataSetName, ImageViewer imageViewer) {
        directory = Utils.fixDirectoryFormat(directory);
        this.fileInfoSource = new FileInfoSource(directory, namingScheme, filterPattern, h5DataSetName);
        CachedCellImg cachedCellImg = CachedCellImageCreator.create(this.fileInfoSource, this.executorService);
        imageViewer.setRai(cachedCellImg);
        imageViewer.setImageName(FileInfoConstants.IMAGE_NAME);
        imageViewer.show();
        imageViewer.addMenus(new BdvMenus());
        Utils.doAutoContrastPerChannel(imageViewer);
    }

    public void saveImage(SavingSettings savingSettings) {
        String streamName = selectedImageViewer.getImageName();
        RandomAccessibleInterval rai = selectedImageViewer.getRai();
        if (streamName.equalsIgnoreCase(FileInfoConstants.CROPPED_STREAM_NAME)) {
            rai = Views.zeroMin(rai);
        }
        savingSettings.image = rai;
        SaveCentral.interruptSavingThreads = false;
        SaveCentral.goSave(savingSettings, executorService);
    }

    public static void stopSave() {
        SaveCentral.interruptSavingThreads = true;
    }


    /*Alternate Logic for shear*/
    public static <T extends RealType<T> & NativeType<T>> void shearImage1(ShearingSettings shearingSettings) {
        System.out.println("Shear Factor X " + shearingSettings.shearingFactorX);
        System.out.println("Shear Factor Y " + shearingSettings.shearingFactorY);
        AffineTransform3D affine = new AffineTransform3D();
        affine.set(shearingSettings.shearingFactorX, 0, 2);
        affine.set(shearingSettings.shearingFactorY, 1, 2);
        selectedImageViewer.repaint(affine);
    }

    public static <T extends RealType<T> & NativeType<T>> void shearImage(ShearingSettings shearingSettings) {
        RandomAccessibleInterval rai = selectedImageViewer.getRai();
        List<RandomAccessibleInterval<T>> timeTracks = new ArrayList<>();
        List<RandomAccessibleInterval<T>> channelTracks = new ArrayList<>();
        int nTimeFrames = (int) rai.dimension(FileInfoConstants.T_AXIS_POSITION);
        int nChannels = (int) rai.dimension(FileInfoConstants.C_AXIS_POSITION);
        System.out.println("Shear Factor X " + shearingSettings.shearingFactorX);
        System.out.println("Shear Factor Y " + shearingSettings.shearingFactorY);
        AffineTransform3D affine = new AffineTransform3D();
        affine.set(shearingSettings.shearingFactorX, 0, 2);
        affine.set(shearingSettings.shearingFactorY, 1, 2);
        long startTime = System.currentTimeMillis();
        for (int t = 0; t < nTimeFrames; ++t) {
            RandomAccessibleInterval tStep = Views.hyperSlice(rai, FileInfoConstants.T_AXIS_POSITION, t);
            for (int channel = 0; channel < nChannels; ++channel) {
                RandomAccessibleInterval cStep = Views.hyperSlice(tStep, FileInfoConstants.C_AXIS_POSITION, channel);
                RealRandomAccessible real = Views.interpolate(Views.extendZero(cStep), new ClampingNLinearInterpolatorFactory<T>());
                AffineRandomAccessible af = RealViews.affine(real, affine);
/*
                long[] min= new long[3];
                long[] max= new long[3];
                cStep.min(min);
                cStep.max(max);*/
//                double offsetX2 = -shearingSettings.shearingFactorX*max[2]+shearingSettings.shearingFactorX*(max[2]);
//                double offsetX1 = shearingSettings.shearingFactorX*(max[2]);
//                long[] range = {min[0]+(long)offsetX1 ,min[1],min[2],
//                        max[0]+(long)offsetX2,max[1],max[2]};
                /*long[] range = {min[0]+(long)(shearingSettings.shearingFactorX*max[2]/2) ,min[1],min[2],
                        max[0]+(long)(shearingSettings.shearingFactorX*max[2]/2),max[1],max[2]};*/
                //RandomAccessibleInterval intervalView = Views.interval(af, Intervals.createMinMax(range));

                FinalRealInterval transformedRealInterval = affine.estimateBounds(cStep);
                FinalInterval transformedInterval = Utils.asIntegerInterval(transformedRealInterval);
                RandomAccessibleInterval intervalView = Views.interval(af, transformedInterval);
                channelTracks.add(intervalView);
            }
            RandomAccessibleInterval cStackedRAI = Views.stack(channelTracks);
            timeTracks.add(cStackedRAI);
            channelTracks.clear();
        }
        RandomAccessibleInterval sheared = Views.stack(timeTracks);
        sheared = Views.permute(sheared, FileInfoConstants.C_AXIS_POSITION, FileInfoConstants.Z_AXIS_POSITION);
        System.out.println("Time elapsed(ms) " + (System.currentTimeMillis() - startTime));
        selectedImageViewer.repaint(sheared, "sheared");
        double[] centerCoordinates = {sheared.min(FileInfoConstants.X_AXIS_POSITION) / 2.0,
                sheared.max(FileInfoConstants.Y_AXIS_POSITION) / 2.0,
                (sheared.max(FileInfoConstants.Z_AXIS_POSITION) - sheared.min(FileInfoConstants.Z_AXIS_POSITION)) / 2
                        + sheared.min(FileInfoConstants.Z_AXIS_POSITION)};
        selectedImageViewer.shiftImageToCenter(centerCoordinates);
        Utils.doAutoContrastPerChannel(selectedImageViewer);
    }
}
