package test.open;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import org.junit.Test;

import static de.embl.cba.bdp2.open.core.NamingSchemes.*;

public class TestOpenLeicaDSL
{
    public static void main(String[] args)
    {
        new TestOpenLeicaDSL().run();
    }

    //@Test
    public void run()
    {
        final String directory = "/Users/tischer/Documents/bigdataprocessor2/src/test/resources/test/leica-dsl-tiff-planes";

        final Image image = BigDataProcessor2.openImage(
                directory,
                LEICA_DSL_TIFF_PLANES_REG_EXP,
                ".*"
        );

        double[] voxelSize = image.getVoxelSize();
        image.setVoxelSize( voxelSize[ 0 ], voxelSize[ 1 ], 0.00001 ); // necessary because voxel size in z is NaN for single plane Tiff

        BigDataProcessor2.showImage( image, true );
    }
}
