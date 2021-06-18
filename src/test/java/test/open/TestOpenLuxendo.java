package test.open;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.open.NamingSchemes;
import test.Utils;

import java.util.regex.Pattern;

import static de.embl.cba.bdp2.open.NamingSchemes.HDF5;
import static de.embl.cba.bdp2.open.NamingSchemes.LUXENDO;
import static de.embl.cba.bdp2.open.NamingSchemes.T;

public class TestOpenLuxendo
{
    public static void main(String[] args)
    {
        //Utils.prepareInteractiveMode();

        new TestOpenLuxendo().run();
    }

    //@Test
    public void run()
    {
//        String regExp = ".*stack_6_(?<C1>channel_.*)\\/(?<C2>Cam_.*)_(" + T + "\\d+)(?:.lux)" + HDF5;
        //String regExp = ".*stack_6_(?<C1>channel_.*)\\/(?<C2>Cam_.*)_(" + T + "\\d+)(?:.lux).h5";
        String regExp = LUXENDO.replace( NamingSchemes.P, "6" );
        final String s = "/Volumes/cba/exchange/bigdataprocessor/data/mouse_2cam_publication/stack_6_channel_2/Cam_Short_00136.h5";

        Pattern pattern = Pattern.compile( regExp );
        final boolean matches = pattern.matcher( s ).matches();

        // /Volumes/cba/exchange/bigdataprocessor/data/mouse_2cam_publication_figure/stack_6_channel_2
        final Image image = BigDataProcessor2.openHDF5Series(
                "/Volumes/cba/exchange/bigdataprocessor/data/mouse_2cam_publication",
                regExp,
                "Data" );

        BigDataProcessor2.showImage( image, true );
    }
}
