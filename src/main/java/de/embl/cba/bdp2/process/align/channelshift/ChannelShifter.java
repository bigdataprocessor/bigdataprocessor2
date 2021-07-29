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
package de.embl.cba.bdp2.process.align.channelshift;

import de.embl.cba.bdp2.utils.DimensionOrder;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import java.util.ArrayList;
import java.util.List;

public class ChannelShifter < R extends RealType< R > & NativeType< R > >
{
	private final RandomAccessibleInterval< R > rai;
	private final long numChannels;
	private final ArrayList< RandomAccessibleInterval< R > > channelRAIs;

	public ChannelShifter( RandomAccessibleInterval< R > rai )
	{
		this.rai = rai;
		numChannels = rai.dimension( DimensionOrder.C );
		channelRAIs = getChannelRAIs();
	}

	public RandomAccessibleInterval< R > getShiftedRai( List< long[] > translationsXYZT )
	{
		ArrayList< RandomAccessibleInterval< R > > shiftedChannelRAIs =
				getShiftedRAIs( translationsXYZT );

		Interval intersect = getIntersectionInterval( shiftedChannelRAIs );

		final ArrayList< RandomAccessibleInterval< R > > croppedRAIs
				= getCroppedRAIs( shiftedChannelRAIs, intersect );

		final IntervalView< R > shiftedView = Views.permute(
				Views.stack( croppedRAIs ),
				DimensionOrder.C,
				DimensionOrder.T );

		
		return shiftedView;
	}

	private ArrayList< RandomAccessibleInterval< R > > getCroppedRAIs(
			ArrayList< RandomAccessibleInterval< R > > rais,
			Interval intersect )
	{
		final ArrayList< RandomAccessibleInterval< R > > cropped = new ArrayList<>();
		for ( int c = 0; c < numChannels; c++ )
		{
			final IntervalView< R > crop = Views.interval( rais.get( c ), intersect );
			cropped.add( Views.zeroMin( crop ) );
		}
		return cropped;
	}

	private Interval getIntersectionInterval( ArrayList< RandomAccessibleInterval< R > > shiftedChannelRAIs )
	{
		Interval intersect = shiftedChannelRAIs.get( 0 );
		for ( int c = 1; c < numChannels; c++ )
			intersect = Intervals.intersect( intersect, shiftedChannelRAIs.get( c ) );
		return intersect;
	}

	private ArrayList< RandomAccessibleInterval< R > > getShiftedRAIs( List< long[] > translationsXYZT )
	{
		ArrayList< RandomAccessibleInterval< R > > shiftedChannelRAIs = new ArrayList<>();

		for ( int c = 0; c < numChannels; c++ )
			shiftedChannelRAIs.add(
					Views.translate( channelRAIs.get( c ), translationsXYZT.get( c ) ) );

		return shiftedChannelRAIs;
	}

	private ArrayList< RandomAccessibleInterval< R > > getChannelRAIs()
	{
		ArrayList< RandomAccessibleInterval< R > > channelRais = new ArrayList<>();

		for ( int c = 0; c < numChannels; c++ )
			channelRais.add( Views.hyperSlice( rai, DimensionOrder.C, c ) );

		return channelRais;
	}

	private long getNumChannels()
	{
		return numChannels;
	}
}
