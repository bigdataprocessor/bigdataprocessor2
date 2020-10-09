package example;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.open.NamingSchemes;

public class OpenTwoChannelsFromSubFoldersTiffSeries
{
    public static void main( String[] args )
    {
        BigDataProcessor2 bdp = new BigDataProcessor2();

        String imageDirectory =
                OpenTwoChannelsFromSubFoldersTiffSeries.class
                        .getResource( "/nc2-nt3-calibrated-tiff"  ).getFile();

        final Image image = bdp.openImage(
                imageDirectory,
                NamingSchemes.LOAD_CHANNELS_FROM_FOLDERS,
                ".*" );

        bdp.showImage( image);
    }

}
