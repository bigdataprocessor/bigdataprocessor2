package develop;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.open.core.NamingSchemes;
import org.junit.Test;

import static de.embl.cba.bdp2.open.core.NamingSchemes.LEICA_LIGHT_SHEET_TIFF;
import static de.embl.cba.bdp2.open.core.NamingSchemes.Z;

public class DevelopGenericSinglePlaneTiffLoading
{
    public static void main(String[] args)
    {
        new DevelopGenericSinglePlaneTiffLoading().open();
    }

    @Test
    public void open()
    {
        final String directory = "/Users/tischer/Downloads/data_as_sequence";

        final Image image = BigDataProcessor2.openImage(
                directory,
                ".*_T(" + NamingSchemes.T + "\\d+)__z(" + Z + "\\d+).*_c(" + NamingSchemes.C + "\\d+).*",
                ".*"
        );

        // BigDataProcessor2.showImage( image, true );
    }
}
