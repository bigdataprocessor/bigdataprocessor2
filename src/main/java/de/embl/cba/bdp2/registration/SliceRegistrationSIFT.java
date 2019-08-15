package de.embl.cba.bdp2.registration;

import de.embl.cba.bdp2.progress.ProgressListener;
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
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import java.awt.geom.AffineTransform;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

public class SliceRegistrationSIFT < R extends RealType< R > & NativeType< R > > implements HypersliceTransformProvider
{
	private final List< RandomAccessibleInterval< R > > hyperslices;
	private final long referenceHyperSliceIndex;
	private final Map< Long, AffineGet > hyperSliceIndexToLocalTransform;
	private final Map< Long, Boolean > hyperSliceTransformIsBeingComputed;

	private final Map< Long, List< Feature > > sliceToFeatures;
	private final Map< Long, net.imglib2.realtransform.AffineTransform > hyperSliceToGlobalTransform;
	private final FinalInterval hyperSliceInterval;

	private int numHyperSliceDimensions;
	private final int numThreads;
	private int numHyperSlices;

	private ProgressListener progressListener;
	private int totalTransforms;
	private int countTransforms = 0;



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


	public SliceRegistrationSIFT( List< RandomAccessibleInterval< R > > hyperslices ,
								  long referenceHyperSliceIndex,
								  int numThreads )
	{
		this( hyperslices, referenceHyperSliceIndex, null, numThreads );
	}

	public SliceRegistrationSIFT(
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

		sliceToFeatures = new ConcurrentHashMap<>( );

		hyperSliceIndexToLocalTransform = new ConcurrentHashMap< >( );
		hyperSliceIndexToLocalTransform.put( referenceHyperSliceIndex, new net.imglib2.realtransform.AffineTransform( numHyperSliceDimensions ) );

		hyperSliceToGlobalTransform = new ConcurrentHashMap< >( );
		hyperSliceToGlobalTransform.put( referenceHyperSliceIndex, new net.imglib2.realtransform.AffineTransform( numHyperSliceDimensions )  );

		hyperSliceTransformIsBeingComputed = new ConcurrentHashMap< >( );

		p.sift.initialSigma = 1.0F;

	}


	public void computeTransformsUntilSlice( long slice )
	{
		totalTransforms = ( int ) Math.abs( referenceHyperSliceIndex - slice );
		new Thread( () -> computeFeatures( slice, numThreads ) ).start();
		computeTransforms( slice, numThreads );
	}

	public void computeAllTransforms( )
	{
		totalTransforms = numHyperSlices - 1; // reference slice needs no processing

		new Thread( () -> computeFeatures( 0, numThreads ) ).start();
		computeTransforms( 0, numThreads );

		new Thread( () -> computeFeatures( numHyperSlices - 1, numThreads ) ).start();
		computeTransforms( numHyperSlices, numThreads );
	}

	public void setProgressListener( ProgressListener progressListener )
	{
		this.progressListener = progressListener;
	}


	@Override
	public net.imglib2.realtransform.AffineTransform getTransform( long hyperSliceIndex )
	{
		if ( hyperSliceToGlobalTransform.containsKey( hyperSliceIndex ))
			return hyperSliceToGlobalTransform.get( hyperSliceIndex );
		else
			return null;
	}

	private void computeTransforms( final long requestedSlice, int numThreads )
	{
		final ExecutorService executorService = Executors.newFixedThreadPool( numThreads );
		final ArrayList< Future > futures = new ArrayList<>();

		final int step = getStep( requestedSlice );

		boolean finished = false;

		while( ! finished )
		{
			for ( long hyperSlice = referenceHyperSliceIndex + step;
				  hyperSlice != ( requestedSlice + step );
				  hyperSlice += step )
			{
				if ( hyperSliceIndexToLocalTransform.containsKey( hyperSlice ) ) continue;
				if ( hyperSliceTransformIsBeingComputed.containsKey( hyperSlice ) ) continue;
				if ( ! sliceToFeatures.containsKey( hyperSlice ) ) continue;
				if ( ! sliceToFeatures.containsKey( hyperSlice - step  ) ) continue;

				hyperSliceTransformIsBeingComputed.put( hyperSlice, true );

				final long finalSlice = hyperSlice;
				futures.add( executorService.submit( () -> computeLocalTransform( step, finalSlice ) ) );
			}

			for ( long hyperSlice = referenceHyperSliceIndex + step; ; hyperSlice += step )
			{
				if ( ! hyperSliceIndexToLocalTransform.containsKey( hyperSlice ) ) break;
				if( hyperSliceToGlobalTransform.containsKey( hyperSlice ) ) continue;

				AffineGet currentLocalTransform = getLocalTransform( hyperSlice );

				final net.imglib2.realtransform.AffineTransform previousGlobal =
						hyperSliceToGlobalTransform.get( hyperSlice - step ).copy();

				final net.imglib2.realtransform.AffineTransform currentGlobal =
						previousGlobal.preConcatenate( currentLocalTransform );

				hyperSliceToGlobalTransform.put( hyperSlice, currentGlobal );

				updateProgress();

				if ( hyperSlice == requestedSlice )
				{
					finished = true;
					break;
				}
			}
		}

		// TODO: if there is an error before I might not catch it...

		collectFutures( executorService, futures );

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
		{
			currentLocal = new net.imglib2.realtransform.AffineTransform( numHyperSliceDimensions );
		}
		else
		{
			currentLocal = hyperSliceIndexToLocalTransform.get( slice ).copy();
		}
		return currentLocal;
	}

	private void computeLocalTransform( int step, long finalSlice )
	{
		final Vector< PointMatch > candidates =
				FloatArray2DSIFT.createMatches(
						sliceToFeatures.get( finalSlice ),
						sliceToFeatures.get( finalSlice - step ),
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


	/**
	 * Computes features for all hyper-slices between the reference hyper-slice
	 * and the target hyper-slice.
	 *
	 * @param targetHyperSlice
	 * @param numThreads
	 */
	private void computeFeatures( final long targetHyperSlice, int numThreads )
	{
		final ExecutorService executorService = Executors.newFixedThreadPool( numThreads );
		final ArrayList< Future > futures = new ArrayList<>();

		final int step = getStep( targetHyperSlice );

		for ( long hyperSlice = referenceHyperSliceIndex; hyperSlice != ( targetHyperSlice + step ) ; hyperSlice += step )
		{
			if ( sliceToFeatures.containsKey( hyperSlice ) ) continue;

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
					sliceToFeatures.put( currentHyperSliceIndex, features );
				}
			} ) );
		}

		collectFutures( executorService, futures );
	}

	private ImageProcessor getImageProcessor( long hyperSliceIndex )
	{
		final RandomAccessibleInterval< R > hyperSlice = hyperslices.get( ( int ) hyperSliceIndex );

		final RandomAccessibleInterval< R > hyperSlicePlane = Views.dropSingletonDimensions(
				Views.interval( hyperSlice, this.hyperSliceInterval ) );

		final ImagePlus wrap = ImageJFunctions.wrap( hyperSlicePlane, "" + hyperSliceIndex );

		final ImageProcessor processor = wrap.getProcessor();
//		processor.multiply( 100 );
		wrap.show();

		return processor;
	}

	private int getStep( long requestedSlice )
	{
		int step;
		if ( requestedSlice < referenceHyperSliceIndex ) step = -1;
		else step = +1;
		return step;
	}

}
