package test.open;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import org.junit.Test;
import test.Utils;

import static de.embl.cba.bdp2.open.NamingSchemes.*;

public class TestOpenMultiChannelZLIBCompressedTiffVolumes
{
    public static void main(String[] args)
    {
        Utils.prepareInteractiveMode();

        new TestOpenMultiChannelZLIBCompressedTiffVolumes().run();
    }

    @Test
    public void run()
    {
        final String directory = "src/test/resources/test/tiff-nc2-nt6-zlib";

        final Image image = BigDataProcessor2.openTIFFSeries( directory, MULTI_CHANNEL_VOLUMES + OME_TIF );

        image.setVoxelDimensions( 1, 1, 1 );
        BigDataProcessor2.showImage( image );
    }
}
