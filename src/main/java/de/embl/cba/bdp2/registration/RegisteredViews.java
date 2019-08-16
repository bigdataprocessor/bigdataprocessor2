package de.embl.cba.bdp2.registration;

import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.logging.Logger;
import de.embl.cba.bdp2.process.IntervalImageViews;
import de.embl.cba.bdp2.progress.LoggingProgressListener;
import de.embl.cba.bdp2.progress.ProgressListener;
import de.embl.cba.bdp2.utils.DimensionOrder;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import de.embl.cba.bdv.utils.BdvUtils;
import net.imglib2.FinalInterval;
import net.imglib2.FinalRealInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import java.util.ArrayList;

public class RegisteredViews
{
	public static < R extends RealType< R > & NativeType< R > >
	Image< R > siftAlignFirstVolume( Image< R > image,
									 long referenceSlice,
									 boolean lazy,
									 ProgressListener progressListener )
	{
		referenceSlice = referenceSlice - image.getRai().min( 2 );

		final ArrayList< RandomAccessibleInterval< R > > hyperslices = getPlanes( image, 0, 0 );

		final Registration< R > registration =
				new Registration(
						hyperslices,
						referenceSlice,
						6, Registration.SIFT_CORRESPONDENCES );

		if ( progressListener != null )
			registration.setProgressListener( progressListener );

		if ( lazy )
			new Thread( () -> registration.computeTransforms() ).start();
		else
			registration.computeTransforms();

		RandomAccessibleInterval< R > registered =
				new TransformedStackView( hyperslices, registration );

		final Image< R > alignedImage = image.newImage( volumeTo5D( registered ) );
		alignedImage.setName( "SIFT aligned" );

		return alignedImage;
	}

	// TODO: link to here from BigDataProcessor2
	public static < R extends RealType< R > & NativeType< R > >
	Image< R > alignMovie( Image< R > image,
						   long referenceTimePoint,
						   FinalInterval hyperSliceInterval,
						   boolean lazy,
						   ProgressListener progressListener,
						   String registrationMethod )
	{
		final ArrayList< RandomAccessibleInterval< R > > hyperslices = getVolumes( image, 0 );

		final Registration< R > registration =
				new Registration<>( hyperslices,
						referenceTimePoint,
						hyperSliceInterval,
						registrationMethod,
						6 );

		if ( progressListener != null )
			registration.setProgressListener( progressListener );

		if ( lazy )
			new Thread( () -> registration.computeTransforms() ).start();
		else
			registration.computeTransforms();

		RandomAccessibleInterval< R > registered =
				new TransformedStackView( hyperslices, registration );

		final Image< R > alignedImage = image.newImage( movieTo5D( registered ) );
		alignedImage.setName( "Aligned with " + registrationMethod );

		return alignedImage;
	}


	private static < R extends RealType< R > & NativeType< R > >
	RandomAccessibleInterval<R> movieTo5D( RandomAccessibleInterval< R > rai )
	{
		rai = Views.addDimension( rai, 0, 0 );
		rai = Views.permute( rai, 3, 4 );
		return rai;
	}

	private static < R extends RealType< R > & NativeType< R > >
	RandomAccessibleInterval<R> volumeTo5D( RandomAccessibleInterval< R > rai )
	{
		rai = Views.addDimension( rai, 0, 0 );
		rai = Views.addDimension( rai, 0, 0 );
		return rai;
	}

	private static < R extends RealType< R > & NativeType< R > >
	ArrayList< RandomAccessibleInterval< R > > getPlanes( Image< R > image, int c, int t )
	{
		final ArrayList< RandomAccessibleInterval< R > > hyperslices = new ArrayList<>();

		for ( int z = 0; z < image.getRai().dimension( DimensionOrder.Z ); z++ )
		{
			final RandomAccessibleInterval< R > sliceView =
					IntervalImageViews.getSliceView( image.getRai(), z, c, t );

			hyperslices.add( sliceView );
		}

		return hyperslices;
	}

	private static < R extends RealType< R > & NativeType< R > >
	ArrayList< RandomAccessibleInterval< R > > getVolumes( Image< R > image, int c )
	{
		final ArrayList< RandomAccessibleInterval< R > > hyperslices = new ArrayList<>();

		for ( int t = 0; t < image.getRai().dimension( DimensionOrder.T ); t++ )
		{
			final RandomAccessibleInterval< R > sliceView =
					IntervalImageViews.getVolumeView( image.getRai(), c, t );

			hyperslices.add( sliceView );
		}

		return hyperslices;
	}


	public static void showSIFTVolumeAlignedBdvView( BdvImageViewer imageViewer )
	{
		Logger.log("Alignment with SIFT started...");
		final double currentSlice = getCurrentPlane( imageViewer );

		final Image alignedImage = siftAlignFirstVolume(
				imageViewer.getImage(),
				(long) currentSlice,
				true,
				new LoggingProgressListener( "SIFT" ) );

		imageViewer.showImageInNewWindow( alignedImage );
	}

	// TODO: put into BigDataProcessor2
	public static void showAlignedMovieView( BdvImageViewer imageViewer,
											 String registrationMethod )
	{
		Logger.log("Alignment with started...");
		final double currentPlane = getCurrentPlane( imageViewer );
		final Image image = imageViewer.getImage();

		final FinalInterval hyperSliceInterval = FinalInterval.createMinMax(
				0, 0, (long) currentPlane,
				image.getRai().max( 0 ), image.getRai().max( 1 ), (long) currentPlane );

		final Image alignedImage = alignMovie(
				imageViewer.getImage(),
				imageViewer.getCurrentTimePoint(),
				hyperSliceInterval,
				true,
				new LoggingProgressListener( "SIFT" ),
				registrationMethod );

		imageViewer.showImageInNewWindow( alignedImage );
	}


	private static double getCurrentPlane( BdvImageViewer imageViewer )
	{
		final FinalRealInterval interval =
				BdvUtils.getViewerGlobalBoundingInterval( imageViewer.getBdvHandle() );

		final double currentSlice = interval.realMax( DimensionOrder.Z )
				/ imageViewer.getImage().getVoxelSpacing()[ DimensionOrder.Z ];

		return currentSlice;
	}
}
