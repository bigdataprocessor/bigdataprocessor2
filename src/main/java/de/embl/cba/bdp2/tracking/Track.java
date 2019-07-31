package de.embl.cba.bdp2.tracking;

import java.util.HashMap;

public class Track
{
	private final String id;
	private double[] voxelSpacings;
	private HashMap< Long, long[] > timeToPosition;

	public Track( String id )
	{
		this.id = id;
		timeToPosition = new HashMap<>();
	}

	public void setVoxelSpacing( double[] voxelSpacings )
	{
		this.voxelSpacings = voxelSpacings;
	}

	public String getId()
	{
		return id;
	}

	public double[] getCalibratedPosition( int t )
	{
		final long[] position = timeToPosition.get( t );
		return calibrate( position );
	}

	private double[] calibrate( long[] position )
	{
		double[] calibratedPosition = new double[ position.length ];
		for ( int d = 0; d < position.length; d++ )
			calibratedPosition[ d ] = position[ d ] * voxelSpacings[ d ];
		return calibratedPosition;
	}

	public void setPosition( long t, long[] position )
	{
		timeToPosition.put( t, position );
	}

	public long[] getPosition( long t )
	{
		return timeToPosition.get( t );
	}
}
