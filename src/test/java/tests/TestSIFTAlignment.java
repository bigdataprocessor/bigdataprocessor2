package tests;

import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.process.IntervalImageViews;
import de.embl.cba.bdp2.sift.SliceRegistrationSIFT;
import de.embl.cba.bdp2.ui.BigDataProcessor2;
import net.imagej.ImageJ;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.realtransform.AffineTransform2D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.StackView;
import net.imglib2.view.Views;
import org.junit.Test;

import java.util.ArrayList;

public class TestSIFTAlignment < R extends RealType< R > & NativeType< R > >
{

	@Test
	public void testSIFTFeatureComputation()
	{
		new ImageJ().ui().showUI();

		final BigDataProcessor2< R > bdp = new BigDataProcessor2<>();

		final Image< R > image = bdp.openImage(
				"/Users/tischer/Documents/fiji-plugin-bigDataTools2/src/test/resources/test-data/em-2d-sift-align-01",
				FileInfos.TIFF_SLICES,
				".*.tif" );

		final RandomAccessibleInterval< R > volumeView =
				IntervalImageViews.getVolumeView( image.getRai(), 0, 0 );

		final SliceRegistrationSIFT< R > sift =
				new SliceRegistrationSIFT<>( volumeView, 20, 4 );


		final ArrayList< RandomAccessibleInterval< R > > slices = new ArrayList<>();
		for ( int slice = 0; slice < volumeView.dimension( 2 ); slice++ )
		{
			slices.add( IntervalImageViews.getSliceView( image.getRai(), slice, 0, 0 ) );
		}

		final StackView< R > stackView = new StackView<>( slices );


		final AffineTransform2D transform = sift.getTransform( 30 );
	}

	public static void main( String[] args )
	{
		new TestSIFTAlignment().testSIFTFeatureComputation();
	}

}
