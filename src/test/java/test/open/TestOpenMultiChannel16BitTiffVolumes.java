package test.open;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.BigDataProcessor2UI;
import de.embl.cba.bdp2.scijava.Services;
import net.imagej.ImageJ;
import org.junit.Test;

import static de.embl.cba.bdp2.open.NamingSchemes.MULTI_CHANNEL_VOLUMES;
import static de.embl.cba.bdp2.open.NamingSchemes.TIF;

public class TestOpenMultiChannel16BitTiffVolumes
{
    public static void main(String[] args)
    {
        ImageJ imageJ = new ImageJ();
        Services.setContext( imageJ.getContext() );
        Services.setCommandService( imageJ.command() );
        BigDataProcessor2UI.showUI();
        new TestOpenMultiChannel16BitTiffVolumes().run();
    }

    @Test
    public void run()
    {
        final String directory = "src/test/resources/test/tiff-nc2-nt2-16bit";

        final Image image = BigDataProcessor2.openTiffSeries( directory, MULTI_CHANNEL_VOLUMES + TIF );

        image.setVoxelDimensions( new double[]{1.0, 1.0, 1.0} );

        BigDataProcessor2.showImage( image, true );
    }
}
