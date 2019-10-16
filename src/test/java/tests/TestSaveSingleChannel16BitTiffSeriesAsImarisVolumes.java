package tests;

import bdv.img.imaris.Imaris;
import bdv.img.imaris.ImarisImageLoader;
import bdv.util.BdvFunctions;
import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.saving.SavingSettings;
import de.embl.cba.bdp2.ui.BigDataProcessor2;
import loci.common.DebugTools;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static junit.framework.TestCase.assertTrue;

public class TestSaveSingleChannel16BitTiffSeriesAsImarisVolumes
{

    @Test
    public void test( ) throws IOException
    {
        DebugTools.setRootLevel("OFF"); // Bio-Formats

        final BigDataProcessor2 bdp = new BigDataProcessor2();

        final String directory =
                TestConstants.TEST_FOLDER + "test-data/nc1-nt3-calibrated-16bit-tiff";

        final String loadingScheme = FileInfos.SINGLE_CHANNEL_TIMELAPSE;
        final String filterPattern = ".*.tif";

        final Image image = bdp.openImage(
                directory,
                loadingScheme,
                filterPattern );

        // bdp.showImage( image );

        final SavingSettings savingSettings = SavingSettings.getDefaults();
        savingSettings.fileType = SavingSettings.FileType.IMARIS_STACKS;
        savingSettings.numIOThreads = 1;
        savingSettings.numProcessingThreads = 4;
        savingSettings.saveProjections = true;
        savingSettings.volumesFilePath =
                TestConstants.TEST_FOLDER + "test-output/nc1-nt3-calibrated-16bit-tiff-imaris-volumes/volume";
        savingSettings.saveVolumes = true;
        savingSettings.projectionsFilePath =
                TestConstants.TEST_FOLDER + "test-output/nc1-nt3-calibrated-16bit-tiff-imaris-projections/projection";

        final File testVolumeFile = new File( savingSettings.volumesFilePath + "--C00--T00000.h5" );
        if ( testVolumeFile.exists() ) testVolumeFile.delete();

        final File testProjectionsFile = new File( savingSettings.projectionsFilePath + "--xyz-max-projection--C00--T00002.tif" );
        if ( testProjectionsFile.exists() ) testProjectionsFile.delete();

        BigDataProcessor2.saveImageAndWaitUntilDone( savingSettings, image );

        assertTrue( testVolumeFile.exists() );
        assertTrue( testProjectionsFile.exists() );

        if ( TestConstants.interactive )
        {
            BdvFunctions.show( Imaris.openIms( savingSettings.volumesFilePath + ".ims" ) );
        }
    }

    public static void main( String[] args ) throws IOException
    {
        TestConstants.interactive = true;
        new TestSaveSingleChannel16BitTiffSeriesAsImarisVolumes().test();
    }

}
