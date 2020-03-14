package de.embl.cba.bdp2.track;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

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
		return Arrays.stream(  timeToPosition.get( t ) ).mapToLong( x -> Math.round( x ) ).toArray();
	}

	public HashMap< Long, double[] > getTimeToPositionMap()
	{
		return timeToPosition;
	}

	public Set< Long > getTimePoints()
	{
		return timeToPosition.keySet();
	}

	public int numDimensions()
	{
		return timeToPosition.values().iterator().next().length;
	}

	public long tMin()
	{
		long tMin = Long.MAX_VALUE;
		for ( long t : timeToPosition.keySet() )
			if ( t < tMin ) tMin = t;

		return tMin;
	}

	public long tMax()
	{
		long tMax = Long.MIN_VALUE;
		for ( long t : timeToPosition.keySet() )
			if ( t > tMax ) tMax = t;

		return tMax;
	}



}
