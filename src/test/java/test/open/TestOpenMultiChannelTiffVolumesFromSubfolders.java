package test.open;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.read.NamingScheme;

public class TestOpenMultiChannelTiffVolumesFromSubfolders
{
    public static void main(String[] args)
    {
        final String directory = "/Users/tischer/Documents/bigdataprocessor2/src/test/resources/test/tiff-nc2-nt3-subfolders";

        final Image image = BigDataProcessor2.openImage(
                directory,
                NamingScheme.MULTI_CHANNEL_TIFF_VOLUMES_FROM_SUBFOLDERS,
                ".*"
        );

        BigDataProcessor2.showImage( image, true );
    }

}
