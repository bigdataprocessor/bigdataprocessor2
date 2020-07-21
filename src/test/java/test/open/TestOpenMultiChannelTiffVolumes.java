package test.open;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.ui.BigDataProcessor2UI;
import org.junit.Test;

import static de.embl.cba.bdp2.open.core.NamingSchemes.MULTI_CHANNEL_VOLUMES;
import static de.embl.cba.bdp2.open.core.NamingSchemes.TIF;

public class TestOpenMultiChannelTiffVolumes
{
    public static void main(String[] args)
    {
        new TestOpenMultiChannelTiffVolumes().open();
    }

    @Test
    public void open()
    {
        final String directory = "/Users/tischer/Documents/bigdataprocessor2/src/test/resources/test/tiff-nc2-nt6";

        final Image image = BigDataProcessor2.openImage(
                directory,
                MULTI_CHANNEL_VOLUMES + TIF,
                ".*"
        );

        BigDataProcessor2UI.showUI();
        BigDataProcessor2.showImage( image, true );
    }
}
