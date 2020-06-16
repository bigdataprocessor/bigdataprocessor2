package test;

import bdv.img.imaris.Imaris;
import bdv.util.BdvFunctions;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.open.core.NamingScheme;
import de.embl.cba.bdp2.save.SavingSettings;
import de.embl.cba.bdp2.BigDataProcessor2;
import loci.common.DebugTools;

import java.io.File;
import java.io.IOException;

import static junit.framework.TestCase.assertTrue;

public class TestSaveSingleChannel16BitTiffSeriesAsImarisVolumes
{

    //@Test
    public void test( ) throws IOException
    {
        DebugTools.setRootLevel("OFF"); // Bio-Formats

        final BigDataProcessor2 bdp = new BigDataProcessor2();

        final String directory =
                TestConstants.TEST_FOLDER + "test-data/nc1-nt3-calibrated-16bit-tiff";

        final String loadingScheme = NamingScheme.SINGLE_CHANNEL_TIMELAPSE;
        final String filterPattern = ".*.tif";

        final Image image = bdp.openImage(
                directory,
                loadingScheme,
                filterPattern );

        // bdp.showImage( image );

        final SavingSettings savingSettings = SavingSettings.getDefaults();
        savingSettings.fileType = SavingSettings.FileType.IMARIS_VOLUMES;
        savingSettings.numIOThreads = 1;
        savingSettings.numProcessingThreads = 4;
        savingSettings.saveProjections = true;
        savingSettings.volumesFilePathStump =
                TestConstants.TEST_FOLDER + "test-output/nc1-nt3-calibrated-16bit-tiff-imaris-volumes/volume";
        savingSettings.saveVolumes = true;
        savingSettings.projectionsFilePathStump =
                TestConstants.TEST_FOLDER + "test-output/nc1-nt3-calibrated-16bit-tiff-imaris-projections/projection";

        final File testVolumeFile = new File( savingSettings.volumesFilePathStump + "--C00--T00000.h5" );
        if ( testVolumeFile.exists() ) testVolumeFile.delete();

        final File testProjectionsFile = new File( savingSettings.projectionsFilePathStump + "--xyz-max-projection--C00--T00002.tif" );
        if ( testProjectionsFile.exists() ) testProjectionsFile.delete();

        BigDataProcessor2.saveImageAndWaitUntilDone( image, savingSettings );

        assertTrue( testVolumeFile.exists() );
        assertTrue( testProjectionsFile.exists() );

        if ( TestConstants.interactive )
        {
            BdvFunctions.show( Imaris.openIms( savingSettings.volumesFilePathStump + ".ims" ) );
        }
    }

    public static void main( String[] args ) throws IOException
    {
        TestConstants.interactive = true;
        new TestSaveSingleChannel16BitTiffSeriesAsImarisVolumes().test();
    }

}
