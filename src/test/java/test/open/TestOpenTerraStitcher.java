package test.open;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import org.junit.Test;

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
        image.setVoxelUnit( "micrometer" );
        image.setVoxelDimensions( new double[]{1,1,1} );
        //BigDataProcessor2.showImage( image, true );
    }
}
