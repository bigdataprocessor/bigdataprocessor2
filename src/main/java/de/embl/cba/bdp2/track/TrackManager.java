package de.embl.cba.bdp2.track;

import java.util.HashMap;

public abstract class TrackManager
{
	private static HashMap< String, Track > nameToTrack = new HashMap<>( );

	public static HashMap< String, Track > getTracks(){
		return nameToTrack;
	}
}
