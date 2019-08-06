package de.embl.cba.bdp2.sift;

import ij.IJ;
import ij.process.ImageProcessor;
import mpicbg.ij.InverseTransformMapping;
import mpicbg.ij.Mapping;
import mpicbg.ij.SIFT;
import mpicbg.imagefeatures.Feature;
import mpicbg.imagefeatures.FloatArray2DSIFT;
import mpicbg.models.*;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.realtransform.AffineTransform2D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

public class SliceRegistrationSIFT < R extends RealType< R > & NativeType< R > >
{
	private final RandomAccessibleInterval< R > rai3d;
	private final long referenceSlice;
	private final Map< Long, AffineTransform2D > sliceToTransform;
	private final Map< Long, List< Feature > > sliceToFeatures;

	private int sliceDimension;
	private final int numThreads;


	static private class Param
	{
		final public FloatArray2DSIFT.Param sift = new FloatArray2DSIFT.Param();

		/**
		 * Closest/next closest neighbour distance ratio
		 */
		public float rod = 0.92f;

		/**
		 * Maximal allowed alignment error in px
		 */
		public float maxEpsilon = 25.0f;

		/**
		 * Inlier/candidates ratio
		 */
		public float minInlierRatio = 0.05f;

		/**
		 * Implemeted transformation models for choice
		 */
		final static public String[] modelStrings = new String[]{ "Translation", "Rigid", "Similarity", "Affine" };
		public int modelIndex = 0;

		public boolean interpolate = true;

		public boolean showInfo = false;

		public boolean showMatrix = false;
	}

	final static Param p = new Param();


	public SliceRegistrationSIFT( RandomAccessibleInterval< R > rai3d, long referenceSlice, int numThreads )
	{
		this.rai3d = rai3d;
		this.referenceSlice = referenceSlice;
		this.numThreads = numThreads;
		this.sliceToTransform = new HashMap< >( );
		this.sliceDimension = 2;
		this.sliceToFeatures = new ConcurrentHashMap<>( );
	}

	public AffineTransform2D getTransform( long slice )
	{
		if ( ! sliceToTransform.containsKey( slice ) )
		{
			computeMissingFeatures( slice );
			computeMissingTransforms( slice );
		}

		return sliceToTransform.get( slice );
	}

	private void computeMissingTransforms( long requestedSlice )
	{
		final ExecutorService executorService = Executors.newFixedThreadPool( numThreads );
		final ArrayList< Future > futures = new ArrayList<>();

		final int step = getStep( requestedSlice );

		for ( long slice = referenceSlice; slice != requestedSlice ; slice += step )
		{
			if ( sliceToTransform.containsKey( slice ) ) continue;

			final long featureSlice = slice;

			futures.add( executorService.submit( () -> {

				final Vector< PointMatch > candidates =
						FloatArray2DSIFT.createMatches(
								sliceToFeatures.get( featureSlice + step ),
								sliceToFeatures.get( featureSlice ),
								1.5f,
								null,
								Float.MAX_VALUE, p.rod );

				final Vector< PointMatch > inliers = new Vector< PointMatch >();

				AbstractAffineModel2D< ? > model = getAbstractAffineModel2D();

				boolean modelFound;
				try
				{
					modelFound = model.filterRansac(
							candidates,
							inliers,
							1000,
							p.maxEpsilon,
							p.minInlierRatio );
				}
				catch ( final Exception e )
				{
					modelFound = false;
					System.err.println( e.getMessage() );
				}

				if ( modelFound )
				{
					final AffineTransform affine = model.createAffine();
					final double[] array = new double[ 6 ];
					affine.getMatrix( array );
					final AffineTransform2D affineTransform2D = new AffineTransform2D();
					affineTransform2D.set( array );
					sliceToTransform.put( featureSlice, affineTransform2D );
				}
				else
				{
					sliceToTransform.put( featureSlice, null );
				}
			} ) );

		}

		collectFutures( executorService, futures );

	}

	private AbstractAffineModel2D< ? > getAbstractAffineModel2D()
	{
		AbstractAffineModel2D< ? > currentModel;
		switch ( p.modelIndex )
		{
			case 0:
				currentModel = new TranslationModel2D();
				break;
			case 1:
				currentModel = new RigidModel2D();
				break;
			case 2:
				currentModel = new SimilarityModel2D();
				break;
			case 3:
				currentModel = new AffineModel2D();
				break;
			default:
				currentModel = null;
		}
		return currentModel;
	}

	private void collectFutures( ExecutorService executorService, ArrayList< Future > futures )
	{
		for ( Future future : futures )
		{
			try
			{
				future.get();
			} catch ( InterruptedException e )
			{
				e.printStackTrace();
			} catch ( ExecutionException e )
			{
				e.printStackTrace();
			}
		}
		executorService.shutdown();
	}

	private void computeMissingFeatures( long requestedSlice )
	{
		final ExecutorService executorService = Executors.newFixedThreadPool( numThreads );
		final ArrayList< Future > futures = new ArrayList<>();

		int step = getStep( requestedSlice );

		for ( long slice = referenceSlice; slice != requestedSlice ; slice += step )
		{
			if ( sliceToFeatures.containsKey( slice ) ) continue;

			final long featureSlice = slice;

			futures.add( executorService.submit( new Runnable()
			{
				@Override
				public void run()
				{
					long start_time = System.currentTimeMillis();
					final ImageProcessor ip = getImageProcessor( featureSlice );
					final ArrayList< Feature > features = new ArrayList<>();
					final FloatArray2DSIFT sift = new FloatArray2DSIFT( p.sift );
					final SIFT ijSIFT = new SIFT( sift );
					ijSIFT.extractFeatures( ip, features );
					System.out.println( "Processing SIFT of slice: " + featureSlice + " took "
							+ ( System.currentTimeMillis() - start_time ) + "ms; "
							+ features.size() + " features extracted." );
					sliceToFeatures.put( featureSlice, features );
				}
			} ) );
		}

		collectFutures( executorService, futures );
	}

	private ImageProcessor getImageProcessor( long featureSlice )
	{
		final long[] min = Intervals.minAsLongArray( rai3d );
		final long[] max = Intervals.maxAsLongArray( rai3d );

		min[ sliceDimension ] = featureSlice;
		max[ sliceDimension ] = featureSlice;

		final RandomAccessibleInterval< R > sliceView
				= Views.dropSingletonDimensions( Views.interval( rai3d, min, max ) );

		return ImageJFunctions.wrap( sliceView, "slice" ).getProcessor();
	}

	private int getStep( long requestedSlice )
	{
		int step;
		if ( requestedSlice < referenceSlice ) step = -1;
		else step = +1;
		return step;
	}

}
