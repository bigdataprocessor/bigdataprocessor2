package test.open;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;

import static de.embl.cba.bdp2.open.core.NamingSchemes.LEICA_DSL_TIFF_PLANES_REG_EXP;
import static de.embl.cba.bdp2.open.core.NamingSchemes.LUXENDO_REGEXP;

public class TestOpenLuxendo
{
    public static void main(String[] args)
    {
        new TestOpenLuxendo().run();
    }

    //@Test
    public void run()
    {
        String regExp = LUXENDO_REGEXP.replace( "STACK", "" + 6 );

        // /Volumes/cba/exchange/bigdataprocessor/data/mouse_2cam_publication_figure/stack_6_channel_2
        final Image image = BigDataProcessor2.openImageFromHdf5(
                "/Volumes/cba/exchange/bigdataprocessor/data/mouse_2cam_publication_figure",
                regExp,
                regExp,
                "Data" );

        BigDataProcessor2.showImage( image, true );
    }
}
