package tests;

import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.process.Binner;
import de.embl.cba.bdp2.saving.SavingSettings;
import de.embl.cba.bdp2.ui.BigDataProcessor2;
import de.embl.cba.bdp2.utils.Utils;
import org.junit.Test;

import java.io.File;

import static junit.framework.TestCase.assertTrue;

public class TestSaveSingleChannelTiffSeriesAsTiffStacks
{
    @Test
    public static void main(String[] args)
    {
        final BigDataProcessor2 bdp = new BigDataProcessor2();

        final String directory =
                "/Users/tischer/Documents/fiji-plugin-bigDataTools2/src/test/resources/test-data/nc1-nt3-calibrated-tiff";

        final String loadingScheme = FileInfos.SINGLE_CHANNEL_TIMELAPSE;
        final String filterPattern = ".*.tif";

        final Image image = bdp.openImage(
                directory,
                loadingScheme,
                filterPattern );

        bdp.showImage( image );

        final SavingSettings savingSettings = SavingSettings.getDefaults();
        savingSettings.fileType = SavingSettings.FileType.TIFF_STACKS;
        savingSettings.numIOThreads = 1;
        savingSettings.numProcessingThreads = 4;
        savingSettings.saveProjections = true;
        savingSettings.volumesFilePath =
                "/Users/tischer/Documents/fiji-plugin-bigDataTools2/src/test/resources/test-saving-output/nc1-nt3-calibrated-tiff-volumes/volume";
        savingSettings.saveVolumes = true;
        savingSettings.projectionsFilePath =
                "/Users/tischer/Documents/fiji-plugin-bigDataTools2/src/test/resources/test-saving-output/nc1-nt3-calibrated-tiff-projections/projection";

        Utils.saveImageAndWaitUntilDone( bdp, savingSettings, image );

        File file = new File(savingSettings.volumesFilePath );
        assertTrue( file.exists() );

    }

}
