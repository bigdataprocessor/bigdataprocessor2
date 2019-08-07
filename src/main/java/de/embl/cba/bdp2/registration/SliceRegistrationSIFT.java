package de.embl.cba.bdp2.registration;

import de.embl.cba.bdp2.registration.AffineGetNull;
import de.embl.cba.bdp2.registration.HypersliceTransformProvider;
import ij.process.ImageProcessor;
import mpicbg.ij.SIFT;
import mpicbg.imagefeatures.Feature;
import mpicbg.imagefeatures.FloatArray2DSIFT;
import mpicbg.models.*;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.realtransform.AffineGet;
import net.imglib2.realtransform.AffineTransform2D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import java.awt.geom.AffineTransform;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

public class SliceRegistrationSIFT < R extends RealType< R > & NativeType< R > > implements HypersliceTransformProvider
{
	private final List< RandomAccessibleInterval< R > > hyperslices;
	private final long referenceSlice;
	private final Map< Long, AffineGet > sliceToLocalTransform;
	private final Map< Long, Boolean > sliceToLocalTransformIsBeingComputed;

	private final Map< Long, List< Feature > > sliceToFeatures;
	private final Map< Long, net.imglib2.realtransform.AffineTransform > sliceToGlobalTransform;

	private int numSliceDimensions;
	private final int numThreads;
	private int numSlices;


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
								  long referenceSlice,
								  int numThreads )
	{
		this.hyperslices = hyperslices;
		this.referenceSlice = referenceSlice;
		this.numThreads = numThreads;

		numSlices = hyperslices.size() - 1;

		numSliceDimensions = hyperslices.get( 0 ).numDimensions();
		sliceToFeatures = new ConcurrentHashMap<>( );

		sliceToLocalTransform = new ConcurrentHashMap< >( );
		sliceToLocalTransform.put( referenceSlice, new net.imglib2.realtransform.AffineTransform( numSliceDimensions ) );

		sliceToGlobalTransform = new ConcurrentHashMap< >( );
		sliceToGlobalTransform.put( referenceSlice, new net.imglib2.realtransform.AffineTransform( numSliceDimensions )  );

		sliceToLocalTransformIsBeingComputed = new ConcurrentHashMap< >( );;
	}

	public void computeTransformsUntilSlice( long slice )
	{
		new Thread( () -> computeSIFTFeatures( slice, numThreads ) ).start();
		computeTransforms( slice, numThreads );
	}

	public void computeAllTransforms( )
	{
		new Thread( () -> computeSIFTFeatures( 0, numThreads ) ).start();
		computeTransforms( 0, numThreads );

		new Thread( () -> computeSIFTFeatures( numSlices, numThreads ) ).start();
		computeTransforms( numSlices, numThreads );
	}


	@Override
	public net.imglib2.realtransform.AffineTransform getTransform( long slice )
	{
		if ( sliceToGlobalTransform.containsKey( slice ))
			return sliceToGlobalTransform.get( slice );
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
			for ( long slice = referenceSlice + step; slice != ( requestedSlice + step ); slice += step )
			{
				if ( sliceToLocalTransform.containsKey( slice ) ) continue;
				if ( sliceToLocalTransformIsBeingComputed.containsKey( slice ) ) continue;
				if ( ! sliceToFeatures.containsKey( slice ) ) continue;
				if ( ! sliceToFeatures.containsKey( slice - step  ) ) continue;

				sliceToLocalTransformIsBeingComputed.put( slice, true );

				final long finalSlice = slice;
				futures.add( executorService.submit( () -> computeLocalTransform( step, finalSlice ) ) );
			}

			for ( long slice = referenceSlice + step; ; slice += step )
			{
				if ( ! sliceToLocalTransform.containsKey( slice ) ) break;
				if( sliceToGlobalTransform.containsKey( slice ) ) continue;

				AffineGet currentLocal = getLocalTransform( slice );

				final net.imglib2.realtransform.AffineTransform previousGlobal = sliceToGlobalTransform.get( slice - step ).copy();

				final net.imglib2.realtransform.AffineTransform currentGlobal = previousGlobal.preConcatenate( currentLocal );
				sliceToGlobalTransform.put( slice, currentGlobal );

				System.out.println( "Transformation ready for slice: " + slice );
				if ( slice == requestedSlice )
				{
					finished = true;
					break;
				}
			}
		}

		// TODO: if there is an error before I might not catch it...

		collectFutures( executorService, futures );

	}

	private AffineGet getLocalTransform( long slice )
	{
		AffineGet currentLocal;
		if ( sliceToLocalTransform.get( slice ) instanceof AffineGetNull )
		{
			System.out.println( slice + ":" + sliceToLocalTransform.get( slice ).toString() );
			currentLocal = new net.imglib2.realtransform.AffineTransform( numSliceDimensions );
		}
		else
		{
			currentLocal = sliceToLocalTransform.get( slice ).copy();
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
		} catch ( final Exception e )
		{
			modelFound = false;
			System.err.println( e.getMessage() );
			System.out.println( "Could not compute local transform for slice: " + finalSlice );
		}

		if ( modelFound )
			sliceToLocalTransform.put( finalSlice, getAffineTransform( model ) );
		else
			sliceToLocalTransform.put( finalSlice, new AffineGetNull() );
	}

	private AffineGet getAffineTransform( AbstractAffineModel2D< ? > model )
	{
		final AffineTransform affine = model.createAffine();
		final double[] array = new double[ 6 ];
		affine.getMatrix( array );
		final AffineTransform2D affineTransform2D = new AffineTransform2D();
		affineTransform2D.set(
				array[ 0 ],
				array[ 1 ],
				array[ 4 ],
				array[ 2 ],
				array[ 3 ],
				array[ 5 ]);

		return affineTransform2D;
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

	private void computeSIFTFeatures( final long requestedSlice, int numThreads )
	{
		final ExecutorService executorService = Executors.newFixedThreadPool( numThreads );
		final ArrayList< Future > futures = new ArrayList<>();

		final int step = getStep( requestedSlice );

		for ( long slice = referenceSlice; slice != ( requestedSlice + step ) ; slice += step )
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

	private ImageProcessor getImageProcessor( long slice )
	{
		return ImageJFunctions.wrap( hyperslices.get( (int) slice ), "slice" ).getProcessor();
	}

	private int getStep( long requestedSlice )
	{
		int step;
		if ( requestedSlice < referenceSlice ) step = -1;
		else step = +1;
		return step;
	}

}
