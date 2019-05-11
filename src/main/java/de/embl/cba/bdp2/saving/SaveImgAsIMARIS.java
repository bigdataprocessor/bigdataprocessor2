package de.embl.cba.bdp2.saving;

import de.embl.cba.bdp2.logging.Logger;
import de.embl.cba.bdp2.utils.DimensionOrder;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.imaris.H5DataCubeWriter;
import de.embl.cba.imaris.ImarisDataSet;
import ij.ImagePlus;
import net.imagej.ImgPlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class SaveImgAsIMARIS<T extends RealType<T> & NativeType<T>> implements Runnable {
    private int timePoint;
    private final int nFrames;
    private final int nChannels;
    private AtomicInteger counter;
    private SavingSettings savingSettings;
    private final long startTime;
    private ImarisDataSet imarisDataSetProperties;
    private final AtomicBoolean stop;
    private final RandomAccessibleInterval rai;

    public SaveImgAsIMARIS(
            SavingSettings savingSettings,
            ImarisDataSet imarisDataSet,
            int timePoint,
            AtomicInteger counter,
            long startTime,
            AtomicBoolean stop)
    {
        rai = savingSettings.rai;
        this.nFrames = Math.toIntExact( rai.dimension( DimensionOrder.T ) );
        this.nChannels = Math.toIntExact( rai.dimension( DimensionOrder.C ) );
        this.savingSettings = savingSettings;
        this.timePoint = timePoint;
        this.counter = counter;
        this.startTime = startTime;
        this.imarisDataSetProperties = imarisDataSet;
        this.stop = stop;
    }

    @Override
    public void run() {

        // TODO:
        // - check whether enough RAM is available to execute current thread
        // - if not, merge GC and wait until there is enough
        // - estimate 3x more RAM then actually necessary
        // - if waiting takes to long somehow terminate in a nice way

//        long freeMemoryInBytes = IJ.maxMemory() - IJ.currentMemory();
//        long numBytesOfImage = image.dimension(FileInfoConstants.X) *
//                image.dimension(FileInfoConstants.Y) *
//                image.dimension(FileInfoConstants.Z) *
//                image.dimension(FileInfoConstants.C) *
//                image.dimension(FileInfoConstants.T) *
//                files.bitDepth / 8;
//
//        if (numBytesOfImage > 1.5 * freeMemoryInBytes) {
//            // TODO: do something...
//        }

        long start;
        final long totalFiles = nFrames * nChannels;

        for ( int c = 0; c < nChannels; c++ )
        {
            if ( stop.get() ) {
                savingSettings.saveVolumes = false;
                Logger.progress("Stopped saving thread: ", "" + this.timePoint );
                return;
            }

            // TODO: This involves both loading and computation and thus
            // could be done in parallel to the writing...
            final ImagePlus duplicate = getImagePlus( c );

            // Save volume
            if ( savingSettings.saveVolumes )
            {
                start = System.currentTimeMillis();
                H5DataCubeWriter writer = new H5DataCubeWriter();
                writer.writeImarisCompatibleResolutionPyramid(
                        duplicate, imarisDataSetProperties, c, this.timePoint );
                System.out.println( "Save data cube as Imaris [ s ]: "
                        + ( System.currentTimeMillis() - start ) / 1000);
            }

            // Save projections
            if (savingSettings.saveProjections ) {
                // TODO
//                String projectionPath = savingSettings.projectionsFilePath;
//                String sC = String.format("%1$02d", c);
//                String sT = String.format("%1$05d", current_t);
//                projectionPath = projectionPath + "--C" + sC + "--T" + sT + ".tif";
//                SaveImgAsTIFFStacks.saveAsTiffXYZMaxProjection(
//                        imagePlus, c, this.current_t, projectionPath);
            }

            counter.incrementAndGet();

            if (!stop.get())
                SaveImgHelper.documentProgress( totalFiles, counter, startTime );

        }
    }

    public ImagePlus getImagePlus( int c )
    {
        long start;
        long[] minInterval = new long[]{
                rai.min( DimensionOrder.X ),
                rai.min( DimensionOrder.Y ),
                rai.min( DimensionOrder.Z ),
                c,
                this.timePoint };
        long[] maxInterval = new long[]{
                rai.max( DimensionOrder.X ),
                rai.max( DimensionOrder.Y ),
                rai.max( DimensionOrder.Z ),
                c,
                this.timePoint };

        RandomAccessibleInterval< T > oneChannelAndTimePoint =
                Views.interval(rai, minInterval, maxInterval);


        ImagePlus imagePlus =
                Utils.wrapToCalibratedImagePlus(
                        oneChannelAndTimePoint,
                        savingSettings.voxelSpacing,
                        savingSettings.voxelUnit,
                    "BinnedWrapped");

        // force into RAM
        start = System.currentTimeMillis();
        final ImagePlus duplicate = imagePlus.duplicate();
        System.out.println( "Load and process data cube [ s ]: " + ( System.currentTimeMillis() - start ) / 1000);

        return duplicate;
    }


}
