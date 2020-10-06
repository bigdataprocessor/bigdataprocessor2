package de.embl.cba.bdp2.tools.batch;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.process.crop.Cropper;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.save.SavingSettings;
import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.utils.DimensionOrder;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.bdp2.viewers.ImageViewer;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static de.embl.cba.bdp2.dialog.Utils.selectDirectories;

@Plugin(type = Command.class, menuPath = "Plugins>BigDataTools>EMBL>Ellenberg>Batch Merge Split Chip", initializer = "init")
public class LuxendoBatchMergeSplitChipCommand< R extends RealType< R > & NativeType< R > > implements Command
{
    @Parameter(label = "Voxel Unit")
    public String voxelUnit = "micrometer";

    @Parameter(label = "Voxel Size X")
    public double voxelSpacingMicrometerX = 0.13;

    @Parameter(label = "Voxel Size Y")
    public double voxelSpacingMicrometerY = 0.13;

    @Parameter(label = "Voxel Size Z")
    public double voxelSpacingMicrometerZ = 1.04;

    /**
     * This specifies the regions where the different channels appear on the camera chip
     */
    @Parameter(label = "Channel Regions [ minX, minY, sizeX, sizeY, channel; ... ]")
    public String intervalsString = "896, 46, 1000, 1000, 0; 22, 643, 1000, 1000, 0";

    @Parameter(label = "Tiff Output Compression", choices =
            { SavingSettings.COMPRESSION_NONE,
            SavingSettings.COMPRESSION_ZLIB,
            SavingSettings.COMPRESSION_LZW } )
    public String compression = SavingSettings.COMPRESSION_NONE;

    @Parameter( label = "Crop")
    public boolean doCrop = true;
    private int numIOThreads;

    @Override
    public void run()
    {
        final ArrayList< File > directories = selectDirectories();
        process( directories );
    }

    public void process( ArrayList< File > directories )
    {
        final SavingSettings savingSettings = SavingSettings.getDefaults();
        savingSettings.saveFileType = SavingSettings.SaveFileType.TIFF_VOLUMES;
        savingSettings.numIOThreads = 1; // input is hdf5 => single threaded
        savingSettings.numProcessingThreads = Runtime.getRuntime().availableProcessors();

        final List< long[] > intervalsXYC = Utils.delimitedStringToLongs( intervalsString, ";" );

        /*
         * Get cropping intervals from user
         */
        ArrayList< Interval > croppingIntervals = new ArrayList<>(  );

        if ( doCrop )
        {
            for ( File directory : directories )
            {
                // Open and merge channels from split chip
                final Image< R > merge = Utils.openMergedImageFromLuxendoChannelFolders(
                        voxelUnit,
                        voxelSpacingMicrometerX,
                        voxelSpacingMicrometerY,
                        voxelSpacingMicrometerZ,
                        intervalsXYC,
                        directory );

                final ImageViewer viewer = BigDataProcessor2.showImage( merge );

                final FinalInterval interval = viewer.getVoxelIntervalXYZCTViaDialog( );

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
                    intervalsXYC,
                    new File( directory ) );

            final String outputDirectoryStump = directory.replace( "_channel_0", "" );

            // save full volume
            savingSettings.volumesFilePathStump = outputDirectoryStump + "-stacks" + File.separator + "stack";
            savingSettings.projectionsFilePathStump =
                    outputDirectoryStump + "-projections" + File.separator + "projection";
            savingSettings.saveProjections = ! doCrop; // when not cropping, save full projections
            savingSettings.compression = compression;
            savingSettings.rowsPerStrip = (int) merge.getRai().dimension( DimensionOrder.Y );
            BigDataProcessor2.saveImageAndWaitUntilDone( merge, savingSettings );

            if ( doCrop )
            {
                // crop & save cropped volume
                final Image< R > crop = Cropper.crop5D( merge, croppingIntervals.get( i ) );
                savingSettings.volumesFilePathStump = outputDirectoryStump + "-crop-stacks" + File.separator + "stack";
                savingSettings.saveProjections = true;
                savingSettings.projectionsFilePathStump =
                        outputDirectoryStump + "-crop-projections" + File.separator + "projection";
                savingSettings.rowsPerStrip = (int) crop.getRai().dimension( DimensionOrder.Y );
                BigDataProcessor2.saveImageAndWaitUntilDone( crop, savingSettings);
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
