package users.isabell;

import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.logging.Logger;
import de.embl.cba.bdp2.process.Cropper;
import de.embl.cba.bdp2.process.splitviewmerge.SplitViewMerger;
import de.embl.cba.bdp2.saving.SavingSettings;
import de.embl.cba.bdp2.ui.BigDataProcessor2;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import net.imagej.ImageJ;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import java.io.File;
import java.util.ArrayList;

import static de.embl.cba.bdp2.ui.Utils.selectDirectories;

public class MergeSplitChipWorkflowTwoChannels
{
    public static < R extends RealType< R > & NativeType< R > >
    void main( String[] args )
    {
        final ImageJ imageJ = new ImageJ();
        imageJ.ui().showUI();

        final BigDataProcessor2< R > bdp = new BigDataProcessor2<>();

        final ArrayList< File > directories = selectDirectories();

        directories.clear();
        directories.add( new File( "/Users/tischer/Desktop/stack_10_channel_0" ) );
//        directories.add( new File( "/Volumes/cba/exchange/Isabell_Schneider/3-Color/stack_11_channel_0" ) );

        final String voxelUnit = "micrometer";
        double voxelSpacingMicrometerX = 0.13;
        double voxelSpacingMicrometerY = 0.13;
        double voxelSpacingMicrometerZ = 1.04;

        final SavingSettings savingSettings = SavingSettings.getDefaults();
        savingSettings.fileType = SavingSettings.FileType.TIFF_STACKS;
        savingSettings.numIOThreads = Runtime.getRuntime().availableProcessors();

        final SplitViewMerger merger = new SplitViewMerger();
        merger.addIntervalXYC( 896, 46, 1000, 1000, 0 );
        merger.addIntervalXYC( 22, 643, 1000, 1000, 0 );
        merger.addIntervalXYC( 896, 46, 1000, 1000, 1 );

        /*
         * Get cropping intervals from user
         */
        ArrayList< Interval > croppingIntervals = new ArrayList<>(  );

        for ( File directory : directories )
        {
            // Open
            final Image< R > merge = openMergedImage(
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
            Logger.log( "Crop interval: " + interval.toString()   );
            croppingIntervals.add( interval );
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
            final Image< R > merge = openMergedImage(
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
            Utils.saveImageAndWaitUntilDone( bdp, savingSettings, merge );

//            // crop & save cropped volume
//            final Image< R > crop = Cropper.crop( merge, croppingIntervals.get( i ) );
//            savingSettings.saveVolumes = true;
//            savingSettings.volumesFilePath = outputDirectoryStump + "-crop-stacks/stack";
//            savingSettings.saveProjections = true;
//            savingSettings.projectionsFilePath =
//                    outputDirectoryStump + "-crop-projections/projection";
//            Utils.saveImageAndWaitUntilDone( bdp, savingSettings, crop );

        }

        Logger.log( "Done!" );
    }

    public static < R extends RealType< R > & NativeType< R > > Image< R > openMergedImage(
            BigDataProcessor2< R > bdp,
            String voxelUnit,
            double voxelSpacingMicrometerX,
            double voxelSpacingMicrometerY,
            double voxelSpacingMicrometerZ,
            SplitViewMerger merger,
            File directory )
    {
        final String subFolderPattern = directory.getName().replace( "channel_0", "channel_.*" );
        final String parentFolder = directory.getParent();

        final Image< R > image = bdp.openHdf5Image(
                parentFolder,
                FileInfos.LOAD_CHANNELS_FROM_FOLDERS,
                subFolderPattern + File.separator + ".*.h5",
                "Data" );

        image.setVoxelUnit( voxelUnit );
        image.setVoxelSpacing(
                voxelSpacingMicrometerX,
                voxelSpacingMicrometerY,
                voxelSpacingMicrometerZ );

        return merger.mergeIntervalsXYC( image );
    }


}
