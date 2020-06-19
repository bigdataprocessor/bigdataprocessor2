package de.embl.cba.bdp2.drift.track;

import net.imagej.ops.Ops;
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
				track.setPosition( t, pInterpolate );
			}
		}
	}
}
