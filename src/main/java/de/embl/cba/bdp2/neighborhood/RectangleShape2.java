package de.embl.cba.bdp2.neighborhood;

import java.util.Arrays;
import java.util.Iterator;

import net.imglib2.AbstractEuclideanSpace;
import net.imglib2.AbstractInterval;
import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.FlatIterationOrder;
import net.imglib2.Interval;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.*;

/**
 * A factory for Accessibles on rectangular neighboorhoods.
 *
 * @author Tobias Pietzsch
 * @author Jonathan Hale (University of Konstanz)
 * @author Christian Tischer
 */
public class RectangleShape2 implements Shape
{
	final Interval spanInterval;

	final boolean skipCenter;

	/**
	 * @param spanInterval
	 * @param skipCenter
	 */
	public RectangleShape2( final Interval spanInterval, final boolean skipCenter )
	{
		this.spanInterval = spanInterval;
		this.skipCenter = skipCenter;
	}

	/**
	 *
	 * Note that the width of the rectangle along each dimension will be
	 * 2 * span + 1
	 *
	 * @param spans
	 * @param skipCenter
	 */
	public RectangleShape2( final long[] spans, final boolean skipCenter )
	{
		this.spanInterval = createSpanInterval( spans );
		this.skipCenter = skipCenter;
	}

	private FinalInterval createSpanInterval( long[] spans )
	{
		int n = spans.length;
		final long[] min = new long[ n ];
		final long[] max = new long[ n ];
		for ( int d = 0; d < n; ++d )
		{
			min[ d ] = -spans[d];
			max[ d ] = spans[d];
		}
		return new FinalInterval( min, max );
	}

	@Override
	public < T > RectangleShape.NeighborhoodsIterableInterval< T >
	neighborhoods( final RandomAccessibleInterval< T > source )
	{
		final RectangleNeighborhoodFactory< T > f =
				skipCenter ?
						RectangleNeighborhoodSkipCenterUnsafe.< T >factory() :
						RectangleNeighborhoodUnsafe.< T >factory();

		return new RectangleShape.NeighborhoodsIterableInterval< T >(
				source, spanInterval, f );
	}

	@Override
	public < T > RectangleShape.NeighborhoodsAccessible< T >
	neighborhoodsRandomAccessible( final RandomAccessible< T > source )
	{
		final RectangleNeighborhoodFactory< T > f =
				skipCenter ?
						RectangleNeighborhoodSkipCenterUnsafe.< T >factory() :
						RectangleNeighborhoodUnsafe.< T >factory();

		return new RectangleShape.NeighborhoodsAccessible< T >( source, spanInterval, f );
	}

	@Override
	public < T > RectangleShape.NeighborhoodsIterableInterval< T >
	neighborhoodsSafe( final RandomAccessibleInterval< T > source )
	{
		final RectangleNeighborhoodFactory< T > f =
				skipCenter ?
						RectangleNeighborhoodSkipCenter.< T >factory() :
						RectangleNeighborhood.< T >factory();
		return new RectangleShape.NeighborhoodsIterableInterval< T >( source, spanInterval, f );
	}

	@Override
	public < T > RectangleShape.NeighborhoodsAccessible< T >
	neighborhoodsRandomAccessibleSafe( final RandomAccessible< T > source )
	{
		final RectangleNeighborhoodFactory< T > f =
				skipCenter ?
				RectangleNeighborhoodSkipCenter.< T >factory() :
				RectangleNeighborhood.< T >factory();
		return new RectangleShape.NeighborhoodsAccessible< T >( source, spanInterval, f );
	}

	/**
	 * @return {@code true} if {@code skipCenter} was set to true
	 *         during construction, {@code false} otherwise.
	 * @see CenteredRectangleShape#CenteredRectangleShape(int[], boolean)
	 */
	public boolean isSkippingCenter()
	{
		return skipCenter;
	}

	/**
	 * @return The span of this shape.
	 */
	public Interval getSpan()
	{
		return spanInterval;
	}

	@Override
	public String toString()
	{
		return "RectangleShape, span = " + spanInterval;
	}

	public static final class NeighborhoodsIterableInterval< T >
			extends AbstractInterval implements IterableInterval< Neighborhood< T > >
	{
		final RandomAccessibleInterval< T > source;

		final Interval span;

		final RectangleNeighborhoodFactory< T > factory;

		final long size;

		public NeighborhoodsIterableInterval(
				final RandomAccessibleInterval< T > source,
				final Interval span,
				final RectangleNeighborhoodFactory< T > factory )
		{
			super( source );
			this.source = source;
			this.span = span;
			this.factory = factory;
			long s = source.dimension( 0 );
			for ( int d = 1; d < n; ++d )
				s *= source.dimension( d );
			size = s;
		}

		@Override
		public Cursor< Neighborhood< T >> cursor()
		{
			return new RectangleNeighborhoodCursor< T >( source, span, factory );
		}

		@Override
		public long size()
		{
			return size;
		}

		@Override
		public Neighborhood< T > firstElement()
		{
			return cursor().next();
		}

		@Override
		public Object iterationOrder()
		{
			return new FlatIterationOrder( this );
		}

		@Override
		public Iterator< Neighborhood< T >> iterator()
		{
			return cursor();
		}

		@Override
		public Cursor< Neighborhood< T >> localizingCursor()
		{
			return cursor();
		}
	}

	public static final class NeighborhoodsAccessible< T >
			extends AbstractEuclideanSpace implements RandomAccessible< Neighborhood< T > >
	{
		final RandomAccessible< T > source;

		final Interval span;

		final RectangleNeighborhoodFactory< T > factory;

		public NeighborhoodsAccessible(
				final RandomAccessible< T > source,
				final Interval span,
				final RectangleNeighborhoodFactory< T > factory )
		{
			super( source.numDimensions() );
			this.source = source;
			this.span = span;
			this.factory = factory;
		}

		@Override
		public RandomAccess< Neighborhood< T >> randomAccess()
		{
			return new RectangleNeighborhoodRandomAccess< T >( source, span, factory );
		}

		@Override
		public RandomAccess< Neighborhood< T >> randomAccess( final Interval interval )
		{
			return new RectangleNeighborhoodRandomAccess< T >( source, span, factory, interval );
		}
	}

	@Override
	public Interval getStructuringElementBoundingBox( final int numDimensions )
	{
		return spanInterval;
	}
}

