package test;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.open.core.NamingSchemes;
import loci.common.DebugTools;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class TestOpenSingleStripZipTiffPlanes
{
	// TODO: optimise speed
	public static boolean showImages = false;

	//@Test
	public < R extends RealType< R > & NativeType< R > > void open()
	{
		DebugTools.setRootLevel("OFF"); // Bio-Formats

		final Image< R > image = BigDataProcessor2.openImage(
				"/Users/tischer/Documents/fiji-plugin-bigDataProcessor2/src/test/resources/test-data/em-slices-zip-strips",
				NamingSchemes.TIFF_SLICES,
				".*.tif" );

		if ( showImages ) BigDataProcessor2.showImage( image);

		System.out.println("Done.");
	}

	public static void main( String[] args )
	{
		//showImages = true;
		new TestOpenSingleStripZipTiffPlanes().open();
	}
}
