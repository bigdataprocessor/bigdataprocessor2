package de.embl.cba.bdp2.tracking;

import java.util.Arrays;
import java.util.HashMap;

public class Track
{
	private final String id;
	private double[] voxelSpacings;
	private HashMap< Long, double[] > timeToPosition;

	public Track( String id )
	{
		this.id = id;
		timeToPosition = new HashMap< Long, double[] >();
	}

	public void setVoxelSpacing( double[] voxelSpacings )
	{
		this.voxelSpacings = voxelSpacings;
	}

	public String getId()
	{
		return id;
	}

	public double[] getCalibratedPosition( long t )
	{
		if ( timeToPosition.containsKey( t ) )
		{
			final double[] position = timeToPosition.get( t );
			return calibrate( position );
		}
		else
		{
			return null;
		}
	}

	private double[] calibrate( double[] position )
	{
		double[] calibratedPosition = new double[ position.length ];
		for ( int d = 0; d < position.length; d++ )
			calibratedPosition[ d ] = position[ d ] * voxelSpacings[ d ];
		return calibratedPosition;
	}

	public void setPosition( long t, double[] position )
	{
		timeToPosition.put( t, position );
	}

	public double[] getPosition( long t )
	{
		return timeToPosition.get( t );
	}

	public long[] getLongPosition( long t )
	{
		return Arrays.stream(  timeToPosition.get( t ) ).mapToLong( x -> (long) x  ).toArray();
	}


}
