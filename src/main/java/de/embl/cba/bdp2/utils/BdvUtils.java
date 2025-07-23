/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2025 EMBL
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
package de.embl.cba.bdp2.utils;

import bdv.ViewerSetupImgLoader;
import bdv.tools.transformation.TransformedSource;
import bdv.util.*;
import bdv.viewer.Interpolation;
import bdv.viewer.Source;
import bdv.viewer.SourceAndConverter;
import bdv.viewer.animate.AbstractTransformAnimator;
import bdv.viewer.animate.SimilarityTransformAnimator;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imglib2.*;
import net.imglib2.converter.Converter;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.util.LinAlgHelpers;
import net.imglib2.util.Util;
import net.imglib2.view.Views;
import org.apache.commons.lang.WordUtils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.DoubleStream;

import static de.embl.cba.bdp2.utils.Transforms.createBoundingIntervalAfterTransformation;

public abstract class BdvUtils
{
	public static final String OVERLAY = "overlay";

	public static Interval getSourceGlobalBoundingInterval( Bdv bdv, Source< ? > source )
	{
		final AffineTransform3D sourceTransform = getSourceTransform( source );
		final RandomAccessibleInterval< ? > rai = source.getSource( 0,0  );
		final Interval interval = Intervals.smallestContainingInterval( sourceTransform.estimateBounds( rai ) );
		return interval;
	}

	public static void zoomToSource( Bdv bdv, Source< ? > source )
	{
		final FinalInterval interval = getInterval( source );
		zoomToInterval( bdv, interval, 1.0 );
	}

	public static FinalInterval getInterval( Source< ? > source )
	{
		final AffineTransform3D sourceTransform = getSourceTransform( source );
		final RandomAccessibleInterval< ? > rai = source.getSource( 0, 0 );
		return createBoundingIntervalAfterTransformation( rai, sourceTransform );
	}

	public static FinalRealInterval getViewerGlobalBoundingInterval( Bdv bdv )
	{
		AffineTransform3D viewerTransform = new AffineTransform3D();
		bdv.getBdvHandle().getViewerPanel().state().getViewerTransform( viewerTransform );
		viewerTransform = viewerTransform.inverse();
		final long[] min = new long[ 3 ];
		final long[] max = new long[ 3 ];
		max[ 0 ] = bdv.getBdvHandle().getViewerPanel().getWidth();
		max[ 1 ] = bdv.getBdvHandle().getViewerPanel().getHeight();
		final FinalRealInterval realInterval
				= viewerTransform.estimateBounds( new FinalInterval( min, max ) );
		return realInterval;
	}

	public static int getSourceIndex( Bdv bdv, Source< ? > source )
	{
		final List< SourceAndConverter< ? > > sources =
				bdv.getBdvHandle().getViewerPanel().state().getSources();

		for ( int i = 0; i < sources.size(); ++i )
			if ( sources.get( i ).getSpimSource().equals( source ) )
				return i;

		return -1;
	}

	public static Source< ? > getSource( Bdv bdv, int sourceIndex )
	{
		final List< SourceAndConverter< ? > > sources = bdv.getBdvHandle().getViewerPanel().state().getSources();

		return sources.get( sourceIndex ).getSpimSource();
	}

	public static Converter< ?, ARGBType > getConverter( Bdv bdv, int sourceIndex )
	{
		final List< SourceAndConverter< ? > > sources = bdv.getBdvHandle().getViewerPanel().state().getSources();

		return sources.get( sourceIndex ).getConverter();
	}


	public static Source< ? > getVolatileSource( Bdv bdv, int sourceIndex )
	{
		final List< SourceAndConverter< ? > > sources = bdv.getBdvHandle().getViewerPanel().state().getSources();

		return sources.get( sourceIndex ).asVolatile().getSpimSource();
	}

	public static String getSourceName( Bdv bdv, int sourceId )
	{
		return bdv.getBdvHandle().getViewerPanel()
				.state().getSources().get( sourceId ).getSpimSource().getName();
	}

	public static ArrayList< String > getSourceNames( Bdv bdv )
	{
		final ArrayList< String > sourceNames = new ArrayList<>();

		final List< SourceAndConverter< ? > > sources = bdv.getBdvHandle().getViewerPanel().state().getSources();

		for ( SourceAndConverter< ? > source : sources )
			sourceNames.add( source.getSpimSource().getName() );

		return sourceNames;
	}


	public static int getSourceIndex( Bdv bdv, String sourceName )
	{
		return getSourceNames( bdv ).indexOf( sourceName );
	}

	public static VoxelDimensions getVoxelDimensions( SourceAndConverter< ? > sourceAndConverter )
	{
		return sourceAndConverter.getSpimSource().getVoxelDimensions();
	}

	/**
	 * TODO: does that make sense?
	 *
	 * @param bdv
	 * @return
	 */
	public static double[] getViewerVoxelSpacingOLD( BdvHandle bdv )
	{

		// TODO: understand this logic!
		final AffineTransform3D viewerTransform = new AffineTransform3D();
		bdv.getViewerPanel().state().getViewerTransform( viewerTransform );

		final double[] zeroCanvas = { 0.0, 0.0, 0.0 };
		final double[] zeroGlobal = new double[ 3 ];

		final double[] oneCanvas = { 1.0, 1.0, 1.0 };
		final double[] oneGlobal = new double[ 3 ];

		viewerTransform.applyInverse( zeroGlobal, zeroCanvas );
		viewerTransform.applyInverse( oneGlobal, oneCanvas );

		final double[] viewerVoxelSpacing = new double[ 3 ];
		for ( int d = 0; d < 3; d++ )
			viewerVoxelSpacing[ d ] = Math.abs( zeroGlobal[ d ] - oneGlobal[ d ]);

		return viewerVoxelSpacing;
	}

	public static double getViewerVoxelSpacing( BdvHandle bdv )
	{
		final int windowWidth = getBdvWindowWidth( bdv );
		final int windowHeight = getBdvWindowHeight( bdv );

		// TODO: understand this logic!
		final AffineTransform3D viewerTransform = new AffineTransform3D();
		bdv.getViewerPanel().state().getViewerTransform( viewerTransform );

		final double[] physicalA = new double[ 3 ];
		final double[] physicalB = new double[ 3 ];

		viewerTransform.applyInverse( physicalA, new double[]{ 0, 0, 0} );
		viewerTransform.applyInverse( physicalB, new double[]{ 0, windowWidth, 0} );

		double viewerPhysicalWidth = LinAlgHelpers.distance( physicalA, physicalB );

		viewerTransform.applyInverse( physicalA, new double[]{ 0, 0, 0} );
		viewerTransform.applyInverse( physicalB, new double[]{ windowHeight, 0, 0} );

		double viewerPhysicalHeight = LinAlgHelpers.distance( physicalA, physicalB );

		final double viewerPhysicalVoxelSpacingX = viewerPhysicalWidth / windowWidth;
		final double viewerPhysicalVoxelSpacingY = viewerPhysicalHeight / windowHeight;

//		IJ.log( "[DEBUG] windowWidth = " + windowWidth );
//		IJ.log( "[DEBUG] windowHeight = " + windowHeight );
//		IJ.log( "[DEBUG] viewerPhysicalWidth = " + viewerPhysicalWidth );
//		IJ.log( "[DEBUG] viewerPhysicalHeight = " + viewerPhysicalHeight );
//		IJ.log( "[DEBUG] viewerPhysicalVoxelSpacingX = " + viewerPhysicalVoxelSpacingX );
//		IJ.log( "[DEBUG] viewerPhysicalVoxelSpacingY = " + viewerPhysicalVoxelSpacingY );

		return viewerPhysicalVoxelSpacingX;
	}


	public static AffineTransform3D getSourceTransform( Source< ? > spimSource )
	{
		final AffineTransform3D sourceTransform = new AffineTransform3D();
		spimSource.getSourceTransform( 0, 0, sourceTransform );
		return sourceTransform;
	}


	public static AffineTransform3D getSourceTransform( Source source, int t, int level )
	{
		AffineTransform3D sourceTransform = new AffineTransform3D();
		source.getSourceTransform( t, level, sourceTransform );
		return sourceTransform;
	}


	public static RandomAccessibleInterval< ? > getRandomAccessibleInterval( Bdv bdv, int sourceId )
	{
		return bdv.getBdvHandle().getViewerPanel().state().getSources().get( sourceId ).getSpimSource().getSource( 0, 0 );
	}

	public static RealRandomAccessible< ? > getRealRandomAccessible( Bdv bdv, int sourceId )
	{
		return bdv.getBdvHandle().getViewerPanel().state().getSources().get( sourceId ).getSpimSource().getInterpolatedSource( 0, 0, Interpolation.NLINEAR );
	}

	public static void zoomToInterval( Bdv bdv, FinalInterval interval, double zoomFactor )
	{
		final AffineTransform3D affineTransform3D = getImageZoomTransform( bdv, interval, zoomFactor );

		bdv.getBdvHandle().getViewerPanel().state().setViewerTransform( affineTransform3D );
	}

	public static AffineTransform3D getImageZoomTransform( Bdv bdv, FinalInterval interval, double zoomFactor )
	{
		final AffineTransform3D affineTransform3D = new AffineTransform3D();

		double[] shiftToImage = new double[ 3 ];

		for ( int d = 0; d < 3; ++d )
			shiftToImage[ d ] = -( interval.min( d ) + interval.dimension( d ) / 2.0 );

		affineTransform3D.translate( shiftToImage );

		int[] bdvWindowDimensions = new int[ 2 ];
		bdvWindowDimensions[ 0 ] = bdv.getBdvHandle().getViewerPanel().getWidth();
		bdvWindowDimensions[ 1 ] = bdv.getBdvHandle().getViewerPanel().getHeight();

		affineTransform3D.scale( zoomFactor * bdvWindowDimensions[ 0 ] / interval.dimension( 0 ) );

		double[] shiftToBdvWindowCenter = new double[ 3 ];

		for ( int d = 0; d < 2; ++d )
			shiftToBdvWindowCenter[ d ] += bdvWindowDimensions[ d ] / 2.0;

		affineTransform3D.translate( shiftToBdvWindowCenter );

		return affineTransform3D;
	}

	public static Source getSource( BdvStackSource bdvStackSource, int i )
	{
		final SourceAndConverter sourceAndConverter = ( SourceAndConverter ) bdvStackSource.getSources().get( 0 );

		return sourceAndConverter.getSpimSource();
	}

	public static ARGBType asArgbType( Color color )
	{
		return new ARGBType( ARGBType.rgba(
							color.getRed(),
							color.getGreen(),
							color.getBlue(),
							color.getAlpha() ) );
	}

	public static Color asColor( ARGBType argbType )
	{
		return new Color( argbType.get() );
	}

	public static long[] getPositionInSource(
			Source source,
			RealPoint positionInViewer,
			int t,
			int level )
	{
		int n = 3;

		final AffineTransform3D sourceTransform =
				BdvUtils.getSourceTransform( source, t, level );

		final RealPoint positionInSourceInPixelUnits = new RealPoint( n );

		sourceTransform.inverse().apply(
				positionInViewer, positionInSourceInPixelUnits );

		final long[] longPosition = new long[ n ];

		for ( int d = 0; d < n; ++d )
			longPosition[ d ] = (long) positionInSourceInPixelUnits.getFloatPosition( d );

		return longPosition;
	}



	public static double[] getCurrentViewNormalVector( Bdv bdv )
	{
		AffineTransform3D currentViewerTransform = new AffineTransform3D();
		bdv.getBdvHandle().getViewerPanel().state().getViewerTransform( currentViewerTransform );

		final double[] viewerC = new double[]{ 0, 0, 0 };
		final double[] viewerX = new double[]{ 1, 0, 0 };
		final double[] viewerY = new double[]{ 0, 1, 0 };

		final double[] dataC = new double[ 3 ];
		final double[] dataX = new double[ 3 ];
		final double[] dataY = new double[ 3 ];

		final double[] dataV1 = new double[ 3 ];
		final double[] dataV2 = new double[ 3 ];
		final double[] currentNormalVector = new double[ 3 ];

		currentViewerTransform.inverse().apply( viewerC, dataC );
		currentViewerTransform.inverse().apply( viewerX, dataX );
		currentViewerTransform.inverse().apply( viewerY, dataY );

		LinAlgHelpers.subtract( dataX, dataC, dataV1 );
		LinAlgHelpers.subtract( dataY, dataC, dataV2 );

		LinAlgHelpers.cross( dataV1, dataV2, currentNormalVector );

		LinAlgHelpers.normalize( currentNormalVector );

		return currentNormalVector;
	}


	public static void levelCurrentView( Bdv bdv, double[] targetNormalVector )
	{

		double[] currentNormalVector = BdvUtils.getCurrentViewNormalVector( bdv );

		AffineTransform3D currentViewerTransform = new AffineTransform3D();
		bdv.getBdvHandle().getViewerPanel().state().getViewerTransform( currentViewerTransform );

		LinAlgHelpers.normalize( targetNormalVector ); // just to be sure.

		// determine rotation axis
		double[] rotationAxis = new double[ 3 ];
		LinAlgHelpers.cross( currentNormalVector, targetNormalVector, rotationAxis );
		if ( LinAlgHelpers.length( rotationAxis ) > 0 ) LinAlgHelpers.normalize( rotationAxis );

		// The rotation axis is in the coordinate system of the original data set => transform to viewer coordinate system
		double[] qCurrentRotation = new double[ 4 ];
		Affine3DHelpers.extractRotation( currentViewerTransform, qCurrentRotation );
		final AffineTransform3D currentRotation = quaternionToAffineTransform3D( qCurrentRotation );

		double[] rotationAxisInViewerSystem = new double[ 3 ];
		currentRotation.apply( rotationAxis, rotationAxisInViewerSystem );

		// determine rotation angle
		double angle = - Math.acos( LinAlgHelpers.dot( currentNormalVector, targetNormalVector ) );

		// construct rotation of angle around axis
		double[] rotationQuaternion = new double[ 4 ];
		LinAlgHelpers.quaternionFromAngleAxis( rotationAxisInViewerSystem, angle, rotationQuaternion );
		final AffineTransform3D rotation = quaternionToAffineTransform3D( rotationQuaternion );

		// apply transformation (rotating around current viewer centre position)
		final AffineTransform3D translateCenterToOrigin = new AffineTransform3D();
		translateCenterToOrigin.translate( DoubleStream.of( getBdvWindowCenter( bdv )).map( x -> -x ).toArray() );

		final AffineTransform3D translateCenterBack = new AffineTransform3D();
		translateCenterBack.translate( getBdvWindowCenter( bdv ) );

		ArrayList< AffineTransform3D > viewerTransforms = new ArrayList<>(  );

		viewerTransforms.add( currentViewerTransform.copy()
				.preConcatenate( translateCenterToOrigin )
				.preConcatenate( rotation )
				.preConcatenate( translateCenterBack )	);

		changeBdvViewerTransform( bdv, viewerTransforms, 2000 );

	}

	public static double[] getBdvWindowCenter( Bdv bdv )
	{
		final double[] centre = new double[ 3 ];

		centre[ 0 ] = bdv.getBdvHandle().getViewerPanel().getDisplay().getWidth() / 2.0;
		centre[ 1 ] = bdv.getBdvHandle().getViewerPanel().getDisplay().getHeight() / 2.0;

		return centre;
	}

	public static int getBdvWindowWidth( Bdv bdv )
	{
		return bdv.getBdvHandle().getViewerPanel().getDisplay().getWidth();
	}


	public static int getBdvWindowHeight( Bdv bdv )
	{
		return bdv.getBdvHandle().getViewerPanel().getDisplay().getHeight();
	}

	public static AffineTransform3D quaternionToAffineTransform3D( double[] rotationQuaternion )
	{
		double[][] rotationMatrix = new double[ 3 ][ 3 ];
		LinAlgHelpers.quaternionToR( rotationQuaternion, rotationMatrix );
		return matrixAsAffineTransform3D( rotationMatrix );
	}

	public static AffineTransform3D matrixAsAffineTransform3D( double[][] rotationMatrix )
	{
		final AffineTransform3D rotation = new AffineTransform3D();
		for ( int row = 0; row < 3; ++row )
			for ( int col = 0; col < 3; ++ col)
				rotation.set( rotationMatrix[ row ][ col ], row, col);
		return rotation;
	}

	public static void changeBdvViewerTransform(
			Bdv bdv,
			AffineTransform3D newViewerTransform,
			long duration)
	{
		AffineTransform3D currentViewerTransform = new AffineTransform3D();
		bdv.getBdvHandle().getViewerPanel().state().getViewerTransform( currentViewerTransform );

		final SimilarityTransformAnimator similarityTransformAnimator =
				new SimilarityTransformAnimator(
						currentViewerTransform,
						newViewerTransform,
						0 ,
						0,
						duration );

		bdv.getBdvHandle().getViewerPanel().setTransformAnimator( similarityTransformAnimator );
	}

	public static void changeBdvViewerTransform(
			Bdv bdv,
			ArrayList< AffineTransform3D > transforms,
			long duration)
	{

		AffineTransform3D currentTransform = new AffineTransform3D();
		bdv.getBdvHandle().getViewerPanel().state().getViewerTransform( currentTransform );

		ArrayList< SimilarityTransformAnimator > animators = new ArrayList<>(  );

		final SimilarityTransformAnimator firstAnimator =
				new SimilarityTransformAnimator(
						currentTransform.copy(),
						transforms.get( 0 ).copy(),
						0 ,
						0,
						duration );

		animators.add( firstAnimator );

		for ( int i = 1; i < transforms.size(); i++ )
		{
			final SimilarityTransformAnimator animator =
					new SimilarityTransformAnimator(
							transforms.get( i - 1 ).copy(),
							transforms.get( i ).copy(),
							0 ,
							0,
							duration );

			animators.add( animator );
		}


		AbstractTransformAnimator transformAnimator = new ConcatenatedTransformAnimator( duration, animators );

		bdv.getBdvHandle().getViewerPanel().setTransformAnimator( transformAnimator );
		//bdv.getBdvHandle().getViewerPanel().transformChanged( currentTransform.copy() );

	}

	public static ArrayList< Color > getColors( List< Integer > nonOverlaySources )
	{
		ArrayList< Color > defaultColors = new ArrayList<>(  );
		if ( nonOverlaySources.size() > 1 )
		{
			defaultColors.add( Color.BLUE );
			defaultColors.add( Color.GREEN );
			defaultColors.add( Color.RED );
			defaultColors.add( Color.MAGENTA );
			defaultColors.add( Color.GRAY );
			defaultColors.add( Color.GRAY );
			defaultColors.add( Color.GRAY );
			defaultColors.add( Color.GRAY );
			defaultColors.add( Color.GRAY );
		}
		else
		{
			defaultColors.add( Color.GRAY );
		}
		return defaultColors;
	}

	public static List< Integer > getNonOverlaySourceIndices( Bdv bdv, List< SourceAndConverter< ? > > sources )
	{
		final List< Integer > nonOverlaySources = new ArrayList<>(  );

		for ( int sourceIndex = 0; sourceIndex < sources.size(); ++sourceIndex )
		{
			String name = getSourceName( bdv, sourceIndex );
			if ( ! name.contains( OVERLAY ) )
			{
				nonOverlaySources.add( sourceIndex );
			}
		}

		return nonOverlaySources;
	}

//	public static boolean isARGBConvertedRealSource( BdvStackSource bdvStackSource )
//	{
//		final Source source = getSource( bdvStackSource, 0 );
//
//		return isARGBConvertedRealSource( source );
//
//	}
//
//	public static boolean isARGBConvertedRealSource( Source source )
//	{
//		if ( source instanceof TransformedSource )
//		{
//			final Source wrappedVolatileSource = ( ( TransformedSource ) source ).getWrappedRealSource();
//
//			if ( wrappedVolatileSource instanceof ARGBConvertedRealSource )
//			{
//				return true;
//			}
//		}
//
//		return false;
//	}
//
//	public static ARGBConvertedRealSource getLabelsSource( BdvStackSource bdvStackSource )
//	{
//		final Source source = getSource( bdvStackSource, 0 );
//
//		return getLabelsSource( source );
//	}
//
//	private static ARGBConvertedRealSource getLabelsSource( Source source )
//	{
//		if ( source instanceof TransformedSource )
//		{
//			final Source wrappedVolatileSource = ( ( TransformedSource ) source ).getWrappedRealSource();
//
//			if ( wrappedVolatileSource instanceof ARGBConvertedRealSource )
//			{
//				return  ( ( ARGBConvertedRealSource ) wrappedVolatileSource) ;
//			}
//		}
//
//		return null;
//	}
//
	public static RealPoint getGlobalMouseCoordinates( Bdv bdv )
	{
		final RealPoint posInBdvInMicrometer = new RealPoint( 3 );
		bdv.getBdvHandle().getViewerPanel()
				.getGlobalMouseCoordinates( posInBdvInMicrometer );
		return posInBdvInMicrometer;
	}

	public static Set< SourceAndConverter< ? > > getVisibleSources( Bdv bdv )
	{
		return bdv.getBdvHandle().getViewerPanel().state().getVisibleSources();
	}



	/**
	 * Returns the highest level where the source voxel spacings are &lt;= the requested ones.
	 *
	 *
	 * @param source
	 * @param voxelSpacings
	 * @return
	 */
	public static int getLevel( Source< ? > source, double... voxelSpacings )
	{
		final int numMipmapLevels = source.getNumMipmapLevels();
		final int numDimensions = voxelSpacings.length;

		for ( int level = numMipmapLevels - 1; level >= 0 ; level-- )
		{
			final double[] calibration = BdvUtils.getCalibration( source, level );

			boolean allSpacingsSmallerThanRequested = true;

			for ( int d = 0; d < numDimensions; d++ )
			{
                if ( calibration[ d ] > voxelSpacings[ d ] )
                {
                    allSpacingsSmallerThanRequested = false;
                    break;
                }
			}

			if ( allSpacingsSmallerThanRequested )
				return level;
		}
		return 0;
	}

	public static double[] getCalibration( Source source, int level )
	{
		final AffineTransform3D sourceTransform = new AffineTransform3D();

		source.getSourceTransform( 0, level, sourceTransform );

		final double[] calibration = Transforms.getScale( sourceTransform );

		return calibration;
	}

	public static void moveToPosition( Bdv bdv, double[] xyz, int t, long durationMillis )
	{
		if ( t != bdv.getBdvHandle().getViewerPanel().state().getCurrentTimepoint() )
		{
			bdv.getBdvHandle().getViewerPanel().state().setCurrentTimepoint( t );
			durationMillis = 0; // otherwise there can be hickups when changing both the viewer transform and the timepoint
		}

		final AffineTransform3D currentViewerTransform = new AffineTransform3D();
		bdv.getBdvHandle().getViewerPanel().state().getViewerTransform( currentViewerTransform );

		AffineTransform3D newViewerTransform = currentViewerTransform.copy();

		// ViewerTransform
		// applyInverse: coordinates in viewer => coordinates in image
		// apply: coordinates in image => coordinates in viewer

		final double[] locationOfTargetCoordinatesInCurrentViewer = new double[ 3 ];
		currentViewerTransform.apply( xyz, locationOfTargetCoordinatesInCurrentViewer );

		for ( int d = 0; d < 3; d++ )
		{
			locationOfTargetCoordinatesInCurrentViewer[ d ] *= -1;
		}

		newViewerTransform.translate( locationOfTargetCoordinatesInCurrentViewer );

		newViewerTransform.translate( getBdvWindowCenter( bdv ) );

		if ( durationMillis <= 0 )
		{
			bdv.getBdvHandle().getViewerPanel().state().setViewerTransform(  newViewerTransform );
		}
		else
		{
			final SimilarityTransformAnimator similarityTransformAnimator =
					new SimilarityTransformAnimator(
							currentViewerTransform,
							newViewerTransform,
							0,
							0,
							durationMillis );

			bdv.getBdvHandle().getViewerPanel().setTransformAnimator( similarityTransformAnimator );
		}
	}

	public static void zoomToPosition( Bdv bdv, double[] xyzt, Double scale, long durationMillis )
	{
		final AffineTransform3D currentViewerTransform = new AffineTransform3D();
		bdv.getBdvHandle().getViewerPanel().state().getViewerTransform( currentViewerTransform );

		if ( scale == null )
		{
			final double[] currentScales = Transforms.getScale( currentViewerTransform );
			scale = currentScales[ 0 ];
		}

		final AffineTransform3D newViewerTransform = getViewerTransform( bdv, xyzt, scale );

		final SimilarityTransformAnimator similarityTransformAnimator =
				new SimilarityTransformAnimator(
						currentViewerTransform,
						newViewerTransform,
						0,
						0,
						durationMillis );

		bdv.getBdvHandle().getViewerPanel().setTransformAnimator( similarityTransformAnimator );
	}

	public static AffineTransform3D getTranslatedViewerTransform( Bdv bdv, double[] position, AffineTransform3D currentViewerTransform )
	{
		final AffineTransform3D viewerTransform = currentViewerTransform.copy();

		double[] translation = new double[ 3 ];
		for( int d = 0; d < 3; ++d )
		{
			translation[ d ] = - position[ d ];
		}

		viewerTransform.setTranslation( 0,0,0 );
		viewerTransform.translate( translation );

		double[] centerBdvWindowTranslation = getBdvWindowCentre( bdv );
		viewerTransform.translate( centerBdvWindowTranslation );

		return viewerTransform;
	}

	public static double[] getBdvWindowCentre( Bdv bdv )
	{
		int[] bdvWindowDimensions = new int[ 3 ];
		bdvWindowDimensions[ 0 ] = bdv.getBdvHandle().getViewerPanel().getWidth();
		bdvWindowDimensions[ 1 ] = bdv.getBdvHandle().getViewerPanel().getHeight();

		double[] centerBdvWindowTranslation = new double[ 3 ];
		for( int d = 0; d < 3; ++d )
		{
			centerBdvWindowTranslation[ d ] = + bdvWindowDimensions[ d ] / 2.0;
		}
		return centerBdvWindowTranslation;
	}

	public static AffineTransform3D getViewerTransform( Bdv bdv, double[] position, double scale )
	{
		final AffineTransform3D viewerTransform = new AffineTransform3D();

		int[] bdvWindowDimensions = new int[ 3 ];
		bdvWindowDimensions[ 0 ] = bdv.getBdvHandle().getViewerPanel().getWidth();
		bdvWindowDimensions[ 1 ] = bdv.getBdvHandle().getViewerPanel().getHeight();

		double[] translation = new double[ 3 ];
		for( int d = 0; d < 3; ++d )
			translation[ d ] = - position[ d ];

		viewerTransform.setTranslation( translation );
		viewerTransform.scale( scale );

		double[] centerBdvWindowTranslation = new double[ 3 ];
		for( int d = 0; d < 3; ++d )
			centerBdvWindowTranslation[ d ] = + bdvWindowDimensions[ d ] / 2.0;

		viewerTransform.translate( centerBdvWindowTranslation );

		return viewerTransform;
	}

	@Deprecated
	public static ARGBType getSourceColor( Bdv bdv, int sourceId )
	{
		return bdv.getBdvHandle().getSetupAssignments().getConverterSetups().get( sourceId ).getColor();
	}

	public static double[] getDisplayRange( Bdv bdv, SourceAndConverter< ? > source )
	{
		final double displayRangeMin = bdv.getBdvHandle().getConverterSetups().getConverterSetup( source ).getDisplayRangeMin();
		final double displayRangeMax = bdv.getBdvHandle().getConverterSetups().getConverterSetup( source ).getDisplayRangeMax();

		return new double[]{ displayRangeMin, displayRangeMax };
	}
	public static void repaint( Bdv bdv )
	{
		bdv.getBdvHandle().getViewerPanel().requestRepaint();
	}

	public static boolean isActive( Bdv bdv, Source source )
	{
		final Set< SourceAndConverter< ? > > visibleSources = getVisibleSources( bdv );

		for ( SourceAndConverter< ? > visibleSourceAndConverter : visibleSources )
		{
			final Source< ? > visibleSource = visibleSourceAndConverter.getSpimSource();

			if ( visibleSource.equals( source ) ) return true;

			if( visibleSource instanceof TransformedSource )
				if ( ( ( TransformedSource ) visibleSource ).getWrappedSource().equals( source ) )
					return true;
		}

		return false;
	}

	public static boolean isActive( Bdv bdv, int sourceIndex )
	{
		final Set< SourceAndConverter< ? > > visibleSources = getVisibleSources( bdv );

        return visibleSources.contains( sourceIndex );
    }


	public static ArrayList< double[] > getVoxelSpacings( SpimData spimData, int setupId )
	{
		final VoxelDimensions voxelDimensions =
				spimData.getSequenceDescription().getViewSetupsOrdered().get( setupId ).getVoxelSize();
		final ViewerSetupImgLoader loader =
				( ViewerSetupImgLoader ) spimData.getSequenceDescription().getImgLoader().getSetupImgLoader( setupId );
		final double[][] resolutions = loader.getMipmapResolutions();

		final ArrayList< double[] > voxelSpacings = new ArrayList<>();

		for ( int level = 0; level < resolutions.length; level++ )
		{
			final double[] voxelSpacing = new double[ 3 ];
			for ( int d = 0; d < 3; d++ )
				voxelSpacing[ d ] = voxelDimensions.dimension( d ) * resolutions[ level ][ d ];
			voxelSpacings.add( voxelSpacing );
		}

		return voxelSpacings;
	}

	@Deprecated
	// Use: bdvStackSource.removeFromBdv();
	public static < R extends RealType< R > & NativeType< R > >
	void removeSource( BdvHandle bdv, BdvStackSource< R > bdvStackSource )
	{
		bdvStackSource.removeFromBdv();
	}

	public static void centerBdvWindowLocation( BdvHandle bdv )
	{
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		final Window viewerFrame = getViewerFrame( bdv );

		viewerFrame.setLocation(
				screenSize.width / 2 - viewerFrame.getWidth() / 2,
                50 );
	}

	public static Window getViewerFrame( BdvHandle bdv )
	{
		return SwingUtilities.getWindowAncestor( bdv.getViewerPanel() );
	}

	public static < R extends RealType< R > & NativeType< R > >
	RandomAccessibleIntervalSource4D< R > createSourceFrom2DFrameList(
			ArrayList< RandomAccessibleInterval< R > > frames2D,
			String name )
	{
		RandomAccessibleIntervalSource4D< R > source =
				new RandomAccessibleIntervalSource4D(
						Views.permute(
								Views.addDimension(
										Views.stack( frames2D ), 0, 0 ),
								2, 3 ),
						Util.getTypeFromInterval( Views.stack( frames2D ) ),
						name );

		return source;
	}

	public static boolean isSourceIntersectingCurrentView( BdvHandle bdv, Source< ? > sourceIndex )
	{
		final Interval interval = getSourceGlobalBoundingInterval( bdv, sourceIndex );

		final Interval viewerInterval =
				Intervals.smallestContainingInterval(
						getViewerGlobalBoundingInterval( bdv ) );

		final boolean intersects = ! Intervals.isEmpty(
				Intervals.intersect( interval, viewerInterval ) );

		return intersects;
	}


	public static String getShortCutString( String trigger )
	{
		return " [ " + WordUtils.capitalize( trigger ) + " ]";
	}
}
