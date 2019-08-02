//#@File[] (label="Select", style="both") directories

import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.logging.Logger;
import de.embl.cba.bdp2.crop.Cropper;
import de.embl.cba.bdp2.process.splitviewmerge.SplitViewMerger;
import de.embl.cba.bdp2.saving.SavingSettings;
import de.embl.cba.bdp2.ui.BigDataProcessor2;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.bdp2.viewers.BdvImageViewer
import net.imglib2.FinalInterval;
import net.imglib2.Interval

final BigDataProcessor2 bdp = new BigDataProcessor2<>();

final String voxelUnit = "micrometer";
double voxelSpacingMicrometerX = 0.13;
double voxelSpacingMicrometerY = 0.13;
double voxelSpacingMicrometerZ = 1.04;

boolean doCrop = false;

final SavingSettings savingSettings = SavingSettings.getDefaults();
savingSettings.fileType = SavingSettings.FileType.TIFF_STACKS;
savingSettings.numIOThreads = Runtime.getRuntime().availableProcessors();

final SplitViewMerger merger = new SplitViewMerger();
merger.addIntervalXYC( 896, 46, 1000, 1000, 0 );
merger.addIntervalXYC( 22, 643, 1000, 1000, 0 );
//merger.addIntervalXYC( 896, 46, 1000, 1000, 1 );

/*
 * Get cropping intervals from user
 */
ArrayList< Interval > croppingIntervals = new ArrayList<>(  );

if ( doCrop )
{
    for ( File directory : directories )
    {
        // Open
        final Image merge = Utils.openMergedImageFromLuxendoChannelFolders(
                bdp,
                voxelUnit,
                voxelSpacingMicrometerX,
                voxelSpacingMicrometerY,
                voxelSpacingMicrometerZ,
                merger,
                directory );

        final BdvImageViewer viewer = bdp.showImage( merge );

        final FinalInterval interval = viewer.get5DIntervalFromUser( false );

        Logger.log( "Data set: " + directory );
        Logger.log( "Crop interval: " + interval.toString() );
        croppingIntervals.add( interval );

        viewer.close();
    }
}


/**
 *
 * Save both the complete merged data
 * as well as the cropped data, with projections
 *
 */
for ( int i = 0; i < directories.size(); i++ )
{
    // open
    final String directory = directories[ i ].toString();
    final Image merge = Utils.openMergedImageFromLuxendoChannelFolders(
            bdp,
            voxelUnit,
            voxelSpacingMicrometerX,
            voxelSpacingMicrometerY,
            voxelSpacingMicrometerZ,
            merger,
            new File( directory ) );

    final String outputDirectoryStump = directory.replace( "_channel_0", "" );

    // save full volume
    savingSettings.saveVolumes = true;
    savingSettings.volumesFilePath = outputDirectoryStump + "-stacks/stack";
    savingSettings.saveProjections = false;
    savingSettings.numIOThreads = 3;
    BigDataProcessor2.saveImageAndWaitUntilDone( bdp, savingSettings, merge );

    if ( doCrop )
    {
        // crop & save cropped volume
        final Image crop = Cropper.crop( merge, croppingIntervals.get( i ) );
        savingSettings.saveVolumes = true;
        savingSettings.volumesFilePath = outputDirectoryStump + "-crop-stacks/stack";
        savingSettings.saveProjections = true;
        savingSettings.projectionsFilePath =
                outputDirectoryStump + "-crop-projections/projection";
        BigDataProcessor2.saveImageAndWaitUntilDone( bdp, savingSettings, crop );
    }
}

Logger.log( "Done!" );
