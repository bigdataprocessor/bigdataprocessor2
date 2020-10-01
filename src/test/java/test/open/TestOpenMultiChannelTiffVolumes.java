package test.open;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import org.junit.Test;

import static de.embl.cba.bdp2.open.core.NamingSchemes.MULTI_CHANNEL_VOLUMES;
import static de.embl.cba.bdp2.open.core.NamingSchemes.TIF;

public class TestOpenMultiChannelTiffVolumes
{
    public static void main(String[] args)
    {
        new TestOpenMultiChannelTiffVolumes().run();
    }

    @Test
    public void run()
    {
        final String directory = "/Users/tischer/Documents/bigdataprocessor2/src/test/resources/test/tiff-nc2-nt6";

        final Image image = BigDataProcessor2.openImage(
                directory,
                MULTI_CHANNEL_VOLUMES + TIF,
                ".*"
        );

        image.setVoxelSize( new double[]{1.0, 1.0, 1.0} );
//        BigDataProcessor2UI.showUI();
        BigDataProcessor2.showImage( image, true );
    }
}
