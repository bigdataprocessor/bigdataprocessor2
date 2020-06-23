package develop;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.open.core.NamingSchemes;
import de.embl.cba.bdp2.save.SavingSettings;
import de.embl.cba.bdp2.BigDataProcessor2;

import java.io.File;

import static junit.framework.TestCase.assertTrue;

public class TestSaveLargeSingleChannelH5AsImarisVolumes
{

    public void test( )
    {
        final BigDataProcessor2 bdp = new BigDataProcessor2();

        final String directory =
                "/Users/tischer/Desktop/bdp2/_stack_0_channel_0";

        final String loadingScheme = NamingSchemes.SINGLE_CHANNEL_TIMELAPSE;
        final String filterPattern = ".*.h5";

        final Image image = bdp.openImageFromHdf5(
                directory,
                loadingScheme,
                filterPattern,
                "Data");

        // bdp.showImage( image );

        final SavingSettings savingSettings = SavingSettings.getDefaults();
        savingSettings.saveFileType = SavingSettings.SaveFileType.IMARIS_VOLUMES;
        savingSettings.numIOThreads = 1;
        savingSettings.numProcessingThreads = 4;
        savingSettings.saveProjections = false;
        savingSettings.volumesFilePathStump =
                "/Users/tischer/Desktop/bdp2/_stack_0_channel_0/imaris/volume";
        savingSettings.saveVolumes = true;
//        savingSettings.projectionsFilePath =
//                "/Users/tischer/Desktop/bdp2/_stack_0_channel_0/imaris/projection";

        final File testVolumeFile = new File( savingSettings.volumesFilePathStump + "--C00--T00000.h5" );
        if ( testVolumeFile.exists() ) testVolumeFile.delete();

//        final File testProjectionsFile = new File( savingSettings.projectionsFilePath + "--xyz-max-projection--C00--T00002.tif" );

//        if ( testProjectionsFile.exists() ) testProjectionsFile.delete();

        BigDataProcessor2.saveImageAndWaitUntilDone( image, savingSettings );

        assertTrue( testVolumeFile.exists() );

        System.out.println( "Done!" );
//        assertTrue( testProjectionsFile.exists() );
    }

    public static void main( String[] args )
    {
        new TestSaveLargeSingleChannelH5AsImarisVolumes().test();
    }

}
