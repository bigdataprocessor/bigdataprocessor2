// #@File[] (label="Select", style="both") directories

import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.logging.Logger;
import de.embl.cba.bdp2.process.Cropper;
import de.embl.cba.bdp2.process.splitviewmerge.SplitViewMerger;
import de.embl.cba.bdp2.saving.SavingSettings;
import de.embl.cba.bdp2.ui.BigDataProcessor2;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;

final BigDataProcessor2 bdp = new BigDataProcessor2<>();

final String voxelUnit = "micrometer";
double voxelSpacingMicrometerX = 0.13;
double voxelSpacingMicrometerY = 0.13;
double voxelSpacingMicrometerZ = 1.04;


int numberOfProcessors = Runtime.getRuntime().availableProcessors();
Logger.log( "Number of processors: " + numberOfProcessors );

final SavingSettings savingSettings = SavingSettings.getDefaults();
savingSettings.fileType = SavingSettings.FileType.TIFF_STACKS;
savingSettings.numIOThreads = Math.sqrt( numberOfProcessors ) + 1;
savingSettings.numProcessingThreads = Math.sqrt( numberOfProcessors ) + 1;

Logger.log( "Number of IO threads: " + savingSettings.numIOThreads );
Logger.log( "Number of processing threads: " + savingSettings.numProcessingThreads );


final SplitViewMerger merger = new SplitViewMerger();
merger.setUpperLeftCornerRegionA( 22, 643 );
merger.setUpperLeftCornerRegionB( 896, 46 );
merger.setRegionSpan( 1000, 1000 );


/*
 * Get cropping intervals from user
 */
ArrayList< Interval > croppingIntervals = new ArrayList<>(  );

for ( File directory : directories )
{
    final Image image = bdp.openHdf5Image(
            directory.toString(),
            FileInfos.SINGLE_CHANNEL_TIMELAPSE,
            ".*.h5",
            "Data" );
    image.setVoxelUnit( voxelUnit );
    image.setVoxelSpacing(
            voxelSpacingMicrometerX,
            voxelSpacingMicrometerY,
            voxelSpacingMicrometerZ );

    final Image merge = merger.mergeRegionsAandB( image );

    final BdvImageViewer viewer = bdp.showImage( merge );

    final FinalInterval interval = viewer.get5DIntervalFromUser( false );

    Logger.log( "Data set: " + directory );
    Logger.log( "Crop interval: " + interval.toString()   );
    croppingIntervals.add( interval );
}


/**
 *
 * Save both the complete merged data
 * as well as the cropped data, with projections
 *
 */
for ( int i = 0; i < directories.length; i++ )
{
    // open
    final String directory = directories[ i ].toString();
    final Image image = bdp.openHdf5Image(
            directory,
            FileInfos.SINGLE_CHANNEL_TIMELAPSE,
            ".*.h5",
            "Data" );

    image.setVoxelUnit( voxelUnit );
    image.setVoxelSpacing(
            voxelSpacingMicrometerX,
            voxelSpacingMicrometerY,
            voxelSpacingMicrometerZ );

    // merge
    final Image merge = merger.mergeRegionsAandB( image );
    savingSettings.saveVolumes = true;
    savingSettings.volumesFilePath = directory + "-stacks/stack";
    savingSettings.saveProjections = false;
    Utils.saveImageAndWaitUntilDone( bdp, savingSettings, merge );

    // crop
    final Image crop = Cropper.crop( merge, croppingIntervals.get( i ) );
    savingSettings.saveVolumes = true;
    savingSettings.volumesFilePath = directory + "-crop-stacks/stack";
    savingSettings.saveProjections = true;
    savingSettings.projectionsFilePath =
            directory + "-crop-projections/projection";
    Utils.saveImageAndWaitUntilDone( bdp, savingSettings, crop );

}

Logger.log( "Done!" );

