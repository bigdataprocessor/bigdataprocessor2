package de.embl.cba.bdp2.tools.batch;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.process.crop.Cropper;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.open.NamingSchemes;
import de.embl.cba.bdp2.save.SavingSettings;
import de.embl.cba.bdp2.BigDataProcessor2;
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

import static de.embl.cba.bdp2.dialog.Utils.selectDirectories;

@Plugin(type = Command.class, menuPath = "Plugins>BigDataTools>EMBL>Ellenberg>Batch Convert and Crop (Broken?)", initializer = "init")
public class LuxendoBatchConvertAndCropCommand< R extends RealType< R > & NativeType< R > > implements Command
{
    @Parameter(label = "Voxel Unit")
    public String voxelUnit = "micrometer";

    @Parameter(label = "Voxel Size X")
    public double voxelSpacingX = 0.104;

    @Parameter(label = "Voxel Size Y")
    public double voxelSpacingY = 0.104;

    @Parameter(label = "Voxel Size Z")
    public double voxelSpacingZ = 1.00;

    @Parameter(label = "Tiff Output Compression", choices = { SavingSettings.COMPRESSION_NONE,
            SavingSettings.COMPRESSION_ZLIB,
            SavingSettings.COMPRESSION_LZW } )
    public String compression = SavingSettings.COMPRESSION_NONE;

//    @Parameter( label = "Crop")
    public boolean doCrop = true;

    @Parameter( label = "Save Projections")
    public boolean doProjections = true;

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
        savingSettings.numIOThreads = Runtime.getRuntime().availableProcessors();

        /*
         * Get cropping intervals from user
         */
        ArrayList< Interval > croppingIntervals = new ArrayList<>(  );

        if ( doCrop )
        {
            for ( File directory : directories )
            {
                // TODO: Deprecated??

                // Open
                final Image< R > image = BigDataProcessor2.openHdf5Series(
                        directory.getAbsolutePath(),
                        NamingSchemes.PATTERN_6,
                        NamingSchemes.PATTERN_LUXENDO,
                        "Data");

                image.setVoxelUnit( voxelUnit );
                image.setVoxelSize( new double[]{ voxelSpacingX, voxelSpacingY, voxelSpacingZ } );
                final ImageViewer viewer = BigDataProcessor2.showImage( image);

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
            final String directory = directories.get( i ).getAbsolutePath();
            final Image< R > image = BigDataProcessor2.openHdf5Series(
                    directory,
                    NamingSchemes.PATTERN_6,
                    NamingSchemes.PATTERN_LUXENDO,
                    "Data");
            image.setVoxelUnit( voxelUnit );
            image.setVoxelSize( new double[]{ voxelSpacingX, voxelSpacingY, voxelSpacingZ } );

            final String outputDirectoryStump = directory.replace( "_channel_0", "" );

            // save full volume
//            savingSettings.saveVolumes = true;
//            savingSettings.volumesFilePath = outputDirectoryStump + "-stacks/stack";
//            savingSettings.projectionsFilePath =
//                    outputDirectoryStump + "-projections/projection";
//            savingSettings.saveProjections = (!doCrop) & doProjections; // when not cropping, save full projections
//            savingSettings.numIOThreads = 3;
//            savingSettings.compression = compression;
//            BigDataProcessor2.saveImageAndWaitUntilDone( savingSettings, image );

            if ( doCrop ) // always true for this Command
            {
                // crop & save cropped volume
                final Image< R > crop = Cropper.crop5D( image, croppingIntervals.get( i ) );
                savingSettings.volumesFilePathStump = outputDirectoryStump + "-crop-stacks/stack";
                savingSettings.saveProjections = doProjections;
                savingSettings.projectionsFilePathStump =
                        outputDirectoryStump + "-crop-projections/projection";
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
