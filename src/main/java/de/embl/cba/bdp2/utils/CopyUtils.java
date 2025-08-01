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
package de.embl.cba.bdp2.utils;

import net.imglib2.*;
import net.imglib2.algorithm.util.Grids;
import net.imglib2.img.AbstractImg;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

public class CopyUtils
{
	public static < T extends RealType< T > & NativeType< T > >
	RandomAccessibleInterval< T > copyAsArrayImg( RandomAccessibleInterval< T > orig )
	{
		final ArrayImg arrayImg = new ArrayImgFactory( Util.getTypeFromInterval( orig ) ).create( orig );

		final RandomAccessibleInterval< T > copy = Views.translate( arrayImg, Intervals.minAsLongArray( orig ) );

		LoopBuilder.setImages( copy, orig ).forEachPixel( Type::set );

		return copy;
	}

	public static < R extends RealType< R > & NativeType< R > >
	RandomAccessibleInterval< R > copyVolumeRaiMultiThreaded( RandomAccessibleInterval< R > volume,
															  int numThreads )
	{
		final int dimensionX = ( int ) volume.dimension( 0 );
		final int dimensionY = ( int ) volume.dimension( 1 );
		final int dimensionZ = ( int ) volume.dimension( 2 );

		final long numElements =
				AbstractImg.numElements( Intervals.dimensionsAsLongArray( volume ) );

		RandomAccessibleInterval< R > copy;

		if ( numElements < Integer.MAX_VALUE - 1 )
		{
			copy = new ArrayImgFactory( Util.getTypeFromInterval( volume ) ).create( volume );
		}
		else
		{
			int cellSizeZ = (int) ( ( Integer.MAX_VALUE - 1 )
					/ ( volume.dimension( 0  ) * volume.dimension( 1 ) ) );

			final int[] cellSize = {
					dimensionX,
					dimensionY,
					cellSizeZ };

			copy = new CellImgFactory( Util.getTypeFromInterval( volume ), cellSize ).create( volume );
		}

		final int[] blockSize = {
				dimensionX,
				dimensionY,
				( int ) Math.ceil( 1.0 * dimensionZ / numThreads ) };

		Grids.collectAllContainedIntervals(
				Intervals.dimensionsAsLongArray( volume ) , blockSize )
				.parallelStream().forEach(
				interval -> copy( volume, Views.interval( copy, interval )));

		return copy;
	}

	public static < R extends RealType< R > & NativeType< R > >
	RandomAccessibleInterval< R > copyPlanarRaiMultiThreaded( RandomAccessibleInterval< R > volume,
															  int numThreads )
	{
		final int dimensionX = ( int ) volume.dimension( 0 );
		final int dimensionY = ( int ) volume.dimension( 1 );

		final long numElements =
				AbstractImg.numElements( Intervals.dimensionsAsLongArray( volume ) );

		RandomAccessibleInterval< R > copy;

		if ( numElements < Integer.MAX_VALUE - 1 )
		{
			copy = new ArrayImgFactory( Util.getTypeFromInterval( volume ) ).create( volume );
		}
		else
		{
			// TODO: test below code
			final int[] cellSize = {
					dimensionX,
					dimensionY };

			copy = new CellImgFactory( Util.getTypeFromInterval( volume ), cellSize ).create( volume );
		}

		final int[] blockSize = {
				dimensionX,
				( int ) Math.ceil( dimensionY / numThreads ) };

		Grids.collectAllContainedIntervals(
				Intervals.dimensionsAsLongArray( volume ) , blockSize )
				.parallelStream().forEach(
				interval -> copy( volume, Views.interval( copy, interval )));

		return copy;
	}

	private static < T extends Type< T > > void copy( final RandomAccessible< T > source,
													 final IterableInterval< T > target )
	{
		// create a cursor that automatically localizes itself on every move
		Cursor< T > targetCursor = target.localizingCursor();
		RandomAccess< T > sourceRandomAccess = source.randomAccess();

		// iterate over the input cursor
		while ( targetCursor.hasNext() )
		{
			// move input cursor forward
			targetCursor.fwd();

			// set the output cursor to the position of the input cursor
			sourceRandomAccess.setPosition( targetCursor );

			// set the value of this pixel of the output image, every Type supports T.set( T type )
			targetCursor.get().set( sourceRandomAccess.get() );
		}
	}
}
