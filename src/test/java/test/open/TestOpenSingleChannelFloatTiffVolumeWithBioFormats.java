package test.open;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import org.junit.Test;
import test.Utils;

public class TestOpenSingleChannelFloatTiffVolumeWithBioFormats
{
    private static Image image;

    public static void main( String[] args)
    {
        Utils.prepareInteractiveMode();

        new TestOpenSingleChannelFloatTiffVolumeWithBioFormats().run();

        image.setVoxelDimensions( 1, 1, 20 );
        BigDataProcessor2.showImage( image );
    }

    @Test
    public void run()
    {
        final String directory = "src/test/resources/test/tiff-nc1-nt1-ImageJFloat/mri-stack-ij-float.tif";

        image = BigDataProcessor2.openBioFormats( directory, 0 );
    }
}
