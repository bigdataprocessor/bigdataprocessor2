package users.isabell;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.open.files.FileInfos;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.splitviewmerge.SplitViewMerger;
import de.embl.cba.bdp2.save.SavingSettings;
import de.embl.cba.bdp2.BigDataProcessor2;
import net.imagej.ImageJ;
import net.imglib2.Interval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import java.io.File;
import java.util.ArrayList;

import static de.embl.cba.bdp2.dialog.Utils.selectDirectories;

public class MergeSplitChipWorkflow
{
    public static < R extends RealType< R > & NativeType< R > >
    void main( String[] args )
    {
        final ImageJ imageJ = new ImageJ();
        imageJ.ui().showUI();

        final ArrayList< File > directories = selectDirectories();

        final String voxelUnit = "micrometer";
        double voxelSpacingMicrometerX = 0.13;
        double voxelSpacingMicrometerY = 0.13;
        double voxelSpacingMicrometerZ = 1.04;

        final SavingSettings savingSettings = SavingSettings.getDefaults();
        savingSettings.fileType = SavingSettings.FileType.TIFF_VOLUMES;
        savingSettings.numIOThreads = Runtime.getRuntime().availableProcessors();

        final SplitViewMerger merger = new SplitViewMerger();
        merger.addIntervalXYC( 86, 5, 1000, 1000, 0 );
        merger.addIntervalXYC( 1, 65, 1000, 1000, 0 );


        /*
         * Get cropping intervals from user
         */
        ArrayList< Interval > croppingIntervals = new ArrayList<>(  );

        for ( File directory : directories )
        {
            final Image< R > image = BigDataProcessor2.openHdf5Image(
                    directory.toString(),
                    FileInfos.SINGLE_CHANNEL_TIMELAPSE,
                    ".*.h5",
                    "Data" );
            image.setVoxelUnit( voxelUnit );
            image.setVoxelSpacing(
                    voxelSpacingMicrometerX,
                    voxelSpacingMicrometerY,
                    voxelSpacingMicrometerZ );

//            final Image< R > merge = merger.mergeRegionsAandB( image );

//            final BdvImageViewer viewer = bdp.showImage( merge );
//
//            final FinalInterval interval = viewer.get5DIntervalFromUser( false );
//
//            Logger.log( "Data set: " + directory );
//            Logger.log( "Crop interval: " + interval.toString()   );
//            croppingIntervals.add( interval );
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
            final String directory = directories.get( i ).toString();
            final Image< R > image = BigDataProcessor2.openHdf5Image(
                    directory,
                    FileInfos.SINGLE_CHANNEL_TIMELAPSE,
                    ".*.h5",
                    "Data" );

            image.setVoxelUnit( voxelUnit );
            image.setVoxelSpacing(
                    voxelSpacingMicrometerX,
                    voxelSpacingMicrometerY,
                    voxelSpacingMicrometerZ );

//            // merge
//            final Image< R > merge = merger.mergeRegionsAandB( image );
//            savingSettings.saveVolumes = true;
//            savingSettings.volumesFilePath = directory + "-stacks/stack";
//            savingSettings.saveProjections = false;
//            Utils.saveImageAndWaitUntilDone( bdp, savingSettings, merge );
//
//            // crop
//            final Image< R > crop = Cropper.crop( merge, croppingIntervals.get( i ) );
//            savingSettings.saveVolumes = true;
//            savingSettings.volumesFilePath = directory + "-crop-stacks/stack";
//            savingSettings.saveProjections = true;
//            savingSettings.projectionsFilePath =
//                    directory + "-crop-projections/projection";
//            Utils.saveImageAndWaitUntilDone( bdp, savingSettings, crop );

        }

        Logger.log( "Done!" );
    }


}
