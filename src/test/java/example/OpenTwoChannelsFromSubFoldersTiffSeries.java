package example;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.load.files.FileInfos;
import de.embl.cba.bdp2.BigDataProcessor2;

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
                FileInfos.LOAD_CHANNELS_FROM_FOLDERS,
                ".*" );

        bdp.showImage( image );
    }

}
