package de.embl.cba.bdp2.sift;

import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.process.IntervalImageViews;
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
	Image< R > siftAlignImage( Image< R > image, long referenceSlice )
	{
		final RandomAccessibleInterval< R > volumeView =
				IntervalImageViews.getVolumeView( image.getRai(), 0, 0 );

		final SliceRegistrationSIFT< R > sift =
				new SliceRegistrationSIFT<>( volumeView, referenceSlice, 6 );
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
							RealViews.transform( rra, sift.getTransform( slice ) )
					), sliceView );

			slices.add( transformed );
		}

		RandomAccessibleInterval< R > stackView = new StackView<>( slices );
		stackView = Views.addDimension( stackView, 0, 0 );
		stackView = Views.addDimension( stackView, 0, 0 );

		final Image< R > alignedImage = image.newImage( stackView );
		alignedImage.setName( "aligned" );

		return alignedImage;
	}

	public static < R extends RealType< R > & NativeType< R > >
	Image< R > lazySIFTAlignVolume( Image< R > image, long referenceSlice )
	{
		final ArrayList< RandomAccessibleInterval< R > > hyperslices = new ArrayList<>();

		for ( int slice = 0; slice < image.getRai().dimension( 2 ); slice++ )
		{
			final RandomAccessibleInterval< R > sliceView = IntervalImageViews.getSliceView( image.getRai(), slice, 0, 0 );
			hyperslices.add( sliceView );
		}

		final SliceRegistrationSIFT< R > registration = ( SliceRegistrationSIFT< R > )
				new SliceRegistrationSIFT<>( hyperslices, referenceSlice, 6 );

		RandomAccessibleInterval< R > registered = new TransformedStackView( hyperslices, registration );

		final Image< R > alignedImage = image.newImage( lazyAlignedRai3D );
		alignedImage.setName( "lazy aligned" );

		return alignedImage;
	}
}
