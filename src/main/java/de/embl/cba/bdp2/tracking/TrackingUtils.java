package de.embl.cba.bdp2.tracking;

import de.embl.cba.bdp2.logging.Logger;

import java.util.Arrays;

public class TrackingUtils
{
	public static void logTrackPosition( Track track, long t )
	{
		Logger.log( "Track: " + track.getId() +
				"; t = " + t +
				"; pos [voxel] = " + Arrays.toString( track.getPosition( t ) ) +
				"; pos [calibrated] = " + Arrays.toString( track.getCalibratedPosition( t ) ) );
	}
}
