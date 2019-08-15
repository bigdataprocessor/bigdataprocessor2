package tests;

import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.progress.LoggingProgressListener;
import de.embl.cba.bdp2.registration.RegisteredViews;
import de.embl.cba.bdp2.ui.BigDataProcessor2;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import loci.common.DebugTools;
import net.imagej.ImageJ;
import net.imglib2.FinalInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.junit.Test;

public class TestSIFTMovieAlignment< R extends RealType< R > & NativeType< R > >
{
	public static boolean showImages = false;

	@Test
	public void lazySIFT()
	{
		DebugTools.setRootLevel("OFF"); // Bio-Formats

		final Image< R > image = BigDataProcessor2.openImage(
				"/Users/tischer/Documents/fiji-plugin-bigDataTools2/src/test/resources/test-data/sift-align-movie",
				FileInfos.SINGLE_CHANNEL_TIMELAPSE,
				".*");

		if ( showImages )
			BigDataProcessor2.showImage( image, true );

		final FinalInterval hyperSliceInterval = FinalInterval.createMinMax( 0, 0, image.getRai().dimension( 2 ) / 2,
				image.getRai().max( 0 ), image.getRai().max( 1 ), image.getRai().dimension( 2 ) / 2 );

		final Image< R > alignedImage =
				RegisteredViews.siftAlignMovie(
						image,
						3,
						hyperSliceInterval,
						true,
						new LoggingProgressListener( "SIFT" ) );

		if ( showImages )
			BigDataProcessor2.showImage( alignedImage, false ).setDisplayRange( 100, 200, 0 );
	}

	public static void main( String[] args )
	{
		showImages = true;
		new ImageJ().ui().showUI();
		new TestSIFTMovieAlignment().lazySIFT();
	}

}
