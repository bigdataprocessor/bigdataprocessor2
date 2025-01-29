/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2025 EMBL
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
package de.embl.cba.bdv.utils.objects3d;

import net.imglib2.*;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.cache.img.DiskCachedCellImgFactory;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.view.ExtendedRandomAccessibleInterval;
import net.imglib2.view.Views;

import java.util.ArrayList;
import java.util.Arrays;

public class ThresholdFloodFill< R extends RealType< R > >
{
	// input
	private final RandomAccessibleInterval< R > source;
	private final double threshold;
	private final Shape shape;
	private final long maxRegionSize;

	// other
	private int n;
	private long[] min;
	private long[] max;

	public double getSeedValue()
	{
		return seedValue;
	}

	private double seedValue;
	private ArrayList< long[] > positions;
	private RandomAccessibleInterval< BitType > mask;

	// output
	private boolean maxRegionSizeReached;

	public ThresholdFloodFill(
			RandomAccessibleInterval< R > source,
			double threshold,
			Shape shape,
			long maxRegionSize )
	{
		this.source = source;
		this.threshold = threshold;
		this.shape = shape;
		this.maxRegionSize = maxRegionSize;
		n = source.numDimensions();
	}

	public void run( long[] initialPosition )
	{
		maxRegionSizeReached = false;

		initPositions( initialPosition );

		initBoundingBox();

		floodFill();
	}

	public RandomAccessibleInterval< BitType > getMask()
	{
		RandomAccessibleInterval< BitType > croppedMask = Views.interval( mask, new FinalInterval( min, max ) );
		return croppedMask;
	}

	public boolean isMaxRegionSizeReached()
	{
		return maxRegionSizeReached;
	}

	private void floodFill()
	{

		mask = new DiskCachedCellImgFactory<>( new BitType() ).create( source );
		mask = Views.translate( mask, Intervals.minAsLongArray( source ) ); // adjust offset
		final ExtendedRandomAccessibleInterval extendedRegionMask = Views.extendZero( mask ); // add oob strategy

		final RandomAccessible< Neighborhood< R > > neighborhood = shape.neighborhoodsRandomAccessible( Views.extendZero( source ) );
		final RandomAccess< Neighborhood< R > > sourceNeighborhoodAccess = neighborhood.randomAccess();

		final RandomAccess< BitType > extendedMaskAccess = extendedRegionMask.randomAccess();

		for ( int i = 0; i < positions.size(); ++i )
		{
			if ( i > maxRegionSize )
			{
				maxRegionSizeReached = true;
				break;
			}

			sourceNeighborhoodAccess.setPosition( positions.get( i ) );

			final Cursor< R > neighborhoodCursor = sourceNeighborhoodAccess.get().cursor();

			double value;

			while ( neighborhoodCursor.hasNext() )
			{
				value = neighborhoodCursor.next().getRealDouble();

				if ( value >= threshold )
				{
					final long[] position = new long[ n ];
					neighborhoodCursor.localize( position );

					// TODO: This could be made faster,
					// because neither the bounding box nor the mask are really needed.
					extendedMaskAccess.setPosition( position );

					if ( ! extendedMaskAccess.get().get() )
					{
						extendedMaskAccess.get().setOne();
						positions.add( position );
						updateBoundingBox( position );
					}
				}
			}
		}
	}

	private void initPositions( long[] initialPosition )
	{
		positions = new ArrayList<>();
		positions.add( initialPosition );
	}


	private void initBoundingBox( )
	{
		min = new long[ n ];
		max = new long[ n ];

		for ( int d = 0; d < min.length; ++d )
		{
			min[ d ] = Long.MAX_VALUE;
			max[ d ] = Long.MIN_VALUE;
		}
	}

	private void updateBoundingBox ( long[] coordinate )
	{
		for ( int d = 0; d < min.length; ++d )
		{
			if ( coordinate[ d ] < min[ d ] ) min[ d ] = coordinate[ d ];
			if ( coordinate[ d ] > max[ d ] ) max[ d ] = coordinate[ d ];
		}
	}

	public ArrayList< long[] > getPositions()
	{
		return positions;
	}
}
