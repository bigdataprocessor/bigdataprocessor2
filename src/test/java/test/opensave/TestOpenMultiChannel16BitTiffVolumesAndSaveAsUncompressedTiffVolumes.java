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

import static de.embl.cba.bdp2.open.NamingSchemes.MULTI_CHANNEL_VOLUMES;
import static de.embl.cba.bdp2.open.NamingSchemes.TIF;

public class TestOpenMultiChannel16BitTiffVolumesAndSaveAsUncompressedTiffVolumes
{
    public static void main(String[] args)
    {
        ImageJ imageJ = new ImageJ();
        Services.setContext( imageJ.getContext() );
        Services.setCommandService( imageJ.command() );
        //BigDataProcessor2UserInterface.showUI();
        new TestOpenMultiChannel16BitTiffVolumesAndSaveAsUncompressedTiffVolumes().run();
    }

    @Test
    public void run()
    {
        final String directory = "src/test/resources/test/tiff-nc2-nt2-16bit";
        final Image image = BigDataProcessor2.openTiffSeries( directory, MULTI_CHANNEL_VOLUMES + TIF );
        image.setVoxelSize( new double[]{1.0, 1.0, 1.0} );

        final SavingSettings settings = SavingSettings.getDefaults();
        settings.volumesFilePathStump = "src/test/resources/test/saved/tiff/" + image.getName();
        settings.image = image;
        settings.fileType = SaveFileType.TiffVolumes;
        settings.numProcessingThreads = 4;
        settings.numIOThreads = 1;
        settings.compression = SavingSettings.COMPRESSION_NONE;
        settings.tStart = 0;
        settings.tEnd = image.getNumTimePoints() - 1;

        Logger.setLevel( Logger.Level.Debug );
        BigDataProcessor2.saveImage( image, settings, new LoggingProgressListener( "Files saved" ) );
    }
}
