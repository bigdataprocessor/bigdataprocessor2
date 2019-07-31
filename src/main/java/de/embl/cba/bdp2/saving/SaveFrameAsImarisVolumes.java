package de.embl.cba.bdp2.saving;

import de.embl.cba.bdp2.logging.Logger;
import de.embl.cba.bdp2.process.Duplicator;
import de.embl.cba.bdp2.utils.DimensionOrder;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.imaris.H5DataCubeWriter;
import de.embl.cba.imaris.ImarisDataSet;
import ij.ImagePlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static de.embl.cba.bdp2.saving.ProjectionXYZ.saveAsTiffXYZMaxProjection;

public class SaveFrameAsImarisVolumes< R extends RealType< R > & NativeType< R >> implements Runnable {
    private int t;
    private final int nFrames;
    private final int nChannels;
    private AtomicInteger counter;
    private SavingSettings settings;
    private final long startTime;
    private ImarisDataSet imarisDataSetProperties;
    private final AtomicBoolean stop;
    private final RandomAccessibleInterval rai;

    public SaveFrameAsImarisVolumes(
            SavingSettings settings,
            ImarisDataSet imarisDataSet,
            int t,
            AtomicInteger counter,
            long startTime,
            AtomicBoolean stop)
    {
        rai = settings.rai;
        this.nFrames = Math.toIntExact( rai.dimension( DimensionOrder.T ) );
        this.nChannels = Math.toIntExact( rai.dimension( DimensionOrder.C ) );
        this.settings = settings;
        this.t = t;
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
                settings.saveVolumes = false;
                Logger.progress("Stopped saving thread: ", "" + this.t );
                return;
            }


            final RandomAccessibleInterval< R > volumeRai
                    = new Duplicator().copyVolumeFromRai(
                            rai, c, t, settings.numProcessingThreads );

            ImagePlus imagePlus =
                    Utils.wrap3DRaiToCalibratedImagePlus(
                            volumeRai,
                            settings.voxelSpacing,
                            settings.voxelUnit,
                            "");

            // Save volume
            if ( settings.saveVolumes )
            {
                start = System.currentTimeMillis();
                H5DataCubeWriter writer = new H5DataCubeWriter();

                writer.writeImarisCompatibleResolutionPyramid(
                        imagePlus,
                        imarisDataSetProperties,
                        c,
                        t );

                Logger.debug( "Save data cube as Imaris [ s ]: "
                        + ( System.currentTimeMillis() - start ) / 1000);
            }

            // Save projections
            if ( settings.saveProjections )
                saveAsTiffXYZMaxProjection( imagePlus, c, t, settings.projectionsFilePath );

            counter.incrementAndGet();

            if (!stop.get())
                SaveImgHelper.documentProgress( totalFiles, counter, startTime );

            imagePlus = null;
            System.gc();
        }


    }

}
