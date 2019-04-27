import de.embl.cba.bdp2.files.FileInfoConstants;
import de.embl.cba.bdp2.ui.BigDataProcessor2;
import de.embl.cba.bdp2.viewers.ViewerUtils;

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

        bigDataProcessor2.openFromDirectory(
                imageDirectory,
                FileInfoConstants.LOAD_CHANNELS_FROM_FOLDERS,
                ".*",
                true,
                ViewerUtils.getImageViewer( ViewerUtils.BIG_DATA_VIEWER ) );
    }

}
