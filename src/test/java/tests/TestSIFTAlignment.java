package tests;

import bdv.util.BdvFunctions;
import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.process.IntervalImageViews;
import de.embl.cba.bdp2.sift.SliceRegistrationSIFT;
import de.embl.cba.bdp2.ui.BigDataProcessor2;
import de.embl.cba.transforms.utils.Transforms;
import itc.utilities.TransformUtils;
import net.imagej.ImageJ;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealRandomAccessible;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.realtransform.AffineTransform2D;
import net.imglib2.realtransform.RealViews;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IntervalView;
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
				new SliceRegistrationSIFT<>( volumeView, 20, 6 );
		sift.computeTransformsUntilSlice( volumeView.min( 2 ) );
		sift.computeTransformsUntilSlice( volumeView.max( 2 ) );

		final ArrayList< RandomAccessibleInterval< R > > slices = new ArrayList<>();

		for ( int slice = 0; slice < volumeView.dimension( 2 ); slice++ )
		{
			final RandomAccessibleInterval< R > sliceView = IntervalImageViews.getSliceView( image.getRai(), slice, 0, 0 );

			RealRandomAccessible rra =
					Views.interpolate( Views.extendZero( sliceView ), new NLinearInterpolatorFactory<>() );

			final IntervalView transformed = Views.interval(
					Views.raster(
							RealViews.transform( rra, sift.getGlobalTransform( slice ) )
					), sliceView );

			slices.add( transformed );
		}

		RandomAccessibleInterval< R > stackView = new StackView<>( slices );

		//BdvFunctions.show( stackView, "aligned" );

		stackView = Views.addDimension( stackView, 0, 0 );
		stackView = Views.addDimension( stackView, 0, 0 );

		final Image< R > alignedImage = image.newImage( stackView );
		alignedImage.setName( "aligned" );

		bdp.showImage( alignedImage );

	}

	public static void main( String[] args )
	{
		new TestSIFTAlignment().testSIFTFeatureComputation();
	}

}
