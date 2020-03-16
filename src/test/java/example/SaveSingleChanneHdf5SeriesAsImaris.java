package example;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.load.files.FileInfos;
import de.embl.cba.bdp2.bin.Binner;
import de.embl.cba.bdp2.save.SavingSettings;
import de.embl.cba.bdp2.ui.BigDataProcessor2;

public class SaveSingleChanneHdf5SeriesAsImaris
{
    public static void main(String[] args)
    {
        final BigDataProcessor2 bdp = new BigDataProcessor2();

        final String directory =
                "/Users/tischer/Documents/isabell-schneider-splitchipmerge/stack_0_channel_0";

        final String loadingScheme = FileInfos.SINGLE_CHANNEL_TIMELAPSE;
        final String filterPattern = ".*.h5";
        final String dataset = "Data";

        final Image image = bdp.openHdf5Image(
                directory,
                loadingScheme,
                filterPattern,
                dataset );

        image.setVoxelUnit( "micrometer" );
        image.setVoxelSpacing( 0.13, 0.13, 1.04 );

        bdp.showImage( image );

        final Image binnedImage = Binner.bin( image, new long[]{ 3, 3, 3, 0, 0 } );
        //   bdp.showImage( bin );

        final SavingSettings savingSettings = SavingSettings.getDefaults();
        savingSettings.fileType = SavingSettings.FileType.IMARIS_STACKS;
        savingSettings.numIOThreads = 1;
        savingSettings.numProcessingThreads = 4;
        savingSettings.saveVolumes = true;
        savingSettings.volumesFilePathStump = "/Users/tischer/Desktop/stack_0_channel_0-imaris-volumes/volume";
        savingSettings.saveProjections = true;
        savingSettings.projectionsFilePathStump = "/Users/tischer/Desktop/stack_0_channel_0-imaris-projections/projection";



        BigDataProcessor2.saveImageAndWaitUntilDone( savingSettings, binnedImage );

    }

}
