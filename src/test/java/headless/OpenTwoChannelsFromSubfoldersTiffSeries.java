package headless;

import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.ui.BigDataProcessor2;

/**
 * IMPORTANT NOTE: Adjust Max value to 255 in the Big Data Viewer. (Settings>Brightness and Color>Max)
 */

public class OpenTwoChannelsFromSubfoldersTiffSeries
{
    public static void main(String[] args)
    {
        BigDataProcessor2 bigDataProcessor2 = new BigDataProcessor2();

        String imageDirectory =
                OpenTwoChannelsFromSubfoldersTiffSeries.class
                        .getResource( "/nc2-nt3-calibrated-tiff"  ).getFile();

        final Image image = bigDataProcessor2.openTiffData(
                imageDirectory,
                FileInfos.LOAD_CHANNELS_FROM_FOLDERS,
                ".*" );

        bigDataProcessor2.showImage( image );

    }

}
