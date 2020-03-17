package users.giulia;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.load.files.FileInfos;
import de.embl.cba.bdp2.BigDataProcessor2;
import net.imagej.ImageJ;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class OpenEMFromLocal
{

	public < R extends RealType< R > & NativeType< R > > void view()
	{
		new ImageJ().ui().showUI();

		final BigDataProcessor2< R > bdp = new BigDataProcessor2<>();

		final Image< R > image = bdp.openImage(
				"/Users/tischer/Documents/fiji-plugin-bigDataProcessor2/src/test/resources/test-data/em-2d-sift-align-01",
				FileInfos.TIFF_SLICES,
				".*.tif" );

		bdp.showImage( image );

		System.out.println("Done.");
	}


	public static void main( String[] args )
	{
		new OpenEMFromLocal().view();
	}
}
