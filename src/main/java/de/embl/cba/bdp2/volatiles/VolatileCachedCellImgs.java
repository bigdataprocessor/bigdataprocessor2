/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2021 EMBL
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
package de.embl.cba.bdp2.volatiles;

import de.embl.cba.bdp2.image.Image;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.cache.img.RandomAccessibleCacheLoader;
import net.imglib2.cache.ref.SoftRefLoaderCache;
import net.imglib2.img.basictypeaccess.AccessFlags;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.img.basictypeaccess.array.LongArray;
import net.imglib2.img.basictypeaccess.array.ShortArray;
import net.imglib2.img.basictypeaccess.volatiles.array.VolatileByteArray;
import net.imglib2.img.basictypeaccess.volatiles.array.VolatileFloatArray;
import net.imglib2.img.basictypeaccess.volatiles.array.VolatileShortArray;
import net.imglib2.img.cell.Cell;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

public class VolatileCachedCellImgs
{
	public static RandomAccessibleInterval< FloatType > asVolatileFloatTypeCachedCellImg( RandomAccessibleInterval< FloatType > rai, CellGrid grid )
	{
		final long[] min = Intervals.minAsLongArray( rai );

		final RandomAccessibleCacheLoader< FloatType, IntArray, VolatileFloatArray > loader = RandomAccessibleCacheLoader.get(
				grid,
				Views.zeroMin( rai ),
				AccessFlags.setOf( AccessFlags.VOLATILE ) );

		final CachedCellImg< FloatType, VolatileFloatArray > cachedCellImg = new CachedCellImg(
				grid,
				new FloatType(),
				new SoftRefLoaderCache< Long, Cell< VolatileFloatArray > >().withLoader( loader ),
				new VolatileFloatArray( 1, true ) );

		final IntervalView< FloatType > translate = Views.translate( cachedCellImg, min );
		return translate;
	}

	public static RandomAccessibleInterval< UnsignedShortType > asVolatileShortTypeCachedCellImg( RandomAccessibleInterval< UnsignedShortType > rai, CellGrid grid )
	{
		final long[] min = Intervals.minAsLongArray( rai );

		final RandomAccessibleCacheLoader< UnsignedShortType, ShortArray, VolatileShortArray > loader = RandomAccessibleCacheLoader.get(
				grid,
				Views.zeroMin( rai ),
				AccessFlags.setOf( AccessFlags.VOLATILE ) );

		final CachedCellImg< UnsignedShortType, VolatileShortArray > cachedCellImg = new CachedCellImg(
				grid,
				new UnsignedShortType(),
				new SoftRefLoaderCache< Long, Cell< VolatileShortArray > >().withLoader( loader ),
				new VolatileShortArray( 1, true ) );

		final IntervalView< UnsignedShortType > translate = Views.translate( cachedCellImg, min );
		return translate;
	}

	public static RandomAccessibleInterval< UnsignedByteType > asVolatileByteTypeCachedCellImg( RandomAccessibleInterval< UnsignedByteType > rai, CellGrid grid )
	{
		final long[] min = Intervals.minAsLongArray( rai );


		final RandomAccessibleCacheLoader< UnsignedByteType, ByteArray, VolatileByteArray > loader = RandomAccessibleCacheLoader.get(
				grid,
				Views.zeroMin( rai ),
				AccessFlags.setOf( AccessFlags.VOLATILE ) );

		final CachedCellImg< UnsignedByteType, VolatileByteArray > cachedCellImg = new CachedCellImg(
				grid,
				new UnsignedByteType(),
				new SoftRefLoaderCache< Long, Cell< VolatileByteArray > >().withLoader( loader ),
				new VolatileByteArray( 1, true ) );

		final IntervalView< UnsignedByteType > translate = Views.translate( cachedCellImg, min );

		return translate;
	}

	public static < R extends RealType< R > & NativeType< R > > RandomAccessibleInterval< R > asVolatileCachedCellImg( Image< R > image )
	{
		final Type typeFromInterval = Util.getTypeFromInterval( image.getRai() );
		final CellGrid grid = new CellGrid( Intervals.dimensionsAsLongArray( image.getRai() ), image.getCachedCellDims() );

		RandomAccessibleInterval< ? > cachedCellImg;

		if ( UnsignedByteType.class.isInstance( typeFromInterval ) )
		{
			cachedCellImg = asVolatileByteTypeCachedCellImg( ( RandomAccessibleInterval ) image.getRai(), grid );
		}
		else if( UnsignedShortType.class.isInstance( typeFromInterval ) )
		{
			cachedCellImg = asVolatileShortTypeCachedCellImg( ( RandomAccessibleInterval ) image.getRai(), grid );
		}
		else if ( FloatType.class.isInstance( typeFromInterval ) )
		{
			cachedCellImg = asVolatileFloatTypeCachedCellImg( ( RandomAccessibleInterval ) image.getRai(), grid );
		}
		else
		{
			throw new UnsupportedOperationException( "Cannot yet create CachedCellImg for type:" + typeFromInterval );
		}

		return ( RandomAccessibleInterval )  cachedCellImg;
	}
}
