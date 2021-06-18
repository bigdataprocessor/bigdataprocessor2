package test.open;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import test.Utils;

import java.util.regex.Pattern;

import static de.embl.cba.bdp2.open.NamingSchemes.LUXENDO;

public class TestOpenTerraStitcher
{
    public static void main(String[] args)
    {
        //Utils.prepareInteractiveMode();

        new TestOpenTerraStitcher().run();
    }

    //@Test
    public void run()
    {
        String regExp = ".*\\/t(?<T>\\d+)\\/c(?<C>\\d+)\\/.*_(?<Z>\\d+).tif";
        final String directory = "/Volumes/cba/exchange/Benjamin";
        final Image image = BigDataProcessor2.openTIFFSeries(
                directory,
                regExp );

        BigDataProcessor2.showImage( image, true );
    }
}
