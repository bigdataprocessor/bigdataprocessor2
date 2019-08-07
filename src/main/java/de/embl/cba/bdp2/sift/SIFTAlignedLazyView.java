package de.embl.cba.bdp2.sift;

import net.imglib2.*;

public class SIFTAlignedLazyView< T > extends AbstractInterval implements RandomAccessibleInterval< T >, View
{
	public SIFTAlignedLazyView( RandomAccessibleInterval< T > rai3D )
	{
		super( rai3D.numDimensions() );
	}

	@Override
	public RandomAccess< T > randomAccess()
	{
		return null;
	}

	@Override
	public RandomAccess< T > randomAccess( Interval interval )
	{
		return null;
	}


	public static final class SIFTAlignedRandomAccess< T > implements RandomAccess< T >
	{
		private final int n;

		private final int sliceDimension;

		private int sliceIndex;

		private final long[] tmpLong;

		private final int[] tmpInt;

		private RandomAccess< T > sliceAccess;

		private RandomAccessibleInterval< T >[] slices;
		private Interval interval;

		public SIFTAlignedRandomAccess( final RandomAccessibleInterval< T >[] slices )
		{
			this( slices, null );
		}

		@SuppressWarnings( "unchecked" )
		public SIFTAlignedRandomAccess(
				final RandomAccessibleInterval< T >[] slices,
				final Interval interval )
		{
			this.slices = slices;
			this.interval = interval;
			n = slices[ 0 ].numDimensions() + 1;
			sliceDimension = n - 1;
			sliceIndex = 0;
			tmpLong = new long[ sliceDimension ];
			tmpInt = new int[ sliceDimension ];
		}

		@Override
		public void localize( final int[] position )
		{
			for ( int d = 0; d < sliceDimension; ++d )
				position[ d ] = sliceAccess.getIntPosition( d );
			position[ sliceDimension ] = sliceIndex;
		}

		@Override
		public void localize( final long[] position )
		{
			for ( int d = 0; d < sliceDimension; ++d )
				position[ d ] = sliceAccess.getLongPosition( d );
			position[ sliceDimension ] = sliceIndex;
		}

		@Override
		public int getIntPosition( final int d )
		{
			return ( d < sliceDimension ) ? sliceAccess.getIntPosition( d ) : sliceIndex;
		}

		@Override
		public long getLongPosition( final int d )
		{
			return ( d < sliceDimension ) ? sliceAccess.getLongPosition( d ) : sliceIndex;
		}

		@Override
		public void localize( final float[] position )
		{
			for ( int d = 0; d < sliceDimension; ++d )
				position[ d ] = sliceAccess.getLongPosition( d );
			position[ sliceDimension ] = sliceIndex;
		}

		@Override
		public void localize( final double[] position )
		{
			for ( int d = 0; d < sliceDimension; ++d )
				position[ d ] = sliceAccess.getLongPosition( d );
			position[ sliceDimension ] = sliceIndex;
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
			return n;
		}

		@Override
		public void fwd( final int d )
		{
			if ( d < sliceDimension )
				sliceAccess.fwd( d );
			else
				setSliceIndex( sliceIndex + 1 );
		}

		@Override
		public void bck( final int d )
		{
			if ( d < sliceDimension )
				sliceAccess.bck( d );
			else
				setSliceIndex( sliceIndex - 1 );
		}

		@Override
		public void move( final int distance, final int d )
		{
			if ( d < sliceDimension )
				sliceAccess.move( distance, d );
			else
				setSliceIndex( sliceIndex + distance );
		}

		@Override
		public void move( final long distance, final int d )
		{
			if ( d < sliceDimension )
				sliceAccess.move( distance, d );
			else
				setSliceIndex( sliceIndex + ( int ) distance );
		}

		@Override
		public void move( final Localizable distance )
		{
			for ( int d = 0; d < sliceDimension; ++d )
				sliceAccess.move( distance.getLongPosition( d ), d );
			setSliceIndex( sliceIndex + distance.getIntPosition( sliceDimension ) );
		}

		@Override
		public void move( final int[] distance )
		{
			for ( int d = 0; d < sliceDimension; ++d )
				sliceAccess.move( distance[ d ], d );
			setSliceIndex( sliceIndex + distance[ sliceDimension ] );
		}

		@Override
		public void move( final long[] distance )
		{
			for ( int d = 0; d < sliceDimension; ++d )
				sliceAccess.move( distance[ d ], d );
			setSliceIndex( sliceIndex + ( int ) distance[ sliceDimension ] );
		}

		@Override
		public void setPosition( final Localizable position )
		{
			for ( int d = 0; d < sliceDimension; ++d )
				tmpLong[ d ] = position.getLongPosition( d );
			sliceAccess.setPosition( tmpLong );
			setSliceIndex( position.getIntPosition( sliceDimension ) );
		}

		@Override
		public void setPosition( final int[] position )
		{
			System.arraycopy( position, 0, tmpInt, 0, sliceDimension );
			sliceAccess.setPosition( tmpInt );
			setSliceIndex( position[ sliceDimension ] );
		}

		@Override
		public void setPosition( final long[] position )
		{
			System.arraycopy( position, 0, tmpLong, 0, sliceDimension );
			sliceAccess.setPosition( tmpLong );
			setSlice( position[ sliceDimension ] );
		}

		@Override
		public void setPosition( final int position, final int d )
		{
			if ( d < sliceDimension )
				sliceAccess.setPosition( position, d );
			else
				setSliceIndex( position );
		}

		@Override
		public void setPosition( final long position, final int d )
		{
			if ( d < sliceDimension )
				sliceAccess.setPosition( position, d );
			else
				setSlice( position );
		}

		private void setSliceIndex( final int i )
		{
//
//			final long[] smin = new long[ sliceDimension ];
//			final long[] smax = new long[ sliceDimension ];
//			for ( int d = 0; d < sliceDimension; ++d )
//			{
//				smin[ d ] = interval.min( d );
//				smax[ d ] = interval.max( d );
//			}
//			final Interval sliceInterval = new FinalInterval( smin, smax );
//			for ( int i = 0; i < slices.length; ++i )
//				sliceAccesses[ i ] = slices[ i ].randomAccess( sliceInterval );
//
//			if ( i != sliceIndex )
//			{
//				sliceIndex = i;
//
//				if ( sliceIndex >= 0 && sliceIndex < sliceAccesses.length )
//				{
//					sliceAccesses[ sliceIndex ].setPosition( sliceAccess );
//					sliceAccess = sliceAccesses[ sliceIndex ];
//
//					final IntervalView< T > interval =
//							Views.interval( slices[ i ], new long[]{ 0, 0, 0 }, new long[]{ 10, 10, 10 } );
//
//					sliceAccess = interval.randomAccess();
//
//				}
//			}
		}

		private void setSlice( final long i )
		{
			setSliceIndex( ( int ) i );
		}

		@Override
		public T get()
		{
			return sliceAccess.get();
		}

		@Override
		public SIFTAlignedLazyView.SIFTAlignedRandomAccess< T > copy()
		{
			return this; // TODO
		}

		@Override
		public SIFTAlignedLazyView.SIFTAlignedRandomAccess< T > copyRandomAccess()
		{
			return copy();
		}
	}

}
