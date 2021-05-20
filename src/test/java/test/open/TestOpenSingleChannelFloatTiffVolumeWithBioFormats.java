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
        String file = "src/test/resources/test/tiff-nc1-nt1-ImageJFloat/mri-stack-ij-float.tif";
        file = "/Volumes/cba/exchange/Shuting/Ecad_Sqh_100x_20210511_01_decon.ics";

        image = BigDataProcessor2.openBioFormats( file, 0 );
    }
}
