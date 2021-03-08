package test.opensave;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.log.progress.LoggingProgressListener;
import de.embl.cba.bdp2.save.SaveFileType;
import de.embl.cba.bdp2.save.SavingSettings;
import org.junit.Test;
import test.Utils;

import static de.embl.cba.bdp2.open.NamingSchemes.MULTI_CHANNEL_VOLUMES;
import static de.embl.cba.bdp2.open.NamingSchemes.TIF;

public class TestOpenBioFormatsAndSaveAsBDVHDF5
{
    public static void main(String[] args)
    {
        Utils.prepareInteractiveMode();

        new TestOpenBioFormatsAndSaveAsBDVHDF5().run();
    }

    //@Test
    public void run()
    {
        final Image image = BigDataProcessor2.openBioFormats( "/Users/tischer/Downloads/StackExampleMarch2021/Brain39_cell1_DC-crop.xml", 0 );
//        image.setVoxelDimensions( new double[]{1.0, 1.0, 1.0} );

        BigDataProcessor2.showImage( image );

        final SavingSettings settings = SavingSettings.getDefaults();
        settings.volumesFilePathStump = "/Users/tischer/Downloads/StackExampleMarch2021/volumes/" + image.getName();
        settings.fileType = SaveFileType.BigDataViewerXMLHDF5;
        settings.numProcessingThreads = 4;
        settings.numIOThreads = 1;
        settings.compression = SavingSettings.COMPRESSION_NONE;
        settings.tStart = 0;
        settings.tEnd = image.getNumTimePoints() - 1;

        BigDataProcessor2.saveImage( image, settings, new LoggingProgressListener( "Progress" ) );
    }
}
