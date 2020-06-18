package de.embl.cba.bdp2.track;

import net.imglib2.RealPoint;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

public class Track
{
	private final String trackName;
	private double[] voxelSpacings;
	private HashMap< Integer, double[] > timeToPosition;

	public Track( String trackName )
	{
		this.trackName = trackName;
		timeToPosition = new HashMap< Integer, double[] >();
	}

	public void setVoxelSpacing( double[] voxelSpacings )
	{
		this.voxelSpacings = voxelSpacings;
	}

	public String getTrackName()
	{
		return trackName;
	}


	// Does that make sense? Positions are always calibrated, or?
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

	public void setPosition( int t, double[] position )
	{
		timeToPosition.put( t, position );
	}

	public void setPosition( int t, RealPoint realPoint )
	{
		final double[] doubles = new double[ realPoint.numDimensions() ];
		realPoint.localize( doubles );
		timeToPosition.put( t, doubles );
	}

	public double[] getPosition( int t )
	{
		return timeToPosition.get( t );
	}

	public long[] getLongPosition( int t )
	{
		return Arrays.stream( timeToPosition.get( t ) ).mapToLong( x -> Math.round( x ) ).toArray();
	}

	public HashMap< Integer, double[] > getTimeToPositionMap()
	{
		return timeToPosition;
	}

	public Set< Integer > getTimePoints()
	{
		return timeToPosition.keySet();
	}

	public int numDimensions()
	{
		return timeToPosition.values().iterator().next().length;
	}

	public int tMin()
	{
		int tMin = Integer.MAX_VALUE;
		for ( int t : timeToPosition.keySet() )
			if ( t < tMin ) tMin = t;

		return tMin;
	}

	public int tMax()
	{
		int tMax = Integer.MIN_VALUE;
		for ( int t : timeToPosition.keySet() )
			if ( t > tMax ) tMax = t;

		return tMax;
	}

	@Override
	public String toString()
	{
		return trackName;
	}
}
