/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2023 EMBL
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
package de.embl.cba.bdp2.process.align.channelshift;

import bdv.util.ModifiableInterval;
import de.embl.cba.bdp2.process.align.splitchip.SplitChipMerger;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.log.Logger;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import java.util.ArrayList;

import static de.embl.cba.bdp2.utils.DimensionOrder.*;

public class RegionOptimiser
{
	public static < R extends RealType< R > & NativeType< R > >
	double[] optimiseIntervals(
			Image< R > image,
			ArrayList< ModifiableInterval > intervals )
	{

		final double[] shift = optimiseRegions2D( image.getRai(), intervals );

		Logger.info( "Region Centre Optimiser: Shift [Pixel]: "
				+ shift[ 0 ] + ", "
				+ shift[ 1 ] );

		return shift;
	}

	public static void adjustModifiableInterval( double[] shift, ModifiableInterval interval )
	{

		final long[] min = Intervals.minAsLongArray( interval );
		final long[] max = Intervals.maxAsLongArray( interval );

		for ( int d = 0; d < 2; d++ )
		{
			min[ d ] = ( long ) ( min[ d ] - shift[ d ] );
			max[ d ] = ( long) ( max[ d ] - shift[ d ] );
		}

		interval.set( new FinalInterval( min, max ) );
	}

	private static < R extends RealType< R > & NativeType< R > >
	double[] optimiseRegions2D(
			RandomAccessibleInterval< R > rai5D,
			ArrayList< ? extends Interval > intervals )
	{
		final ArrayList< RandomAccessibleInterval< R > > planes
				= new ArrayList<>();

		for ( Interval interval : intervals )
		{
			final FinalInterval interval5D = SplitChipMerger.intervalXYZasXYZCT( rai5D, interval );

			final IntervalView< R > crop =
					Views.zeroMin(
							Views.interval(
									rai5D,
									interval5D ) );

			final IntervalView< R > time = Views.hyperSlice( crop, T, 0 );
			final IntervalView< R > channel = Views.hyperSlice( time, C, 0 );
			final IntervalView< R > plane =
					Views.hyperSlice(
							channel,
							Z,
							channel.dimension( Z ) / 2 );

			planes.add( plane );
		}

		// see github branch withPhaseCorrelation
//		final double[] shift = PhaseCorrelationTranslationComputer.computeShift(
//				planes.get( 0 ), planes.get( 1 ),
//				Executors.newFixedThreadPool( 2 ) );

		return null; // shift
	}
}
