package de.embl.cba.bdp2.track;

import de.embl.cba.bdp2.log.Logger;

import java.util.Arrays;

public class TrackingUtils
{
	public static void logTrackPosition( Track track, int t )
	{
		Logger.log( "Track: " + track.getTrackName() +
				"; t = " + t +
				"; pos [voxel] = " + Arrays.toString( track.getLongPosition( t ) ) +
				"; pos [calibrated] = " + Arrays.toString( track.getCalibratedPosition( t ) ) );
	}
}
