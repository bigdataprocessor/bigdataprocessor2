package explore;

import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.saving.SavingSettings;
import de.embl.cba.bdp2.ui.BigDataProcessor2;
import de.embl.cba.bdp2.utils.Utils;
import org.junit.Test;

import java.io.File;

import static junit.framework.TestCase.assertTrue;

public class TestSaveLargeSingleChannelH5AsImarisVolumes
{

    public void test( )
    {
        final BigDataProcessor2 bdp = new BigDataProcessor2();

        final String directory =
                "/Users/tischer/Desktop/bdp2/_stack_0_channel_0";

        final String loadingScheme = FileInfos.SINGLE_CHANNEL_TIMELAPSE;
        final String filterPattern = ".*.h5";

        final Image image = bdp.openHdf5Image(
                directory,
                loadingScheme,
                filterPattern,
                "Data");

        // bdp.showImage( image );

        final SavingSettings savingSettings = SavingSettings.getDefaults();
        savingSettings.fileType = SavingSettings.FileType.IMARIS_STACKS;
        savingSettings.numIOThreads = 1;
        savingSettings.numProcessingThreads = 4;
        savingSettings.saveProjections = false;
        savingSettings.volumesFilePath =
                "/Users/tischer/Desktop/bdp2/_stack_0_channel_0/imaris/volume";
        savingSettings.saveVolumes = true;
//        savingSettings.projectionsFilePath =
//                "/Users/tischer/Desktop/bdp2/_stack_0_channel_0/imaris/projection";

        final File testVolumeFile = new File( savingSettings.volumesFilePath + "--C00--T00000.h5" );
        if ( testVolumeFile.exists() ) testVolumeFile.delete();

//        final File testProjectionsFile = new File( savingSettings.projectionsFilePath + "--xyz-max-projection--C00--T00002.tif" );

//        if ( testProjectionsFile.exists() ) testProjectionsFile.delete();

        Utils.saveImageAndWaitUntilDone( bdp, savingSettings, image );

        assertTrue( testVolumeFile.exists() );

        System.out.println( "Done!" );
//        assertTrue( testProjectionsFile.exists() );
    }

    public static void main( String[] args )
    {
        new TestSaveLargeSingleChannelH5AsImarisVolumes().test();
    }

}
