package test.open;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import test.Utils;

import static de.embl.cba.bdp2.open.NamingSchemes.*;

public class TestOpenLeicaDSL
{
    public static void main(String[] args)
    {
        Utils.prepareInteractiveMode();

        new TestOpenLeicaDSL().run();
    }

    //@Test
    public void run()
    {
        final String directory = "src/test/resources/test/leica-dsl-tiff-planes";

        final Image image = BigDataProcessor2.openTIFFSeries(
                directory,
                LEICA_DSL_TIFF_PLANES
        );

        image.setVoxelDimensions( 1, 1, 1 ); // necessary because voxel size in z is NaN for single plane Tiff
        image.setVoxelUnit( "micrometer" );
        BigDataProcessor2.showImage( image, true );
    }
}
