package tests;

import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.ui.BigDataProcessor2;
import net.imagej.ImageJ;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.junit.Test;

public class TestOpenSinglePlaneSingleStripTiff
{
	// TODO: optimise speed

	@Test
	public < R extends RealType< R > & NativeType< R > > void open()
	{
		final BigDataProcessor2< R > bdp = new BigDataProcessor2<>();

		final Image< R > image = bdp.openImage(
				"/Users/tischer/Documents/fiji-plugin-bigDataTools2/src/test/resources/test-data/em-slices-zip-strips",
				FileInfos.TIFF_SLICES,
				".*.tif" );

		bdp.showImage( image );

		System.out.println("Done.");
	}


	public static void main( String[] args )
	{
		new TestOpenSinglePlaneSingleStripTiff().open();
	}
}
