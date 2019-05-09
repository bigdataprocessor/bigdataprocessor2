package users.isabell;

import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.logging.Logger;
import de.embl.cba.bdp2.process.Cropper;
import de.embl.cba.bdp2.process.splitviewmerge.SplitViewMerger;
import de.embl.cba.bdp2.progress.DefaultProgressListener;
import de.embl.cba.bdp2.progress.Progress;
import de.embl.cba.bdp2.saving.SavingSettings;
import de.embl.cba.bdp2.ui.BigDataProcessor2;
import de.embl.cba.bdp2.viewers.ImageViewer;
import net.imagej.ImageJ;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import java.io.IOException;
import java.util.ArrayList;


public class MergeSplitChipWorkflow
{
    public static < R extends RealType< R > & NativeType< R > > void main( String[] args) throws IOException
    {
        final ImageJ imageJ = new ImageJ();
        imageJ.ui().showUI();

        ArrayList< String > inputDirectories = new ArrayList<>(  );
        inputDirectories.add( "/Users/tischer/Documents/isabell-schneider-splitchipmerge/stack_0_channel_0" );
        inputDirectories.add( "/Users/tischer/Documents/isabell-schneider-splitchipmerge/stack_1_channel_0" );

        ArrayList< Interval > croppingIntervals = new ArrayList<>(  );

        final ArrayList< long[] > minima = new ArrayList<>();
        minima.add( new long[]{ 22, 643 } );
        minima.add( new long[]{ 896, 46 } );
        final long[] span = { 1000, 1000 };
        final double[] voxelSpacingMicrometer = { 0.13, 0.13, 1.04 };

        final BigDataProcessor2< R > bdp = new BigDataProcessor2<>();

        final int progressUpdateMillis = 10000;

        final SavingSettings savingSettings = SavingSettings.getDefaults();
        savingSettings.fileType = SavingSettings.FileType.TIFF_STACKS;
        savingSettings.nThreads = Runtime.getRuntime().availableProcessors();
        savingSettings.isotropicProjectionResampling = true;
        savingSettings.isotropicProjectionVoxelSize = 0.5;


        /**
         *
         * Get cropping intervals from user
         *
         */
        for ( String inputDirectory : inputDirectories )
        {
            final Image< R > image = bdp.openHdf5Data(
                    inputDirectory,
                    FileInfos.SINGLE_CHANNEL_TIMELAPSE,
                    ".*.h5",
                    "Data" );

            image.setVoxelUnit( "micrometer" );
            image.setVoxelSpacing( voxelSpacingMicrometer );

            final Image< R > merge = SplitViewMerger.merge( image, minima, span );

            final ImageViewer viewer = bdp.showImage( merge );

            final FinalInterval interval = viewer.get5DIntervalFromUser();

            Logger.log( "Data set: " + inputDirectory );
            Logger.log( "Crop interval: " + interval.toString()   );

            croppingIntervals.add( interval );
        }


        /**
         *
         * Save both the complete merged data
         * as well as the cropped data, with projections
         *
         */
        for ( int i = 0; i < inputDirectories.size(); i++ )
        {
            // open
            final String inputDirectory = inputDirectories.get( i );
            final Image< R > image = bdp.openHdf5Data(
                    inputDirectory,
                    FileInfos.SINGLE_CHANNEL_TIMELAPSE,
                    ".*.h5",
                    "Data" );
            image.setVoxelUnit( "micrometer" );
            image.setVoxelSpacing( voxelSpacingMicrometer );

            // merge
            final Image< R > merge = SplitViewMerger.merge( image, minima, span );
            savingSettings.saveVolumes = true;
            savingSettings.volumesFilePath = inputDirectory + "-stacks/stack";
            savingSettings.saveProjections = false;
            final DefaultProgressListener progress = new DefaultProgressListener();
            bdp.saveImage( merge, savingSettings, progress );
            Logger.log( "Saving: " + savingSettings.volumesFilePath );
            Progress.waitUntilDone( progress, progressUpdateMillis );

            // crop
            final Image< R > crop = Cropper.crop( merge, croppingIntervals.get( i ) );
            savingSettings.saveVolumes = true;
            savingSettings.volumesFilePath = inputDirectory + "-crop-stacks/stack";
            savingSettings.saveProjections = true;
            savingSettings.projectionsFilePath = inputDirectory + "-crop-projections/projection";
            final DefaultProgressListener progressCrop = new DefaultProgressListener();
            bdp.saveImage( crop, savingSettings, progressCrop );
            Logger.log( "Saving: " + savingSettings.volumesFilePath );
            Progress.waitUntilDone( progressCrop, progressUpdateMillis );

        }

        Logger.log( "Done!" );
    }


}
