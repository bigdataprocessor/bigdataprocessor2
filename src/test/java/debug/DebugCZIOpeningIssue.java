package debug;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.BigDataProcessor2Command;
import de.embl.cba.bdp2.image.Image;
import net.imagej.ImageJ;

public class DebugCZIOpeningIssue
{
    public static void main( String[] args )
    {
        // Configure all services
        ImageJ imageJ = new ImageJ();
        imageJ.command().run( BigDataProcessor2Command.class, true );

        String filePath = "/Users/tischer/Desktop/bdp2/src_cropped_5x5x5.czi";
        int series = 0;
        Image< ? > image = BigDataProcessor2.openBioFormats( filePath, series );
        BigDataProcessor2.showImage( image );
    }
}
