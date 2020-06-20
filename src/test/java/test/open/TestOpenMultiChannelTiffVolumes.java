package test.open;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.open.core.NamingScheme;
import org.junit.Test;

import static de.embl.cba.bdp2.open.core.NamingScheme.MULTI_CHANNEL_VOLUMES;
import static de.embl.cba.bdp2.open.core.NamingScheme.TIF;

public class TestOpenMultiChannelTiffVolumes
{
    public static void main(String[] args)
    {
        final Image image = open();

        BigDataProcessor2.showImage( image, true );
    }

    @Test
    public static Image open()
    {
        final String directory = "/Users/tischer/Documents/bigdataprocessor2/src/test/resources/test/tiff-nc2-nt6";

        return BigDataProcessor2.openImage(
                directory,
                MULTI_CHANNEL_VOLUMES + TIF,
                ".*"
        );
    }
}
