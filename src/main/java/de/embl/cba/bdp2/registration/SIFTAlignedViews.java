package de.embl.cba.bdp2.registration;

import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.logging.Logger;
import de.embl.cba.bdp2.process.IntervalImageViews;
import de.embl.cba.bdp2.progress.LoggingProgressListener;
import de.embl.cba.bdp2.progress.ProgressListener;
import de.embl.cba.bdp2.utils.DimensionOrder;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import de.embl.cba.bdv.utils.BdvUtils;
import net.imglib2.FinalRealInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import java.util.ArrayList;

public class SIFTAlignedViews
{

	public static < R extends RealType< R > & NativeType< R > >
	Image< R > siftAlignFirstVolume( Image< R > image,
									 long referenceSlice,
									 boolean lazy,
									 ProgressListener progressListener )
	{
		referenceSlice = referenceSlice - image.getRai().min( 2 );

		final ArrayList< RandomAccessibleInterval< R > > hyperslices = getSlices( image );

		final SliceRegistrationSIFT< R > registration =
				new SliceRegistrationSIFT<>( hyperslices, referenceSlice, 6 );

		if ( progressListener != null )
			registration.setProgressListener( progressListener );

		if ( lazy )
			new Thread( () -> registration.computeAllTransforms() ).start();
		else
			registration.computeAllTransforms();

		RandomAccessibleInterval< R > registered =
				new TransformedStackView( hyperslices, registration );

		final Image< R > alignedImage = image.newImage( volumeTo5D( registered ) );
		alignedImage.setName( "SIFT aligned" );

		return alignedImage;
	}

	private static < R extends RealType< R > & NativeType< R > >
	RandomAccessibleInterval<R> volumeTo5D( RandomAccessibleInterval< R > rai )
	{
		rai = Views.addDimension( rai, 0, 0 );
		rai = Views.addDimension( rai, 0, 0 );
		return rai;
	}

	private static < R extends RealType< R > & NativeType< R > >
	ArrayList< RandomAccessibleInterval< R > > getSlices( Image< R > image )
	{
		final ArrayList< RandomAccessibleInterval< R > > hyperslices = new ArrayList<>();

		for ( int slice = 0; slice < image.getRai().dimension( 2 ); slice++ )
		{
			final RandomAccessibleInterval< R > sliceView =
					IntervalImageViews.getSliceView( image.getRai(), slice, 0, 0 );

			hyperslices.add( sliceView );
		}
		return hyperslices;
	}

	public static void showAlignedBdvView( BdvImageViewer imageViewer )
	{
		Logger.log("Alignment with SIFT started...");
		final double currentSlice = getCurrentSlice( imageViewer );

		final Image alignedImage = siftAlignFirstVolume(
				imageViewer.getImage(),
				(long) currentSlice,
				true,
				new LoggingProgressListener( "SIFT" ) );

		imageViewer.showImageInNewWindow( alignedImage );
	}

	private static double getCurrentSlice( BdvImageViewer imageViewer )
	{
		final FinalRealInterval interval =
				BdvUtils.getViewerGlobalBoundingInterval( imageViewer.getBdvHandle() );

		final double currentSlice = interval.realMax( DimensionOrder.Z )
				/ imageViewer.getImage().getVoxelSpacing()[ DimensionOrder.Z ];

		return currentSlice;
	}
}
