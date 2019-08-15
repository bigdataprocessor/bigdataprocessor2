package de.embl.cba.bdp2.registration;

import de.embl.cba.bdp2.progress.ProgressListener;
import de.embl.cba.bdp2.utils.Utils;
import ij.ImagePlus;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import mpicbg.ij.SIFT;
import mpicbg.imagefeatures.Feature;
import mpicbg.imagefeatures.FloatArray2DSIFT;
import mpicbg.models.*;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.realtransform.AffineGet;
import net.imglib2.realtransform.AffineTransform2D;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import java.awt.geom.AffineTransform;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

public class Registration< R extends RealType< R > & NativeType< R > > implements HypersliceTransformProvider
{
	private final List< RandomAccessibleInterval< R > > hyperslices;
	private final long referenceHyperSliceIndex;
	private final Map< Long, AffineGet > hyperSliceIndexToLocalTransform;
	private final Map< Long, Boolean > hyperSliceTransformIsBeingComputed;
	private final Map< Long, net.imglib2.realtransform.AffineTransform > hyperSliceIndexToGlobalTransform;
	private final FinalInterval hyperSliceInterval;

	private int numHyperSliceDimensions;
	private final int numThreads;
	private int numHyperSlices;

	private ProgressListener progressListener;
	private int totalTransforms;
	private int countTransforms = 0;

	public Registration( List< RandomAccessibleInterval< R > > hyperslices ,
						 long referenceHyperSliceIndex,
						 int numThreads )
	{
		this( hyperslices, referenceHyperSliceIndex, null, numThreads );
	}

	public Registration(
			List< RandomAccessibleInterval< R > > hyperslices,
			long referenceHyperSliceIndex,
			FinalInterval hyperSliceInterval,
			int numThreads )
	{
		this.hyperslices = hyperslices;
		this.referenceHyperSliceIndex = referenceHyperSliceIndex;
		this.hyperSliceInterval = hyperSliceInterval;
		this.numThreads = numThreads;

		numHyperSlices = hyperslices.size();
		numHyperSliceDimensions = hyperslices.get( 0 ).numDimensions();

		hyperSliceIndexToLocalTransform = new ConcurrentHashMap< >( );
		hyperSliceIndexToLocalTransform.put( referenceHyperSliceIndex,
				new net.imglib2.realtransform.AffineTransform( numHyperSliceDimensions ) );

		hyperSliceIndexToGlobalTransform = new ConcurrentHashMap< >( );
		hyperSliceIndexToGlobalTransform.put( referenceHyperSliceIndex,
				new net.imglib2.realtransform.AffineTransform( numHyperSliceDimensions )  );

		hyperSliceTransformIsBeingComputed = new ConcurrentHashMap< >( );
	}

	@Override
	public net.imglib2.realtransform.AffineTransform getTransform( long hyperSliceIndex )
	{
		if ( hyperSliceIndexToGlobalTransform.containsKey( hyperSliceIndex ))
			return hyperSliceIndexToGlobalTransform.get( hyperSliceIndex );
		else
			return null;
	}

	public void computeSIFTTransforms( )
	{
		totalTransforms = numHyperSlices - 1; // reference slice needs no processing

		new SIFTTransformComputer().computeTransforms();
	}


	public void phaseCorrelationTransforms( )
	{
		totalTransforms = numHyperSlices - 1; // reference slice needs no processing

		new PhaseCorrelationTransformComputer().computeTransforms();
	}


	public void setProgressListener( ProgressListener progressListener )
	{
		this.progressListener = progressListener;
	}


	private void updateProgress()
	{
		countTransforms++;
		if ( progressListener != null )
			progressListener.progress( countTransforms, totalTransforms );
	}

	private AffineGet getLocalTransform( long slice )
	{
		AffineGet currentLocal;

		if ( hyperSliceIndexToLocalTransform.get( slice ) instanceof AffineGetNull )
			currentLocal = new net.imglib2.realtransform.AffineTransform( numHyperSliceDimensions );
		else
			currentLocal = hyperSliceIndexToLocalTransform.get( slice ).copy();

		return currentLocal;
	}

	private int getStep( long requestedSlice )
	{
		int step;
		if ( requestedSlice < referenceHyperSliceIndex ) step = -1;
		else step = +1;
		return step;
	}

	private boolean computeGlobalTransforms( long targetHyperSlice )
	{
		final int step = getStep( targetHyperSlice );

		for ( long hyperSlice = referenceHyperSliceIndex + step; ; hyperSlice += step )
		{
			if ( !hyperSliceIndexToLocalTransform.containsKey( hyperSlice ) ) break;
			if ( hyperSliceIndexToGlobalTransform.containsKey( hyperSlice ) ) continue;

			AffineGet currentLocalTransform = getLocalTransform( hyperSlice );

			final net.imglib2.realtransform.AffineTransform previousGlobal =
					hyperSliceIndexToGlobalTransform.get( hyperSlice - step ).copy();

			final net.imglib2.realtransform.AffineTransform currentGlobal =
					previousGlobal.preConcatenate( currentLocalTransform );

			hyperSliceIndexToGlobalTransform.put( hyperSlice, currentGlobal );

			updateProgress();

			if ( hyperSlice == targetHyperSlice )
				return true;

		}

		return false;
	}



	class PhaseCorrelationTransformComputer
	{

		public PhaseCorrelationTransformComputer( )
		{
		}

		public void computeTransforms( )
		{
			totalTransforms = numHyperSlices - 1; // reference slice needs no processing
			computeTransformsUntil( 0 );
			computeTransformsUntil( numHyperSlices - 1 );
		}

		private void computeTransformsUntil( int targetHyperSliceIndex )
		{
			new Thread( () -> computeLocalTransforms( targetHyperSliceIndex, numThreads ) ).start();

			boolean finished = false;
			while ( ! finished )
				finished = computeGlobalTransforms( 0 );
		}

		/**
		 * Computes features for all hyper-slices between the reference hyper-slice
		 * and the target hyper-slice.
		 *
		 * @param targetHyperSliceIndex
		 * @param numThreads
		 */
		private void computeLocalTransforms( final long targetHyperSliceIndex, int numThreads )
		{
			final ExecutorService executorService = Executors.newFixedThreadPool( numThreads );
			final ArrayList< Future > futures = new ArrayList<>();

			final int step = getStep( targetHyperSliceIndex );

			for ( long hyperSlice = referenceHyperSliceIndex; hyperSlice != ( targetHyperSliceIndex + step ) ; hyperSlice += step )
			{
				if ( hyperSliceIndexToLocalTransform.containsKey( hyperSlice ) ) continue;

				final long currentHyperSliceIndex = hyperSlice;

				futures.add( executorService.submit( new Runnable()
				{
					@Override
					public void run()
					{
						long start_time = System.currentTimeMillis();
						hyperSliceIndexToLocalTransform.put( currentHyperSliceIndex, null );
//						System.out.println( "Processing SIFT of slice: " + currentHyperSliceIndex + " took "
//								+ ( System.currentTimeMillis() - start_time ) + "ms; "
//								+ features.size() + " features extracted." );
					}
				} ) );
			}

			Utils.collectFutures( executorService, futures );
		}
	}

	class SIFTTransformComputer
	{
		private final Map< Long, List< Feature > > hyperSliceIndexToSIFTFeatures;

		public SIFTTransformComputer( )
		{
			hyperSliceIndexToSIFTFeatures = new HashMap<>(  );
			p.sift.initialSigma = 1.0F;
		}

		// SIFT
		private class Param
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
			final public String[] modelStrings = new String[]{ "Translation", "Rigid", "Similarity", "Affine" };

			public int modelIndex = 0;

			public boolean interpolate = true;

			public boolean showInfo = false;

			public boolean showMatrix = false;
		}

		final Param p = new Param();

		public void computeTransforms( )
		{
			totalTransforms = numHyperSlices - 1; // reference slice needs no processing

			computeTransformsUntil( 0 );
			computeTransformsUntil( numHyperSlices - 1 );
		}

		private void computeTransformsUntil( int targetHyperSliceIndex )
		{
			new Thread( () -> computeFeatures( targetHyperSliceIndex, numThreads ) ).start();
			computeTransforms( 0, numThreads );
		}

		/**
		 * Computes features for all hyper-slices between the reference hyper-slice
		 * and the target hyper-slice.
		 *
		 * @param targetHyperSliceIndex
		 * @param numThreads
		 */
		private void computeFeatures( final long targetHyperSliceIndex, int numThreads )
		{
			final ExecutorService executorService = Executors.newFixedThreadPool( numThreads );
			final ArrayList< Future > futures = new ArrayList<>();

			final int step = getStep( targetHyperSliceIndex );

			for ( long hyperSlice = referenceHyperSliceIndex; hyperSlice != ( targetHyperSliceIndex + step ) ; hyperSlice += step )
			{
				if ( hyperSliceIndexToSIFTFeatures.containsKey( hyperSlice ) ) continue;

				final long currentHyperSliceIndex = hyperSlice;

				futures.add( executorService.submit( new Runnable()
				{
					@Override
					public void run()
					{
						long start_time = System.currentTimeMillis();
						final ImageProcessor ip = getImageProcessor( currentHyperSliceIndex );
						final ArrayList< Feature > features = new ArrayList<>();
						final FloatArray2DSIFT sift = new FloatArray2DSIFT( p.sift );
						final SIFT ijSIFT = new SIFT( sift );

						// Auto scale the image (important for the SIFT)
						final FloatProcessor floatProcessor = ip.convertToFloatProcessor();
						floatProcessor.findMinAndMax();

						ijSIFT.extractFeatures( floatProcessor, features );
						System.out.println( "Processing SIFT of slice: " + currentHyperSliceIndex + " took "
								+ ( System.currentTimeMillis() - start_time ) + "ms; "
								+ features.size() + " features extracted." );
						hyperSliceIndexToSIFTFeatures.put( currentHyperSliceIndex, features );
					}
				} ) );
			}

			Utils.collectFutures( executorService, futures );
		}

		private void computeTransforms( final long targetHyperSlice, int numThreads )
		{
			final ExecutorService executorService = Executors.newFixedThreadPool( numThreads );
			final ArrayList< Future > futures = new ArrayList<>();

			final int step = getStep( targetHyperSlice );

			boolean finished = false;
			while ( ! finished )
			{
				for ( long hyperSlice = referenceHyperSliceIndex + step;
					  hyperSlice != ( targetHyperSlice + step );
					  hyperSlice += step )
				{
					if ( hyperSliceIndexToLocalTransform.containsKey( hyperSlice ) ) continue;
					if ( hyperSliceTransformIsBeingComputed.containsKey( hyperSlice ) ) continue;
					if ( !hyperSliceIndexToSIFTFeatures.containsKey( hyperSlice ) ) continue;
					if ( !hyperSliceIndexToSIFTFeatures.containsKey( hyperSlice - step ) ) continue;

					hyperSliceTransformIsBeingComputed.put( hyperSlice, true );

					final long finalSlice = hyperSlice;
					futures.add( executorService.submit( () -> computeLocalTransform( step, finalSlice ) ) );
				}
				finished = computeGlobalTransforms( targetHyperSlice );
			}
			Utils.collectFutures( executorService, futures );
		}

		private void computeLocalTransform( int step, long finalSlice )
		{
			final Vector< PointMatch > candidates =
					FloatArray2DSIFT.createMatches(
							hyperSliceIndexToSIFTFeatures.get( finalSlice ),
							hyperSliceIndexToSIFTFeatures.get( finalSlice - step ),
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
				System.out.println( "Could not compute local transform for slice: " + finalSlice );
			}

			if ( modelFound )
				hyperSliceIndexToLocalTransform.put( finalSlice, getAffineTransform( model ) );
			else
				hyperSliceIndexToLocalTransform.put( finalSlice, new AffineGetNull() );
		}

		private AffineGet getAffineTransform( AbstractAffineModel2D< ? > model )
		{
			final AffineTransform affine = model.createAffine();
			final double[] array = new double[ 6 ];
			affine.getMatrix( array );

			if ( numHyperSliceDimensions == 2 )
			{
				final AffineTransform2D affineTransform2D = new AffineTransform2D();
				affineTransform2D.set(
						array[ 0 ], array[ 1 ], array[ 4 ],
						array[ 2 ], array[ 3 ], array[ 5 ] );

				return affineTransform2D;
			}
			else if ( numHyperSliceDimensions == 3 )
			{
				final AffineTransform3D affineTransform3D = new AffineTransform3D();
				affineTransform3D.set(
						array[ 0 ], array[ 1 ], 0.0, array[ 4 ],
						array[ 2 ], array[ 3 ], 0.0, array[ 5 ],
						0.0,        0.0,        1.0, 0.0);

				return affineTransform3D;
			}
			else
			{
				throw new UnsupportedOperationException( "SIFT Registration: Unsupported hyperslice dimension: " + numHyperSliceDimensions );
			}
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

		private ImageProcessor getImageProcessor( long hyperSliceIndex )
		{
			final RandomAccessibleInterval< R > hyperSlice = hyperslices.get( ( int ) hyperSliceIndex );
			RandomAccessibleInterval< R > hyperSliceCrop = cropHyperSlice( hyperSlice );
			final ImagePlus wrap = ImageJFunctions.wrap( hyperSliceCrop, "" + hyperSliceIndex );
			final ImageProcessor processor = wrap.getProcessor();

			return processor;
		}

		private RandomAccessibleInterval< R > cropHyperSlice( RandomAccessibleInterval< R > hyperSlice )
		{
			RandomAccessibleInterval< R > hyperSliceCrop;
			if ( hyperSliceInterval != null )
				hyperSliceCrop = Views.dropSingletonDimensions(
					Views.interval( hyperSlice, hyperSliceInterval ) );
			else
				hyperSliceCrop = hyperSlice;
			return hyperSliceCrop;
		}
	}

}
