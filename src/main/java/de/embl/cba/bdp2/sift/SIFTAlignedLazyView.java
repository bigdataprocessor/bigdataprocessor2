package de.embl.cba.bdp2.sift;

import net.imglib2.*;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.realtransform.RealViews;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SIFTAlignedLazyView < R extends RealType< R > & NativeType< R > >
		extends AbstractInterval implements RandomAccessibleInterval< R >, View
{
	private final long referenceSlice;
	private final RandomAccessibleInterval< R > rai3D;
	protected final int numThreads;

	public SIFTAlignedLazyView( RandomAccessibleInterval< R > rai3D,
								long referenceSlice,
								int numThreads )
	{
		super( rai3D.numDimensions() );

		this.referenceSlice = referenceSlice;
		this.rai3D = rai3D;
		this.numThreads = numThreads;
	}

	@Override
	public RandomAccess< R > randomAccess()
	{
		return new SIFTAlignedRandomAccess( rai3D, referenceSlice, null, numThreads );
	}

	@Override
	public RandomAccess< R > randomAccess( Interval interval )
	{
		return new SIFTAlignedRandomAccess( rai3D, referenceSlice, interval, numThreads );
	}


	public static final class SIFTAlignedRandomAccess < R extends RealType< R > & NativeType< R > >
			implements RandomAccess< R >
	{
		private final int numDimensions;

		private final int numSliceDimensions;
		private final RandomAccessibleInterval< R > inputRai3D;

		private int sliceIndex;

		private final long referenceSlice;

		private final long[] tmpLong;

		private final int[] tmpInt;

		private RandomAccess< R > sliceAccess;

		private Interval interval;
		private Map< Long, RandomAccess< R > > sliceToAccess;
		private final SliceRegistrationSIFT< R > sift;
		private boolean sliceReady;

		public SIFTAlignedRandomAccess(
				final RandomAccessibleInterval< R > rai3D,
				long referenceSlice,
				final Interval interval,
				int numThreads )
		{
			this.referenceSlice = referenceSlice;
			this.inputRai3D = rai3D;
			this.interval = interval;
			numDimensions = rai3D.numDimensions();
			numSliceDimensions = numDimensions - 1;
			sliceIndex = 0;

			sliceToAccess = new ConcurrentHashMap<>(  );

			tmpLong = new long[ numSliceDimensions ];
			tmpInt = new int[ numSliceDimensions ];

			sift = new SliceRegistrationSIFT<>( rai3D, referenceSlice, numThreads );
			sift.computeAllTransforms();

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

		private void setSliceAccess( final long sliceIndex )
		{
			// TODO: handle the interval case!

			if ( sliceToAccess.containsKey( sliceIndex ) )
			{
				setTransformedAccess( sliceIndex );
				return;
			}

			final RandomAccessibleInterval< R > sliceView = Views.interval(
					inputRai3D,
					new long[]{ inputRai3D.min( 0 ), inputRai3D.min( 1 ), sliceIndex },
					new long[]{ inputRai3D.max( 0 ), inputRai3D.max( 1 ), sliceIndex }
			);

			if ( sift.getGlobalTransform( sliceIndex ) == null )
			{
				sliceReady = false;
				sliceAccess = sliceView.randomAccess(); // TODO: does this make sense?
				return;
			}

			final IntervalView transformed = getTransformedView( sliceIndex, sliceView );

			sliceToAccess.put( sliceIndex, transformed.randomAccess() );

			setTransformedAccess( sliceIndex );

		}

		private void setTransformedAccess( long sliceIndex )
		{
			sliceAccess = sliceToAccess.get( sliceIndex );
			sliceReady = true;
		}

		private IntervalView getTransformedView( long sliceIndex, RandomAccessibleInterval< R > sliceView )
		{
			RealRandomAccessible rra =
						Views.interpolate( Views.extendZero( sliceView ), new NLinearInterpolatorFactory<>() );

			return Views.interval(
						Views.raster(
								RealViews.transform( rra, sift.getGlobalTransform( sliceIndex ) )
						), sliceView );
		}

		private void setSlice( final long i )
		{
			setSliceAccess( i );
		}

		@Override
		public R get()
		{
			if ( sliceReady )
			{
				return sliceAccess.get();
			}
			else
			{
				final R r = sliceAccess.get();
				r.setZero();
				// TODO: make use of Volatile here!
				return r;
			}
		}

		@Override
		public SIFTAlignedLazyView.SIFTAlignedRandomAccess< R > copy()
		{
			return this; // TODO
		}

		@Override
		public SIFTAlignedLazyView.SIFTAlignedRandomAccess< R > copyRandomAccess()
		{
			return copy();
		}
	}

}
