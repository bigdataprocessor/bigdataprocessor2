package headless;

import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.process.Binner;
import de.embl.cba.bdp2.process.ChannelShifter;
import de.embl.cba.bdp2.process.Cropper;
import de.embl.cba.bdp2.ui.BigDataProcessor2;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.bdp2.viewers.ImageViewer;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.view.Views;

import java.util.ArrayList;

/**
 * IMPORTANT NOTE: Adjust Max value to 255 in the Big Data Viewer. (Settings>Brightness and Color>Max)
 */

public class CorrectChromaticShiftAndBin
{
    public static void main(String[] args)
    {
        BigDataProcessor2 bdp = new BigDataProcessor2();

        String imageDirectory =
                CorrectChromaticShiftAndBin.class
                        .getResource( "/nc2-nt3-calibrated-tiff"  ).getFile();

        final Image image = bdp.openTiffData(
                imageDirectory,
                FileInfos.LOAD_CHANNELS_FROM_FOLDERS,
                ".*" );

        final ImageViewer imageViewer = bdp.showImage( image );

        final ChannelShifter shifter = new ChannelShifter<>( image.getRai() );

        final ArrayList< long[] > shifts = new ArrayList< >();
        shifts.add( new long[]{0,0,0,0});
        shifts.add( new long[]{30,0,0,0});

        final RandomAccessibleInterval shiftedRAI = shifter.getChannelShiftedRAI( shifts );

        Utils.showRaiKeepingAllSettings( shiftedRAI, imageViewer );


        final Image bin = Binner.bin( imageViewer.getImage(), new long[]{ 1, 1, 0, 0, 0 } );

        //imageViewer.show( bin, true );

        final RandomAccess randomAccess = bin.getRai().randomAccess();

//
//        final Image crop = Cropper.crop( imageViewer.getImage(), new FinalInterval(
//                new long[]{ 0, 0, 0, 0, 0 },
//                new long[]{ 40, 40, 40, 1, 2 }
//        ) );
//
//        Utils.showRaiKeepingAllSettings( crop.getRai(), imageViewer );

    }

}
