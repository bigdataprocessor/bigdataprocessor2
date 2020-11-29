package test.open;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.scijava.Services;
import net.imagej.ImageJ;
import org.junit.Test;

import static de.embl.cba.bdp2.open.NamingSchemes.MULTI_CHANNEL_VOLUMES;
import static de.embl.cba.bdp2.open.NamingSchemes.TIF;

public class TestOpenMultiChannel8BitTiffVolumes
{
    public static void main(String[] args)
    {
        ImageJ imageJ = new ImageJ();
        Services.setContext( imageJ.getContext() );
        new TestOpenMultiChannel8BitTiffVolumes().run();
    }

    @Test
    public void run()
    {
        final String directory = "src/test/resources/test/tiff-nc2-nt6";

        final Image image = BigDataProcessor2.openTiffSeries( directory, MULTI_CHANNEL_VOLUMES + TIF );

        image.setVoxelDimension( new double[]{1.0, 1.0, 1.0} );

//        BigDataProcessor2UserInterface.showUI();
//        BigDataProcessor2.showImage( image, true );
    }
}
