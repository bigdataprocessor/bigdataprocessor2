package test.open;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import org.junit.Test;
import test.Utils;

import static de.embl.cba.bdp2.open.NamingSchemes.MULTI_CHANNEL_VOLUMES;
import static de.embl.cba.bdp2.open.NamingSchemes.TIF;

public class TestOpenMultiChannel8BitTiffVolumes
{
    private static Image image;

    public static void main( String[] args)
    {
        Utils.prepareInteractiveMode();

        new TestOpenMultiChannel8BitTiffVolumes().run();
        BigDataProcessor2.showImage( image, true, true );
    }

    @Test
    public void run()
    {
        final String directory = "src/test/resources/test/tiff-nc2-nt6";

        image = BigDataProcessor2.openTIFFSeries( directory, MULTI_CHANNEL_VOLUMES + TIF );

        image.setVoxelDimensions( new double[]{1.0, 1.0, 1.0} );
    }
}
