package users.gustavo;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.load.files.FileInfos;
import de.embl.cba.bdp2.BigDataProcessor2;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class TestTiffLoading
{
	public static  < R extends RealType< R > & NativeType< R > > void main( String[] args )
	{
		final Image< R > image = BigDataProcessor2.openImage(
				"/Users/tischer/Documents/gustavo/bdp2-errors/error001",
				FileInfos.SINGLE_CHANNEL_TIMELAPSE,
				".*.tif"
		);

		BigDataProcessor2.showImage( image, true );

//		final ImagePlus imagePlus = IJ.openImage( "/Users/tischer/Desktop/gustavo/P12_Ch1-registered-T0006.tif" );

	}
}
