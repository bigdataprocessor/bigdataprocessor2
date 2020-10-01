package test.open;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.open.core.NamingSchemes;
import org.junit.Test;

import static de.embl.cba.bdp2.open.core.NamingSchemes.Z;

public class TestOpenTiffPlanesNoTime
{
    public static void main(String[] args)
    {
        new TestOpenTiffPlanesNoTime().run();
    }

    @Test
    public void run()
    {
        final String directory = "/Users/tischer/Documents/bigdataprocessor2/src/test/resources/test/tiff-planes-no-time";

        final Image image = BigDataProcessor2.openImage(
                directory,
                ".*_z(" + Z + "\\d+).*_c(" + NamingSchemes.C + "\\d+).*",
                ".*"
        );

        double[] voxelSize = image.getVoxelSize();
        image.setVoxelSize( voxelSize[ 0 ], voxelSize[ 1 ], 1.0 ); // necessary because voxel size in z is NaN for single plane Tiff

        BigDataProcessor2.showImage( image, true );
    }
}
