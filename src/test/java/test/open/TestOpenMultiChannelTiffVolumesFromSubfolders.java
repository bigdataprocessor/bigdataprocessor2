package test.open;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import org.junit.Test;

import static de.embl.cba.bdp2.open.NamingSchemes.MULTI_CHANNEL_VOLUMES_FROM_SUBFOLDERS;
import static de.embl.cba.bdp2.open.NamingSchemes.TIF;

public class TestOpenMultiChannelTiffVolumesFromSubfolders
{
    public static void main(String[] args)
    {
        new TestOpenMultiChannelTiffVolumesFromSubfolders().run();
    }

    @Test
    public void run()
    {
        final String directory = "src/test/resources/test/tiff-nc2-nt6-subfolders";

        final Image image = BigDataProcessor2.openTIFFSeries( directory, MULTI_CHANNEL_VOLUMES_FROM_SUBFOLDERS + TIF );
    }
}
