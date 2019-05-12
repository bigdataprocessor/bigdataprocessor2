package users.isabell;

import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.logging.Logger;
import de.embl.cba.bdp2.process.Cropper;
import de.embl.cba.bdp2.process.splitviewmerge.SplitViewMerger;
import de.embl.cba.bdp2.saving.SavingSettings;
import de.embl.cba.bdp2.ui.BigDataProcessor2;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.bdp2.viewers.ImageViewer;
import net.imagej.ImageJ;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import java.util.ArrayList;


public class MergeSplitChipWorkflow
{
    public static < R extends RealType< R > & NativeType< R > > void main( String[] args)
    {
        final ImageJ imageJ = new ImageJ();
        imageJ.ui().showUI();

        final BigDataProcessor2< R > bdp = new BigDataProcessor2<>();

        ArrayList< String > inputDirectories = new ArrayList<>(  );
        inputDirectories.add( "/Users/tischer/Documents/isabell-schneider-splitchipmerge/stack_0_channel_0" );
        inputDirectories.add( "/Users/tischer/Documents/isabell-schneider-splitchipmerge/stack_1_channel_0" );

        final String voxelUnit = "micrometer";
        double voxelSpacingMicrometerX = 0.13;
        double voxelSpacingMicrometerY = 0.13;
        double voxelSpacingMicrometerZ = 1.04;

        final SavingSettings savingSettings = SavingSettings.getDefaults();
        savingSettings.fileType = SavingSettings.FileType.TIFF_STACKS;
        savingSettings.nThreads = Runtime.getRuntime().availableProcessors();

        final SplitViewMerger merger = new SplitViewMerger();
        merger.setUpperLeftCornerRegionA( 22, 643 );
        merger.setUpperLeftCornerRegionB( 896, 46 );
        merger.setRegionSpan( 1000, 1000 );


        /*
         * Get cropping intervals from user
         */
        ArrayList< Interval > croppingIntervals = new ArrayList<>(  );

        for ( String inputDirectory : inputDirectories )
        {
            final Image< R > image = bdp.openHdf5Image(
                    inputDirectory,
                    FileInfos.SINGLE_CHANNEL_TIMELAPSE,
                    ".*.h5",
                    "Data" );
            image.setVoxelUnit( voxelUnit );
            image.setVoxelSpacing( voxelSpacingMicrometerX, voxelSpacingMicrometerY, voxelSpacingMicrometerZ );

            final Image< R > merge = merger.mergeRegionsAandB( image );

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
            final Image< R > image = bdp.openHdf5Image(
                    inputDirectory,
                    FileInfos.SINGLE_CHANNEL_TIMELAPSE,
                    ".*.h5",
                    "Data" );

            image.setVoxelUnit( voxelUnit );
            image.setVoxelSpacing( voxelSpacingMicrometerX, voxelSpacingMicrometerY, voxelSpacingMicrometerZ );

            // merge
            final Image< R > merge = merger.mergeRegionsAandB( image );
            savingSettings.saveVolumes = true;
            savingSettings.volumesFilePath = inputDirectory + "-stacks/stack";
            savingSettings.saveProjections = false;
            Utils.saveImageAndWaitUntilDone( bdp, savingSettings, merge );

            // crop
            final Image< R > crop = Cropper.crop( merge, croppingIntervals.get( i ) );
            savingSettings.saveVolumes = true;
            savingSettings.volumesFilePath = inputDirectory + "-crop-stacks/stack";
            savingSettings.saveProjections = true;
            savingSettings.projectionsFilePath = inputDirectory + "-crop-projections/projection";
            Utils.saveImageAndWaitUntilDone( bdp, savingSettings, crop );

        }

        Logger.log( "Done!" );
    }


}
