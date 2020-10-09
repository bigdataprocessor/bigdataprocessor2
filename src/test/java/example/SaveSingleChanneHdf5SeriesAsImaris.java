package example;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.process.bin.Binner;
import de.embl.cba.bdp2.open.NamingSchemes;
import de.embl.cba.bdp2.save.SavingSettings;
import de.embl.cba.bdp2.BigDataProcessor2;

public class SaveSingleChanneHdf5SeriesAsImaris
{
    public static void main(String[] args)
    {
        final BigDataProcessor2 bdp = new BigDataProcessor2();

        final String directory =
                "/Users/tischer/Documents/isabell-schneider-splitchipmerge/stack_0_channel_0";

        final String loadingScheme = NamingSchemes.SINGLE_CHANNEL_TIMELAPSE;
        final String filterPattern = ".*.h5";
        final String dataset = "Data";

        final Image image = bdp.openHdf5Series(
                directory,
                loadingScheme,
                filterPattern,
                dataset);

        image.setVoxelUnit( "micrometer" );
        image.setVoxelSize( 0.13, 0.13, 1.04 );

        bdp.showImage( image);

        final Image binnedImage = Binner.bin( image, new long[]{ 3, 3, 3, 0, 0 } );
        //   bdp.showImage( bin );

        final SavingSettings savingSettings = SavingSettings.getDefaults();
        savingSettings.saveFileType = SavingSettings.SaveFileType.IMARIS_VOLUMES;
        savingSettings.numIOThreads = 1;
        savingSettings.numProcessingThreads = 4;
        savingSettings.saveVolumes = true;
        savingSettings.volumesFilePathStump = "/Users/tischer/Desktop/stack_0_channel_0-imaris-volumes/volume";
        savingSettings.saveProjections = true;
        savingSettings.projectionsFilePathStump = "/Users/tischer/Desktop/stack_0_channel_0-imaris-projections/projection";



        BigDataProcessor2.saveImageAndWaitUntilDone( binnedImage, savingSettings );

    }

}
