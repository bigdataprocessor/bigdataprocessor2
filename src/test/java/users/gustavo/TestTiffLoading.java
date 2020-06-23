package users.gustavo;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.open.core.NamingSchemes;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import static de.embl.cba.bdp2.open.core.NamingSchemes.TIF;

public class TestTiffLoading
{
	public static  < R extends RealType< R > & NativeType< R > > void main( String[] args )
	{
		final Image< R > image = BigDataProcessor2.openImage(
				"/Users/tischer/Documents/gustavo/bdp2-errors/error001",
				NamingSchemes.SINGLE_CHANNEL_VOLUMES + TIF,
				".*"
		);

		BigDataProcessor2.showImage( image, true );


//		final ImagePlus imagePlus = IJ.openImage( "/Users/tischer/Desktop/gustavo/P12_Ch1-registered-T0006.tif" );

	}
}
