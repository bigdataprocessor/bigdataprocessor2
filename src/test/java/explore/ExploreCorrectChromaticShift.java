package explore;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.read.NamingScheme;
import loci.common.DebugTools;

import static junit.framework.TestCase.assertTrue;


/**
 * IMPORTANT NOTE: Adjust Max value to 255 in the Big Data Viewer. (Settings>Brightness and Color>Max)
 */

public class ExploreCorrectChromaticShift
{
    //@Test
    public void test()
    {
        DebugTools.setRootLevel("OFF"); // Bio-Formats

        BigDataProcessor2 bdp = new BigDataProcessor2();

        String imageDirectory =
                ExploreCorrectChromaticShift.class
                        .getResource( "/nc2-nt3-calibrated-tiff" ).getFile();

        final Image image = bdp.openImage(
                imageDirectory,
                NamingScheme.LOAD_CHANNELS_FROM_FOLDERS,
                ".*" );

        BigDataProcessor2.showImage( image);
    }

    public static void main( String[] args )
    {
        new ExploreCorrectChromaticShift().test();
    }

}
