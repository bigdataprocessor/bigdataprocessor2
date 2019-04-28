import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.ui.BigDataProcessor2;
import de.embl.cba.bdp2.viewers.ViewerUtils;

/**
 * IMPORTANT NOTE: Adjust Max value to 255 in the Big Data Viewer. (Settings>Brightness and Color>Max)
 */

public class OpenSingleChannelTiffSeries
{

    public static void main(String[] args)
    {
        BigDataProcessor2 bigDataProcessor2 = new BigDataProcessor2();

        String imageDirectory =
                OpenSingleChannelTiffSeries.class
                        .getResource( "/nc1-nt3-calibrated-tiff"  ).getFile();

        bigDataProcessor2.openFromDirectory(
                imageDirectory,
                FileInfos.SINGLE_CHANNEL_TIMELAPSE,
                ".*",
                null,
                true,
                ViewerUtils.getImageViewer( ViewerUtils.BIG_DATA_VIEWER ) );
    }

}
