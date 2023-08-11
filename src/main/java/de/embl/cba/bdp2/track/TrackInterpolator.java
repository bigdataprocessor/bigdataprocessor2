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
package de.embl.cba.bdp2.track;

import net.imglib2.util.LinAlgHelpers;

import java.util.ArrayList;

public class TrackInterpolator
{
	private final Track track;

	public TrackInterpolator( Track track )
	{
		this.track = track;
	}

	public void run()
	{
		final ArrayList< Integer > timePoints = new ArrayList<>( track.getTimePoints() );

		for ( int i = timePoints.size() - 1; i >= 0 ; i-- )
		{
			if ( track.getType( timePoints.get( i ) ).equals( TrackPosition.PositionType.Interpolated ) )
			{
				timePoints.remove( i );
			}
		}

		for ( int i = 0; i < timePoints.size() - 1; i++ )
		{
			final Integer tCurrent = timePoints.get( i );
			final Integer tNext = timePoints.get( i + 1 );

			final int dt = tNext - tCurrent;

			if ( dt == 1 ) continue; // no time point missing

			final double[] pCurrent = track.getPosition( tCurrent );
			final double[] pNext = track.getPosition( tNext );

			final double[] dp = new double[ pCurrent.length ];
			LinAlgHelpers.subtract( pNext, pCurrent, dp );
			for ( int t = tCurrent + 1; t < tNext; t++ )
			{
				double f = ( t - tCurrent ) / (1.0 * dt);
				final double[] pInterpolate = new double[ pCurrent.length ];
				LinAlgHelpers.scale( dp, f, pInterpolate );
				LinAlgHelpers.add( pCurrent, pInterpolate, pInterpolate );
				track.setPosition( t, pInterpolate, TrackPosition.PositionType.Interpolated );
			}
		}
	}
}
