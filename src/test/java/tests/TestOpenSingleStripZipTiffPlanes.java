package tests;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.load.files.FileInfos;
import de.embl.cba.bdp2.ui.BigDataProcessor2;
import loci.common.DebugTools;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.junit.Test;

public class TestOpenSingleStripZipTiffPlanes
{
	// TODO: optimise speed
	public static boolean showImages = false;

	@Test
	public < R extends RealType< R > & NativeType< R > > void open()
	{
		DebugTools.setRootLevel("OFF"); // Bio-Formats

		final BigDataProcessor2< R > bdp = new BigDataProcessor2<>();

		final Image< R > image = bdp.openImage(
				"/Users/tischer/Documents/fiji-plugin-bigDataProcessor2/src/test/resources/test-data/em-slices-zip-strips",
				FileInfos.TIFF_SLICES,
				".*.tif" );

		if ( showImages ) bdp.showImage( image );

		System.out.println("Done.");
	}


	public static void main( String[] args )
	{
		//showImages = true;
		new TestOpenSingleStripZipTiffPlanes().open();
	}
}
