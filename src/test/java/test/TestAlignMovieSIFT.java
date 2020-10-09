package test;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.log.progress.LoggingProgressListener;
import de.embl.cba.bdp2.open.NamingSchemes;
import de.embl.cba.bdp2.devel.register.RegisteredViews;
import de.embl.cba.bdp2.devel.register.Registration;
import de.embl.cba.bdp2.BigDataProcessor2;
import loci.common.DebugTools;
import net.imagej.ImageJ;
import net.imglib2.FinalInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class TestAlignMovieSIFT< R extends RealType< R > & NativeType< R > >
{
	public static boolean showImages = false;

	//@Test
	public void lazySIFT()
	{
		DebugTools.setRootLevel("OFF"); // Bio-Formats

		final Image< R > image = BigDataProcessor2.openTiffSeries(
				"/Users/tischer/Documents/fiji-plugin-bigDataProcessor2/src/test/resources/test-data/gustavo-drift",
				NamingSchemes.LOAD_CHANNELS_FROM_FOLDERS,
				".*");

		if ( showImages )
			BigDataProcessor2.showImage( image, true );

		final FinalInterval hyperSliceInterval = FinalInterval.createMinMax(
				0, 0, image.getRai().dimension( 2 ) / 2, 0,
				image.getRai().max( 0 ), image.getRai().max( 1 ), image.getRai().dimension( 2 ) / 2, 0 );

		final Image< R > alignedImage =
				RegisteredViews.alignMovie(
						image,
						3,
						hyperSliceInterval,
						true,
						new LoggingProgressListener( "SIFT" ),
						Registration.SIFT_CORRESPONDENCES );

		if ( showImages )
			BigDataProcessor2.showImage( alignedImage, false ).setDisplaySettings( 0, 200, 0 );
	}

	public static void main( String[] args )
	{
		showImages = true;
		new ImageJ().ui().showUI();
		new TestAlignMovieSIFT().lazySIFT();
	}

}
