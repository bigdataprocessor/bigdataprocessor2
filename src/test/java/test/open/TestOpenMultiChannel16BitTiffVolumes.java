package test.open;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.BigDataProcessor2UserInterface;
import org.junit.Test;

import static de.embl.cba.bdp2.open.NamingSchemes.MULTI_CHANNEL_VOLUMES;
import static de.embl.cba.bdp2.open.NamingSchemes.TIF;

public class TestOpenMultiChannel16BitTiffVolumes
{
    public static void main(String[] args)
    {
        new TestOpenMultiChannel16BitTiffVolumes().run();
    }

    @Test
    public void run()
    {
        final String directory = "/Users/tischer/Documents/bigdataprocessor2/src/test/resources/test/tiff-nc2-nt2-16bit";

        final Image image = BigDataProcessor2.openImage(
                directory,
                MULTI_CHANNEL_VOLUMES + TIF,
                ".*"
        );

        image.setVoxelSize( new double[]{1.0, 1.0, 1.0} );

        BigDataProcessor2UserInterface.showUI();
        BigDataProcessor2.showImage( image, true );
    }
}
