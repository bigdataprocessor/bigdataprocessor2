package de.embl.cba.bdp2.registration;

import net.imglib2.*;
import net.imglib2.interpolation.randomaccess.ClampingNLinearInterpolatorFactory;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
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

		private int sliceIndex;

		private final long[] tmpLong;

		private final int[] tmpInt;

		private RandomAccess< R > sliceAccess;

		private Interval interval;

		private Map< Integer, RandomAccess< R > > sliceToAccess;

		private final HypersliceTransformProvider transformProvider;

		private final List< RandomAccessibleInterval< R > > hyperslices;

		private boolean sliceReady;
		private int previousSliceIndex;

		public TransformedRandomAccess(
				final List< RandomAccessibleInterval< R > > hyperslices,
				final HypersliceTransformProvider transformProvider,
				final Interval interval )
		{
			this.interval = interval;
			numDimensions = hyperslices.get( 0 ).numDimensions() + 1;
			numSliceDimensions = numDimensions - 1;

			sliceIndex = 0;
			sliceAccess = hyperslices.get( 0 ).randomAccess();
			sliceReady = false;

			sliceToAccess = new ConcurrentHashMap<>(  );

			tmpLong = new long[ numSliceDimensions ];
			tmpInt = new int[ numSliceDimensions ];

			this.transformProvider = transformProvider;
			this.hyperslices = hyperslices;
		}

		@Override
		public void localize( final int[] position )
		{
			for ( int d = 0; d < numSliceDimensions; ++d )
				position[ d ] = sliceAccess.getIntPosition( d );
			position[ numSliceDimensions ] = sliceIndex;
		}

		@Override
		public void localize( final long[] position )
		{
			for ( int d = 0; d < numSliceDimensions; ++d )
				position[ d ] = sliceAccess.getLongPosition( d );
			position[ numSliceDimensions ] = sliceIndex;
		}

		@Override
		public int getIntPosition( final int d )
		{
			return ( d < numSliceDimensions ) ? sliceAccess.getIntPosition( d ) : sliceIndex;
		}

		@Override
		public long getLongPosition( final int d )
		{
			return ( d < numSliceDimensions ) ? sliceAccess.getLongPosition( d ) : sliceIndex;
		}

		@Override
		public void localize( final float[] position )
		{
			for ( int d = 0; d < numSliceDimensions; ++d )
				position[ d ] = sliceAccess.getLongPosition( d );
			position[ numSliceDimensions ] = sliceIndex;
		}

		@Override
		public void localize( final double[] position )
		{
			for ( int d = 0; d < numSliceDimensions; ++d )
				position[ d ] = sliceAccess.getLongPosition( d );
			position[ numSliceDimensions ] = sliceIndex;
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
				sliceAccess.fwd( d );
			else
				setSliceAccess( sliceIndex + 1 );
		}

		@Override
		public void bck( final int d )
		{
			if ( d < numSliceDimensions )
				sliceAccess.bck( d );
			else
				setSliceAccess( sliceIndex - 1 );
		}

		@Override
		public void move( final int distance, final int d )
		{
			if ( d < numSliceDimensions )
				sliceAccess.move( distance, d );
			else
				setSliceAccess( sliceIndex + distance );
		}

		@Override
		public void move( final long distance, final int d )
		{
			if ( d < numSliceDimensions )
				sliceAccess.move( distance, d );
			else
				setSliceAccess( sliceIndex + ( int ) distance );
		}

		@Override
		public void move( final Localizable distance )
		{
			for ( int d = 0; d < numSliceDimensions; ++d )
				sliceAccess.move( distance.getLongPosition( d ), d );
			setSliceAccess( sliceIndex + distance.getIntPosition( numSliceDimensions ) );
		}

		@Override
		public void move( final int[] distance )
		{
			for ( int d = 0; d < numSliceDimensions; ++d )
				sliceAccess.move( distance[ d ], d );
			setSliceAccess( sliceIndex + distance[ numSliceDimensions ] );
		}

		@Override
		public void move( final long[] distance )
		{
			for ( int d = 0; d < numSliceDimensions; ++d )
				sliceAccess.move( distance[ d ], d );
			setSliceAccess( sliceIndex + ( int ) distance[ numSliceDimensions ] );
		}

		@Override
		public void setPosition( final Localizable position )
		{
			for ( int d = 0; d < numSliceDimensions; ++d )
				tmpLong[ d ] = position.getLongPosition( d );
			sliceAccess.setPosition( tmpLong );
			setSliceAccess( position.getIntPosition( numSliceDimensions ) );
		}

		@Override
		public void setPosition( final int[] position )
		{
			System.arraycopy( position, 0, tmpInt, 0, numSliceDimensions );
			sliceAccess.setPosition( tmpInt );
			setSliceAccess( position[ numSliceDimensions ] );
		}

		@Override
		public void setPosition( final long[] position )
		{
			System.arraycopy( position, 0, tmpLong, 0, numSliceDimensions );
			sliceAccess.setPosition( tmpLong );
			setSlice( position[ numSliceDimensions ] );
		}

		@Override
		public void setPosition( final int position, final int d )
		{
			if ( d < numSliceDimensions )
				sliceAccess.setPosition( position, d );
			else
				setSliceAccess( position );
		}

		@Override
		public void setPosition( final long position, final int d )
		{
			if ( d < numSliceDimensions )
				sliceAccess.setPosition( position, d );
			else
				setSlice( position );
		}

		private void setSliceAccess( final long requestedSliceIndex )
		{
			if ( requestedSliceIndex == sliceIndex )
				return;
			else
				changeSliceAccess( (int) requestedSliceIndex );
		}

		private synchronized void changeSliceAccess( int requestedSliceIndex )
		{
			// TODO: handle the interval case!

			sliceIndex = requestedSliceIndex;

			if ( sliceIndex < 0 || sliceIndex >= hyperslices.size() ) return;

			if ( sliceToAccess.containsKey( requestedSliceIndex ) )
			{
				sliceToAccess.get( requestedSliceIndex ).setPosition( sliceAccess );
				sliceAccess = sliceToAccess.get( requestedSliceIndex );
				return;
			}

			if ( transformProvider.getTransform( requestedSliceIndex ) == null )
			{
				if ( sliceAccess.get() instanceof Volatile )
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
					while ( transformProvider.getTransform( requestedSliceIndex ) == null )
						sleep();
				}
			}

			setTransformedSliceAccess( requestedSliceIndex,
					transformProvider.getTransform( requestedSliceIndex ) );

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
					hyperslices.get( requestedSliceIndex ) );

			final RandomAccess access = transformed.randomAccess();
			access.setPosition( sliceAccess );
			sliceAccess = access;
			sliceToAccess.put( requestedSliceIndex, access );
		}

		private IntervalView getTransformedView(
				AffineTransform transform,
				RandomAccessibleInterval< R > hyperslice )
		{
			RealRandomAccessible< R > rra =
						Views.interpolate( Views.extendZero( hyperslice ),
								new ClampingNLinearInterpolatorFactory<>() );

			final IntervalView< R > interval = Views.interval(
					Views.raster(
							RealViews.transform( rra, transform )
					), hyperslice );

			return interval;
		}

		private void setSlice( final long i )
		{
			setSliceAccess( i );
		}

		@Override
		public R get()
		{
			if ( sliceToAccess.containsKey( sliceIndex ) )
			{
				return sliceAccess.get();
			}
			else
			{
				final R r = sliceAccess.get();
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
