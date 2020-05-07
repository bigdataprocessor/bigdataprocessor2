package users.giulia;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.read.NamingScheme;
import net.imagej.ImageJ;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class OpenEMFromLocal
{

	public < R extends RealType< R > & NativeType< R > > void view()
	{
		new ImageJ().ui().showUI();

		final Image< R > image = BigDataProcessor2.openImage(
				"/Users/tischer/Documents/fiji-plugin-bigDataProcessor2/src/test/resources/test-data/em-2d-sift-align-01",
				NamingScheme.TIFF_SLICES,
				".*.tif" );

		BigDataProcessor2.showImage( image);

		System.out.println("Done.");
	}


	public static void main( String[] args )
	{
		new OpenEMFromLocal().view();
	}
}
