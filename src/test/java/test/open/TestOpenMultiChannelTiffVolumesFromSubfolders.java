package test.open;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import org.junit.Test;

import static de.embl.cba.bdp2.open.core.NamingScheme.MULTI_CHANNEL_VOLUMES_FROM_SUBFOLDERS;
import static de.embl.cba.bdp2.open.core.NamingScheme.TIF;

public class TestOpenMultiChannelTiffVolumesFromSubfolders
{
    public static void main(String[] args)
    {
        new TestOpenMultiChannelTiffVolumesFromSubfolders().open();
    }

    @Test
    public void open()
    {
        final String directory = "/Users/tischer/Documents/bigdataprocessor2/src/test/resources/test/tiff-nc2-nt6-subfolders";

        final Image image = BigDataProcessor2.openImage(
                directory,
                MULTI_CHANNEL_VOLUMES_FROM_SUBFOLDERS + TIF,
                ".*"
        );

        // BigDataProcessor2.showImage( image, true );
    }
}
