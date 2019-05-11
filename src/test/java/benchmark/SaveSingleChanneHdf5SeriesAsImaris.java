package benchmark;

import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.saving.SavingSettings;
import de.embl.cba.bdp2.ui.BigDataProcessor2;
import de.embl.cba.bdp2.utils.Utils;
import java.io.File;

public class SaveSingleChanneHdf5SeriesAsImaris
{

    public static void main(String[] args)
    {
        final BigDataProcessor2 bdp = new BigDataProcessor2();

        final String directory = "/Users/tischer/Documents/isabell-schneider-splitchipmerge/stack_0_channel_0";

        final int numIOThreads = 4; // TODO

        final Image image = bdp.openHdf5Data(
                directory,
                FileInfos.SINGLE_CHANNEL_TIMELAPSE,
                ".*.h5",
                "Data" );

        image.setVoxelUnit( "micrometer" );
        image.setVoxelSpacing( 0.13, 0.13, 1.04 );

        //bdp.showImage( image );

        final File out = new File( "/Users/tischer/Desktop/stack_0_channel_0-asImaris-bdp2/im");

        // TODO: change the chunking of the highest resolution!

        final SavingSettings savingSettings = SavingSettings.getDefaults();
        savingSettings.fileType = SavingSettings.FileType.IMARIS_STACKS;
        savingSettings.nThreads = 1;
        savingSettings.isotropicProjectionResampling = true;
        savingSettings.isotropicProjectionVoxelSize = 0.5;
        savingSettings.saveProjections = false;
        savingSettings.saveVolumes = true;
        savingSettings.volumesFilePath = out.toString();

        Utils.saveImageAndWaitUntilDone( bdp, savingSettings, image );

    }

}
