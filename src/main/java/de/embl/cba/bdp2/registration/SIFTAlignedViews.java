package de.embl.cba.bdp2.registration;

import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.process.IntervalImageViews;
import de.embl.cba.bdp2.registration.SliceRegistrationSIFT;
import de.embl.cba.bdp2.registration.TransformedStackView;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealRandomAccessible;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.realtransform.RealViews;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.StackView;
import net.imglib2.view.Views;

import java.util.ArrayList;

public class SIFTAlignedViews
{
	public static < R extends RealType< R > & NativeType< R > >
	Image< R > siftAlignFirstVolume( Image< R > image, long referenceSlice )
	{

		final RandomAccessibleInterval< R > volumeView =
				IntervalImageViews.getVolumeView( image.getRai(), 0, 0 );

		referenceSlice = referenceSlice - volumeView.min( 2 );

		final ArrayList< RandomAccessibleInterval< R > > hyperslices = getSlices( image );

		final SliceRegistrationSIFT< R > sift =
				new SliceRegistrationSIFT( hyperslices, referenceSlice, 6 );
		sift.computeTransformsUntilSlice( 0 );
		sift.computeTransformsUntilSlice( hyperslices.size() - 1 );

		final ArrayList< RandomAccessibleInterval< R > > slices = new ArrayList<>();

		for ( int slice = 0; slice < volumeView.dimension( 2 ); slice++ )
		{
			final RandomAccessibleInterval< R > sliceView = IntervalImageViews.getSliceView( image.getRai(), slice, 0, 0 );

			RealRandomAccessible rra =
					Views.interpolate( Views.extendZero( sliceView ), new NLinearInterpolatorFactory<>() );

			final IntervalView transformed = Views.interval(
					Views.raster(
							RealViews.transform( rra, sift.getTransform( slice ) )
					), sliceView );

			slices.add( transformed );
		}

		RandomAccessibleInterval< R > stackView = new StackView<>( slices );

		final Image< R > alignedImage = image.newImage( volumeTo5D( stackView ) );
		alignedImage.setName( "aligned" );

		return alignedImage;
	}

	public static < R extends RealType< R > & NativeType< R > >
	Image< R > lazySIFTAlignFirstVolume( Image< R > image, long referenceSlice )
	{
		referenceSlice = referenceSlice - image.getRai().min( 2 );

		final ArrayList< RandomAccessibleInterval< R > > hyperslices = getSlices( image );

		final SliceRegistrationSIFT< R > registration =
				new SliceRegistrationSIFT<>( hyperslices, referenceSlice, 6 );

		new Thread( () -> registration.computeAllTransforms() ).start();

		RandomAccessibleInterval< R > registered = new TransformedStackView( hyperslices, registration );

		final Image< R > alignedImage = image.newImage( volumeTo5D( registered ) );

		alignedImage.setName( "lazy aligned" );

		return alignedImage;
	}

	private static < R extends RealType< R > & NativeType< R > >
	RandomAccessibleInterval<R> volumeTo5D( RandomAccessibleInterval< R > rai )
	{
		rai = Views.addDimension( rai, 0, 0 );
		rai = Views.addDimension( rai, 0, 0 );
		return rai;
	}

	private static < R extends RealType< R > & NativeType< R > > ArrayList< RandomAccessibleInterval< R > > getSlices( Image< R > image )
	{
		final ArrayList< RandomAccessibleInterval< R > > hyperslices = new ArrayList<>();

		for ( int slice = 0; slice < image.getRai().dimension( 2 ); slice++ )
		{
			final RandomAccessibleInterval< R > sliceView = IntervalImageViews.getSliceView( image.getRai(), slice, 0, 0 );
			hyperslices.add( sliceView );
		}
		return hyperslices;
	}
}
