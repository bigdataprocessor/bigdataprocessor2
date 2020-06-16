package test.open;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.open.core.NamingScheme;

import static de.embl.cba.bdp2.open.core.NamingScheme.MULTI_CHANNEL_VOLUMES;
import static de.embl.cba.bdp2.open.core.NamingScheme.TIF;

public class TestOpenMultiChannelTiffVolumes
{
    public static void main(String[] args)
    {
        final String directory = "/Users/tischer/Documents/bigdataprocessor2/src/test/resources/test/tiff-nc2-nt6";

        final Image image = BigDataProcessor2.openImage(
                directory,
                MULTI_CHANNEL_VOLUMES + TIF,
                ".*"
        );

        BigDataProcessor2.showImage( image, true );
    }
}
