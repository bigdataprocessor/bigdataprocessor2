package tests;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.load.files.FileInfos;
import de.embl.cba.bdp2.bin.Binner;
import de.embl.cba.bdp2.register.ChannelShifter;
import de.embl.cba.bdp2.BigDataProcessor2;
import loci.common.DebugTools;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import org.junit.Test;

import java.util.ArrayList;

import static junit.framework.TestCase.assertTrue;


/**
 * IMPORTANT NOTE: Adjust Max value to 255 in the Big Data Viewer. (Settings>Brightness and Color>Max)
 */

public class TestCorrectChromaticShiftAndBin
{
    @Test
    public void test()
    {
        DebugTools.setRootLevel("OFF"); // Bio-Formats

        BigDataProcessor2 bdp = new BigDataProcessor2();

        String imageDirectory =
                TestCorrectChromaticShiftAndBin.class
                        .getResource( "/nc2-nt3-calibrated-tiff" ).getFile();

        final Image image = bdp.openImage(
                imageDirectory,
                FileInfos.LOAD_CHANNELS_FROM_FOLDERS,
                ".*" );

        // final BdvImageViewer imageViewer = bdp.showImage( image );

        final ChannelShifter shifter = new ChannelShifter<>( image.getRai() );

        final ArrayList< long[] > shifts = new ArrayList<>();
        shifts.add( new long[]{ 0, 0, 0, 0 } );
        shifts.add( new long[]{ 30, 0, 0, 0 } );

        final RandomAccessibleInterval shiftedRAI = shifter.getShiftedRai( shifts );

        final Image shifted = image.newImage( shiftedRAI );

        // imageViewer.replaceImage( image.newImage( shiftedRAI ) );

        final Image bin = Binner.bin( shifted, new long[]{ 3, 3, 1, 1, 1 } );

        // imageViewer.replaceImage( bin );

        final RandomAccess randomAccess = bin.getRai().randomAccess();

        // bdp.showImage( bin );

        assertTrue( randomAccess != null );

//
//        final Image crop = Cropper.crop( imageViewer.getImage(), new FinalInterval(
//                new long[]{ 0, 0, 0, 0, 0 },
//                new long[]{ 40, 40, 40, 1, 2 }
//        ) );
//
//        Utils.showRaiKeepingAllSettings( crop.getRai(), imageViewer );

    }

    public static void main( String[] args )
    {
        new TestCorrectChromaticShiftAndBin().test();
    }

}
