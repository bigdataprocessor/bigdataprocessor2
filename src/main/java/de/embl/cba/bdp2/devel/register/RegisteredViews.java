/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2022 EMBL
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package de.embl.cba.bdp2.devel.register;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.utils.RAISlicer;
import de.embl.cba.bdp2.log.progress.LoggingProgressListener;
import de.embl.cba.bdp2.log.progress.ProgressListener;
import de.embl.cba.bdp2.utils.DimensionOrder;
import de.embl.cba.bdp2.viewer.ImageViewer;
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

		final Image< R > alignedImage = new Image<>( image );
		alignedImage.setRai( volumeTo5D( registered ) );
		alignedImage.setName( "SIFT aligned" );

		if ( lazy )
			alignedImage.addStopableProcess( registration );

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
		logRegistrationStart();

		final ArrayList< RandomAccessibleInterval< R > > hyperslices = getFrames( image );

		final Registration< R > registration =
				new Registration<>(
						hyperslices,
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

		final Image< R > alignedImage = new Image<>( image );
		alignedImage.setRai( registered  );
		alignedImage.setName( "aligned with " + registrationMethod );

		if ( lazy )
			alignedImage.addStopableProcess( registration );

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
					RAISlicer.getSliceView( image.getRai(), z, c, t );

			hyperslices.add( sliceView );
		}

		return hyperslices;
	}

	private static < R extends RealType< R > & NativeType< R > >
	ArrayList< RandomAccessibleInterval< R > > getFrames( Image< R > image )
	{
		final ArrayList< RandomAccessibleInterval< R > > hyperslices = new ArrayList<>();

		for ( int t = 0; t < image.getRai().dimension( DimensionOrder.T ); t++ )
		{
			final RandomAccessibleInterval< R > frame =
					RAISlicer.getFrameView( image.getRai(), t );

			hyperslices.add( frame );
		}

		return hyperslices;
	}

	// TODO: put as method into BigDataProcessor2
	public static void showSIFTVolumeAlignedBdvView( ImageViewer imageViewer )
	{
		logRegistrationStart();

		final double currentSlice = getCurrentPlane( imageViewer );

		final Image alignedImage = siftAlignFirstVolume(
				imageViewer.getImage(),
				(long) currentSlice,
				true,
				new LoggingProgressListener( "SIFT" ) );

		imageViewer.showImageInNewWindow( alignedImage );
	}

	// TODO: put as method into BigDataProcessor2
	public static void createAlignedMovieView( ImageViewer imageViewer,
											   String registrationMethod,
											   long channel )
	{
		final double currentPlane = getCurrentPlane( imageViewer );
		final Image image = imageViewer.getImage();

		final FinalInterval hyperSliceInterval = FinalInterval.createMinMax(
				0, 0, (long) currentPlane, channel,
				image.getRai().max( 0 ), image.getRai().max( 1 ), (long) currentPlane, channel );

		final Image alignedImage = alignMovie(
				imageViewer.getImage(),
				imageViewer.getCurrentTimePoint(),
				hyperSliceInterval,
				true,
				new LoggingProgressListener( registrationMethod ),
				registrationMethod );

		imageViewer.showImageInNewWindow( alignedImage );
	}

	private static void logRegistrationStart()
	{
		Logger.log("Registration started...");
		Logger.log("To stop it, select the BDV window and [Ctrl + S].");
	}


	private static double getCurrentPlane( ImageViewer imageViewer )
	{
		final FinalRealInterval interval =
				BdvUtils.getViewerGlobalBoundingInterval( imageViewer.getBdvHandle() );

		final double currentSlice = interval.realMax( DimensionOrder.Z )
				/ imageViewer.getImage().getVoxelDimensions()[ DimensionOrder.Z ];

		return currentSlice;
	}
}
