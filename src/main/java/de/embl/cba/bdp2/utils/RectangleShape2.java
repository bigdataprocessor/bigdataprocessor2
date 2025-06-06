/*-
 * #%L
 * Various Java code for ImageJ
 * %%
 * Copyright (C) 2018 - 2024 EMBL
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

import net.imglib2.*;
import net.imglib2.algorithm.neighborhood.*;

import java.util.Iterator;

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
	 * @param span
	 * @param skipCenter
	 */
	public RectangleShape2( final long[] span, final boolean skipCenter )
	{
		this.spanInterval = createSpanInterval( span );
		this.skipCenter = skipCenter;
	}

	private FinalInterval createSpanInterval( long[] span )
	{
		int n = span.length;
		final long[] min = new long[ n ];
		final long[] max = new long[ n ];

		for ( int d = 0; d < n; ++d )
		{
			min[ d ] = - span[ d ] / 2;
			max[ d ] = min[ d ] + span[ d ] - 1;
		}

		return new FinalInterval( min, max );
	}

	@Override
	public < T > NeighborhoodsIterableInterval< T >
	neighborhoods( final RandomAccessibleInterval< T > source )
	{
		final RectangleNeighborhoodFactory< T > f =
				skipCenter ?
						RectangleNeighborhoodSkipCenterUnsafe.factory() :
						RectangleNeighborhoodUnsafe.factory();

		return new NeighborhoodsIterableInterval< T >(
				source, spanInterval, f );
	}

	@Override
	public < T > NeighborhoodsAccessible< T >
	neighborhoodsRandomAccessible( final RandomAccessible< T > source )
	{
		final RectangleNeighborhoodFactory< T > f =
				skipCenter ?
						RectangleNeighborhoodSkipCenterUnsafe.factory() :
						RectangleNeighborhoodUnsafe.factory();

		return new NeighborhoodsAccessible( source, spanInterval, f );
	}

	@Override
	public < T > NeighborhoodsIterableInterval< T >
	neighborhoodsSafe( final RandomAccessibleInterval< T > source )
	{
		final RectangleNeighborhoodFactory< T > f =
				skipCenter ?
						RectangleNeighborhoodSkipCenter.factory() :
						RectangleNeighborhood.factory();
		return new NeighborhoodsIterableInterval< T >( source, spanInterval, f );
	}

	@Override
	public < T > NeighborhoodsAccessible< T >
	neighborhoodsRandomAccessibleSafe( final RandomAccessible< T > source )
	{
		final RectangleNeighborhoodFactory< T > f =
				skipCenter ?
				RectangleNeighborhoodSkipCenter.factory() :
				RectangleNeighborhood.factory();
		return new NeighborhoodsAccessible< T >( source, spanInterval, f );
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

		public RandomAccessible< T > getSource()
		{
			return source;
		}

		public Interval getSpan()
		{
			return span;
		}

		public RectangleNeighborhoodFactory< T > getFactory()
		{
			return factory;
		}
	}

	@Override
	public Interval getStructuringElementBoundingBox( final int numDimensions )
	{
		return spanInterval;
	}
}

