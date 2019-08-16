package de.embl.cba.bdp2.registration;

import net.imglib2.*;
import net.imglib2.interpolation.randomaccess.ClampingNLinearInterpolatorFactory;
import net.imglib2.realtransform.AffineTransform;
import net.imglib2.realtransform.RealViews;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TransformedStackView < R >
		extends AbstractInterval implements RandomAccessibleInterval< R >, View
{
	private final List< RandomAccessibleInterval< R > > hyperslices;
	private final HypersliceTransformProvider transformProvider;

	public TransformedStackView( final List< RandomAccessibleInterval< R > > hyperslices,
								 final HypersliceTransformProvider transformProvider )
	{
		super( hyperslices.get( 0 ).numDimensions() + 1 );
		setInterval( hyperslices );
		this.hyperslices = hyperslices;
		this.transformProvider = transformProvider;
	}

	public List< RandomAccessibleInterval< R > > getHyperslices()
	{
		return hyperslices;
	}

	public HypersliceTransformProvider getTransformProvider()
	{
		return transformProvider;
	}

	private void setInterval( List< RandomAccessibleInterval< R > > hyperslices )
	{
		for ( int d = 0; d < n - 1; ++d )
		{
			min[ d ] = hyperslices.get( 0 ).min( d );
			max[ d ] = hyperslices.get( 0 ).max( d );
		}
		min[ n - 1 ] = 0;
		max[ n - 1 ] = hyperslices.size() - 1;
	}

	@Override
	public RandomAccess< R > randomAccess()
	{
		return new TransformedRandomAccess( hyperslices, transformProvider, null );
	}

	@Override
	public RandomAccess< R > randomAccess( Interval interval )
	{
		return new TransformedRandomAccess( hyperslices, transformProvider, interval );
	}

	public static final class TransformedRandomAccess < R extends RealType< R > & NativeType< R > >
			implements RandomAccess< R >
	{
		private final int numDimensions;

		private final int numSliceDimensions;

		private int currentHyperSliceIndex;

		private final long[] tmpLong;

		private final int[] tmpInt;

		private RandomAccess< R > hyperSliceAccess;

		private Interval interval;

		private Map< Integer, RandomAccess< R > > hyperSliceIndexToAccess;

		private final HypersliceTransformProvider transformProvider;

		private final List< RandomAccessibleInterval< R > > hyperSlices;

		private boolean sliceReady;
		private int previousSliceIndex;

		public TransformedRandomAccess(
				final List< RandomAccessibleInterval< R > > hyperSlices,
				final HypersliceTransformProvider transformProvider,
				final Interval interval )
		{
			this.interval = interval;
			numDimensions = hyperSlices.get( 0 ).numDimensions() + 1;
			numSliceDimensions = numDimensions - 1;

			currentHyperSliceIndex = -1;
			hyperSliceAccess = hyperSlices.get( 0 ).randomAccess();
			sliceReady = false;

			hyperSliceIndexToAccess = new ConcurrentHashMap<>(  );

			tmpLong = new long[ numSliceDimensions ];
			tmpInt = new int[ numSliceDimensions ];

			this.transformProvider = transformProvider;
			this.hyperSlices = hyperSlices;
		}

		@Override
		public void localize( final int[] position )
		{
			for ( int d = 0; d < numSliceDimensions; ++d )
				position[ d ] = hyperSliceAccess.getIntPosition( d );
			position[ numSliceDimensions ] = currentHyperSliceIndex;
		}

		@Override
		public void localize( final long[] position )
		{
			for ( int d = 0; d < numSliceDimensions; ++d )
				position[ d ] = hyperSliceAccess.getLongPosition( d );
			position[ numSliceDimensions ] = currentHyperSliceIndex;
		}

		@Override
		public int getIntPosition( final int d )
		{
			return ( d < numSliceDimensions ) ? hyperSliceAccess.getIntPosition( d ) : currentHyperSliceIndex;
		}

		@Override
		public long getLongPosition( final int d )
		{
			return ( d < numSliceDimensions ) ? hyperSliceAccess.getLongPosition( d ) : currentHyperSliceIndex;
		}

		@Override
		public void localize( final float[] position )
		{
			for ( int d = 0; d < numSliceDimensions; ++d )
				position[ d ] = hyperSliceAccess.getLongPosition( d );
			position[ numSliceDimensions ] = currentHyperSliceIndex;
		}

		@Override
		public void localize( final double[] position )
		{
			for ( int d = 0; d < numSliceDimensions; ++d )
				position[ d ] = hyperSliceAccess.getLongPosition( d );
			position[ numSliceDimensions ] = currentHyperSliceIndex;
		}

		@Override
		public float getFloatPosition( final int d )
		{
			return getLongPosition( d );
		}

		@Override
		public double getDoublePosition( final int d )
		{
			return getLongPosition( d );
		}

		@Override
		public int numDimensions()
		{
			return numDimensions;
		}

		@Override
		public void fwd( final int d )
		{
			if ( d < numSliceDimensions )
				hyperSliceAccess.fwd( d );
			else
				setHyperSliceAccess( currentHyperSliceIndex + 1 );
		}

		@Override
		public void bck( final int d )
		{
			if ( d < numSliceDimensions )
				hyperSliceAccess.bck( d );
			else
				setHyperSliceAccess( currentHyperSliceIndex - 1 );
		}

		@Override
		public void move( final int distance, final int d )
		{
			if ( d < numSliceDimensions )
				hyperSliceAccess.move( distance, d );
			else
				setHyperSliceAccess( currentHyperSliceIndex + distance );
		}

		@Override
		public void move( final long distance, final int d )
		{
			if ( d < numSliceDimensions )
				hyperSliceAccess.move( distance, d );
			else
				setHyperSliceAccess( currentHyperSliceIndex + ( int ) distance );
		}

		@Override
		public void move( final Localizable distance )
		{
			for ( int d = 0; d < numSliceDimensions; ++d )
				hyperSliceAccess.move( distance.getLongPosition( d ), d );
			setHyperSliceAccess( currentHyperSliceIndex + distance.getIntPosition( numSliceDimensions ) );
		}

		@Override
		public void move( final int[] distance )
		{
			for ( int d = 0; d < numSliceDimensions; ++d )
				hyperSliceAccess.move( distance[ d ], d );
			setHyperSliceAccess( currentHyperSliceIndex + distance[ numSliceDimensions ] );
		}

		@Override
		public void move( final long[] distance )
		{
			for ( int d = 0; d < numSliceDimensions; ++d )
				hyperSliceAccess.move( distance[ d ], d );
			setHyperSliceAccess( currentHyperSliceIndex + ( int ) distance[ numSliceDimensions ] );
		}

		@Override
		public void setPosition( final Localizable position )
		{
			for ( int d = 0; d < numSliceDimensions; ++d )
				tmpLong[ d ] = position.getLongPosition( d );
			hyperSliceAccess.setPosition( tmpLong );
			setHyperSliceAccess( position.getIntPosition( numSliceDimensions ) );
		}

		@Override
		public void setPosition( final int[] position )
		{
			System.arraycopy( position, 0, tmpInt, 0, numSliceDimensions );
			hyperSliceAccess.setPosition( tmpInt );
			setHyperSliceAccess( position[ numSliceDimensions ] );
		}

		@Override
		public void setPosition( final long[] position )
		{
			System.arraycopy( position, 0, tmpLong, 0, numSliceDimensions );
			hyperSliceAccess.setPosition( tmpLong );
			setSlice( position[ numSliceDimensions ] );
		}

		@Override
		public void setPosition( final int position, final int d )
		{
			if ( d < numSliceDimensions )
				hyperSliceAccess.setPosition( position, d );
			else
				setHyperSliceAccess( position );
		}

		@Override
		public void setPosition( final long position, final int d )
		{
			if ( d < numSliceDimensions )
				hyperSliceAccess.setPosition( position, d );
			else
				setSlice( position );
		}

		private void setHyperSliceAccess( final long requestedHyperSliceIndex )
		{
			if ( requestedHyperSliceIndex == currentHyperSliceIndex )
				return;
			else
				changeSliceAccess( (int) requestedHyperSliceIndex );
		}

		private synchronized void changeSliceAccess( int requestedHyperSliceIndex )
		{
			// TODO: handle the interval case!

			currentHyperSliceIndex = requestedHyperSliceIndex;

			if ( currentHyperSliceIndex < 0 || currentHyperSliceIndex >= hyperSlices.size() ) return;

			if ( hyperSliceIndexToAccess.containsKey( requestedHyperSliceIndex ) )
			{
				hyperSliceIndexToAccess.get( requestedHyperSliceIndex ).setPosition( hyperSliceAccess );
				hyperSliceAccess = hyperSliceIndexToAccess.get( requestedHyperSliceIndex );
				return;
			}

			if ( transformProvider.getTransform( requestedHyperSliceIndex ) == null )
			{
				if ( hyperSliceAccess.get() instanceof Volatile )
				{
					/**
					 * Even though we cannot compute the data for this slice yet,
					 * we can return, because in below get() method of this
					 * RandomAccess we can set the pixels to be invalid.
					 */
					return;
				}
				else
				{
					/**
					 * The user expects valid data from the get() method.
					 * Thus we need to wait until this slice can be computed.
					 */
					while ( transformProvider.getTransform( requestedHyperSliceIndex ) == null )
						sleep();
				}
			}

			setTransformedSliceAccess( requestedHyperSliceIndex,
					transformProvider.getTransform( requestedHyperSliceIndex ) );
		}

		private static void sleep()
		{
			try
			{
				Thread.sleep( 100 );
			} catch ( InterruptedException e )
			{
				e.printStackTrace();
			}
		}

		private void setTransformedSliceAccess( int requestedSliceIndex,
												AffineTransform transform )
		{
			final IntervalView transformed = getTransformedView(
					transform,
					hyperSlices.get( requestedSliceIndex ) );

			final RandomAccess access = transformed.randomAccess();
			access.setPosition( hyperSliceAccess );
			hyperSliceAccess = access;
			hyperSliceIndexToAccess.put( requestedSliceIndex, access );
		}

		private IntervalView getTransformedView(
				AffineTransform transform,
				RandomAccessibleInterval< R > hyperslice )
		{
			RealRandomAccessible< R > rra =
						Views.interpolate( Views.extendZero( hyperslice ),
								new ClampingNLinearInterpolatorFactory<>() );

			final IntervalView< R > interval = Views.interval( Views.raster( RealViews.transform( rra, transform ) ), hyperslice );

			return interval;
		}

		private void setSlice( final long i )
		{
			setHyperSliceAccess( i );
		}

		@Override
		public R get()
		{
			if ( hyperSliceIndexToAccess.containsKey( currentHyperSliceIndex ) )
			{
				return hyperSliceAccess.get();
			}
			else
			{
				final R r = hyperSliceAccess.get().copy();
				r.setZero();

				if ( r instanceof Volatile )
					( ( Volatile ) r ).setValid( false );

				return r;
			}
		}

		@Override
		public TransformedRandomAccess< R > copy()
		{
			return this; // TODO
		}

		@Override
		public TransformedRandomAccess< R > copyRandomAccess()
		{
			return copy();
		}
	}

}
