package de.embl.cba.bdp2.command;

import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.crop.Cropper;
import de.embl.cba.bdp2.logging.Logger;
import de.embl.cba.bdp2.process.splitviewmerge.SplitViewMerger;
import de.embl.cba.bdp2.saving.SavingSettings;
import de.embl.cba.bdp2.ui.BigDataProcessor2;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;

import static de.embl.cba.bdp2.ui.Utils.selectDirectories;

@Plugin(type = Command.class, menuPath = "Plugins>BigDataTools>Extras>Merge Luxendo Split Chip", initializer = "init")
public class BatchMergeSplitChipCommand < R extends RealType< R > & NativeType< R > > implements Command
{
    @Parameter(label = "Voxel Unit")
    public String voxelUnit = "micrometer";

    @Parameter(label = "Voxel Size X")
    public double voxelSpacingMicrometerX = 0.13;

    @Parameter(label = "Voxel Size Y")
    public double voxelSpacingMicrometerY = 0.13;

    @Parameter(label = "Voxel Size Z")
    public double voxelSpacingMicrometerZ = 1.04;

    @Parameter(label = "Intervals [ minX, minY, sizeX, sizeY, channel; ... ]")
    public String intervalsString = "896, 46, 1000, 1000, 0; 22, 643, 1000, 1000, 0";

    @Parameter( label = "Crop")
    public boolean doCrop = true;

    @Override
    public void run()
    {
        final ArrayList< File > directories = selectDirectories();
        process( directories );
    }

    public void process( ArrayList< File > directories )
    {
        final BigDataProcessor2< R > bdp = new BigDataProcessor2<>();

        final SavingSettings savingSettings = SavingSettings.getDefaults();
        savingSettings.fileType = SavingSettings.FileType.TIFF_STACKS;
        savingSettings.numIOThreads = Runtime.getRuntime().availableProcessors();

        final SplitViewMerger merger = new SplitViewMerger();

        final String[] intervals =
                Utils.delimitedStringToStringArray( intervalsString, ";" );

        for ( String interval : intervals )
        {
            final int[] ints = Utils.delimitedStringToIntegerArray( interval, "," );
            merger.addIntervalXYC( ints[ 0 ], ints[ 1 ], ints[ 2 ], ints[ 3 ], ints[ 4 ] );
        }

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
            final String directory = directories.get( i ).toString();
            final Image< R > merge = Utils.openMergedImageFromLuxendoChannelFolders(
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
            BigDataProcessor2.saveImageAndWaitUntilDone( savingSettings, merge );

            if ( doCrop )
            {
                // crop & save cropped volume
                final Image< R > crop = Cropper.crop( merge, croppingIntervals.get( i ) );
                savingSettings.saveVolumes = true;
                savingSettings.volumesFilePath = outputDirectoryStump + "-crop-stacks/stack";
                savingSettings.saveProjections = true;
                savingSettings.projectionsFilePath =
                        outputDirectoryStump + "-crop-projections/projection";
                BigDataProcessor2.saveImageAndWaitUntilDone( savingSettings, crop );
            }
        }

        Logger.log( "Done!" );
    }

    public void test()
    {
        final ArrayList< File > directories = new ArrayList<>(  );
        directories.add( new File( "/Volumes/cba/exchange/Isabell_Schneider/Example_images/20190329_H2B-mCherry_0.1_EGFP_0.37_thick_sheet_2_cell/stack_0_channel_0") );
        process( directories );
    }
}
