package test.open;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import ome.units.unit.Unit;

import static de.embl.cba.bdp2.open.NamingSchemes.*;

public class TestOpenLeicaDSL
{
    public static void main(String[] args)
    {
        new TestOpenLeicaDSL().run();
    }

    //@Test
    public void run()
    {
        final String directory = "src/test/resources/test/leica-dsl-tiff-planes";

        final Image image = BigDataProcessor2.openTiffSeries(
                directory,
                LEICA_DSL_TIFF_PLANES
        );

        double[] voxelSize = image.getVoxelSize();
        image.setVoxelSize( voxelSize[ 0 ], voxelSize[ 1 ], 0.00001 ); // necessary because voxel size in z is NaN for single plane Tiff

        Unit voxelUnit = image.getVoxelUnit();

        BigDataProcessor2.showImage( image, true );
    }
}
