package users.isabell;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.crop.Cropper;
import de.embl.cba.bdp2.splitviewmerge.SplitViewMerger;
import de.embl.cba.bdp2.save.SavingSettings;
import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import net.imagej.ImageJ;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import java.io.File;
import java.util.ArrayList;

import static de.embl.cba.bdp2.dialog.Utils.selectDirectories;

public class MergeSplitChipWorkflowMultiChannel
{
    public static < R extends RealType< R > & NativeType< R > >
    void main( String[] args )
    {

        final ImageJ imageJ = new ImageJ();
        imageJ.ui().showUI();

        final ArrayList< File > directories = selectDirectories();

        directories.clear();
//        directories.add( new File( "/Users/tischer/Desktop/isabell/stack_10_channel_0" ) );
        //directories.add( new File( "/Volumes/cba/exchange/Isabell_Schneider/3-Color/stack_11_channel_0" ) );
		directories.add( new File( "/Volumes/cba/exchange/Isabell_Schneider/Example_images/20190329_H2B-mCherry_0.1_EGFP_0.37_thick_sheet_2_cell/stack_0_channel_0") );


        final String voxelUnit = "micrometer";
        double voxelSpacingMicrometerX = 0.13;
        double voxelSpacingMicrometerY = 0.13;
        double voxelSpacingMicrometerZ = 1.04;

        boolean doCrop = true;

        final SavingSettings savingSettings = SavingSettings.getDefaults();
        savingSettings.fileType = SavingSettings.FileType.TIFF_VOLUMES;
        savingSettings.numIOThreads = Runtime.getRuntime().availableProcessors();

        final SplitViewMerger merger = new SplitViewMerger();
        merger.addIntervalXYC( 896, 46, 1000, 1000, 0 );
        merger.addIntervalXYC( 22, 643, 1000, 1000, 0 );
//        merger.addIntervalXYC( 896, 46, 1000, 1000, 1 );

        /*
         * Get cropping intervals from user
         */
        ArrayList< Interval > croppingIntervals = new ArrayList<>(  );

        if ( doCrop )
        {
            for ( File directory : directories )
            {
                // Open
                final Image< R > merge = Utils.openMergedImageFromLuxendoChannelFolders(
                        voxelUnit,
                        voxelSpacingMicrometerX,
                        voxelSpacingMicrometerY,
                        voxelSpacingMicrometerZ,
                        merger,
                        directory );

                final BdvImageViewer viewer = BigDataProcessor2.showImage( merge);

                final FinalInterval interval = viewer.getVoxelIntervalXYZCTDialog( false );

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
            final String directory = directories.get( i ).toString();
            final Image< R > merge = Utils.openMergedImageFromLuxendoChannelFolders(
                    voxelUnit,
                    voxelSpacingMicrometerX,
                    voxelSpacingMicrometerY,
                    voxelSpacingMicrometerZ,
                    merger,
                    new File( directory ) );

            final String outputDirectoryStump = directory.replace( "_channel_0", "" );

            // save full volume
            savingSettings.saveVolumes = true;
            savingSettings.volumesFilePathStump = outputDirectoryStump + "-stacks/stack";
            savingSettings.saveProjections = false;
            savingSettings.numIOThreads = 3;
            BigDataProcessor2.saveImageAndWaitUntilDone( merge, savingSettings );

            if ( doCrop )
            {
                // crop & save cropped volume
                final Image< R > crop = Cropper.crop5D( merge, croppingIntervals.get( i ) );
                savingSettings.saveVolumes = true;
                savingSettings.volumesFilePathStump = outputDirectoryStump + "-crop-stacks/stack";
                savingSettings.saveProjections = true;
                savingSettings.projectionsFilePathStump =
                        outputDirectoryStump + "-crop-projections/projection";
                BigDataProcessor2.saveImageAndWaitUntilDone( crop, savingSettings );
            }
        }

        Logger.log( "Done!" );
    }


}
