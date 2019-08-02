package tests;

import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.saving.SavingSettings;
import de.embl.cba.bdp2.ui.BigDataProcessor2;
import org.junit.Test;

import java.io.File;

import static junit.framework.TestCase.assertTrue;

public class TestSaveSingleChannelLZWTiffSeriesAsTiffStacks
{
    @Test
    public void test( )
    {
        final BigDataProcessor2 bdp = new BigDataProcessor2();

        final String directory =
                "/Users/tischer/Documents/fiji-plugin-bigDataTools2/src/test/resources/test-data/nc1-nt3-calibrated-tiff-lzw";

        final String loadingScheme = FileInfos.SINGLE_CHANNEL_TIMELAPSE;
        final String filterPattern = ".*.ome.tif";

        final Image image = bdp.openImage(
                directory,
                loadingScheme,
                filterPattern );

        // bdp.showImage( image );

        final SavingSettings savingSettings = SavingSettings.getDefaults();
        savingSettings.fileType = SavingSettings.FileType.TIFF_STACKS;
        savingSettings.numIOThreads = 1;
        savingSettings.numProcessingThreads = 4;
        savingSettings.saveProjections = false;
        savingSettings.saveVolumes = true;
        savingSettings.volumesFilePath =
                "/Users/tischer/Documents/fiji-plugin-bigDataTools2/src/test/resources/test-output/nc1-nt3-calibrated-tiff-volumes/volume";

        final File testVolumeFile = new File( savingSettings.volumesFilePath + "--C00--T00000.tif" );
        if ( testVolumeFile.exists() ) testVolumeFile.delete();

        BigDataProcessor2.saveImageAndWaitUntilDone( savingSettings, image );

        assertTrue( testVolumeFile.exists() );
    }

    public static void main( String[] args )
    {
        new TestSaveSingleChannelLZWTiffSeriesAsTiffStacks().test();
    }

}
