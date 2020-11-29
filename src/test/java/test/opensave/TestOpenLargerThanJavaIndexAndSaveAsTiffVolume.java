package test.opensave;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.log.progress.LoggingProgressListener;
import de.embl.cba.bdp2.save.SaveFileType;
import de.embl.cba.bdp2.save.SavingSettings;
import de.embl.cba.bdp2.scijava.Services;
import net.imagej.ImageJ;
import org.junit.Test;

import static de.embl.cba.bdp2.open.NamingSchemes.*;

public class TestOpenLargerThanJavaIndexAndSaveAsTiffVolume
{
    public static void main(String[] args)
    {
        ImageJ imageJ = new ImageJ();
        Services.setContext( imageJ.getContext() );
        Services.setCommandService( imageJ.command() );
        //BigDataProcessor2UserInterface.showUI();
        new TestOpenLargerThanJavaIndexAndSaveAsTiffVolume().run();
    }

    // @Test // This takes 10g of RAM and 3g of disk space
    public void run()
    {
        Logger.setLevel( Logger.Level.Debug );

        final String directory = "src/test/resources/test/tiff-nc1-nt1-java-index-issue";
        final Image image = BigDataProcessor2.openTiffSeries( directory, SINGLE_CHANNEL_VOLUMES + TIF );
        image.setVoxelSize( new double[]{1.0, 1.0, 1.0} );

        final SavingSettings settings = SavingSettings.getDefaults();
        settings.volumesFilePathStump = "src/test/resources/test/output/tiff/" + image.getName();
        settings.fileType = SaveFileType.TiffVolumes;
        settings.numProcessingThreads = 4;
        settings.numIOThreads = 1;
        settings.compression = SavingSettings.COMPRESSION_NONE;
        settings.tStart = 0;
        settings.tEnd = image.getNumTimePoints() - 1;

        BigDataProcessor2.saveImage( image, settings, new LoggingProgressListener( "Files saved" ) );
    }
}
