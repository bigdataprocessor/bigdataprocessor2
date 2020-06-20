package test.open;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;

import static de.embl.cba.bdp2.open.core.NamingScheme.*;

public class TestOpenLeicaDSL
{
    public static void main(String[] args)
    {
        final String directory = "/Users/tischer/Documents/bigdataprocessor2/src/test/resources/test/leica-dsl-tiff-planes";

        final Image image = BigDataProcessor2.openImage(
                directory,
                LEICA_LIGHT_SHEET_TIFF,
                ".*"
        );

        BigDataProcessor2.showImage( image, true );
    }
}
