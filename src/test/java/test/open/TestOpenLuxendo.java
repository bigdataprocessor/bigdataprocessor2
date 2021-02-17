package test.open;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import test.Utils;

import static de.embl.cba.bdp2.open.NamingSchemes.LUXENDO;

public class TestOpenLuxendo
{
    public static void main(String[] args)
    {
        Utils.prepareInteractiveMode();

        new TestOpenLuxendo().run();
    }

    //@Test
    public void run()
    {
        String regExp = LUXENDO.replace( "STACK", "" + 6 );

        // /Volumes/cba/exchange/bigdataprocessor/data/mouse_2cam_publication_figure/stack_6_channel_2
        final Image image = BigDataProcessor2.openHDF5Series(
                "/Volumes/cba/exchange/bigdataprocessor/data/mouse_2cam_publication_figure",
                regExp,
                "Data" );

        BigDataProcessor2.showImage( image, true );
    }
}
