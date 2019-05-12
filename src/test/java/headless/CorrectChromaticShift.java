package headless;

import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.process.ChannelShiftCorrectionDialog;
import de.embl.cba.bdp2.ui.BigDataProcessor2;
import de.embl.cba.bdp2.viewers.ImageViewer;

/**
 * IMPORTANT NOTE: Adjust Max value to 255 in the Big Data Viewer. (Settings>Brightness and Color>Max)
 */

public class CorrectChromaticShift
{
    public static void main(String[] args)
    {
        BigDataProcessor2 bdp = new BigDataProcessor2();

        String imageDirectory =
                CorrectChromaticShift.class
                        .getResource( "/nc2-nt3-calibrated-tiff"  ).getFile();

        final Image image = bdp.openTiffImage(
                imageDirectory,
                FileInfos.LOAD_CHANNELS_FROM_FOLDERS,
                ".*" );

        final ImageViewer imageViewer = bdp.showImage( image );

        new ChannelShiftCorrectionDialog<>( imageViewer );

    }

}
