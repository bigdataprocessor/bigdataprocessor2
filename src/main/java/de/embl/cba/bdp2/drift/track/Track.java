package de.embl.cba.bdp2.drift.track;

import net.imglib2.RealPoint;

import java.util.HashMap;
import java.util.Set;

public class Track
{
	private String trackName;
	private double[] voxelSpacings;
	private HashMap< Integer, double[] > timeToPosition;

	public Track( String trackName, double[] voxelSpacings )
	{
		this.trackName = trackName;
		this.voxelSpacings = voxelSpacings;
		timeToPosition = new HashMap<>();
	}

	public void setVoxelSpacing( double[] voxelSpacings )
	{
		this.voxelSpacings = voxelSpacings;
	}

	public String getName()
	{
		return trackName;
	}

	public void setName( String trackName )
	{
		this.trackName = trackName;
	}
	private long[] uncalibrate( double[] position )
	{
		long[] voxelPosition = new long[ position.length ];
		for ( int d = 0; d < position.length; d++ )
			voxelPosition[ d ] = ( long ) ( position[ d ] / voxelSpacings[ d ] );
		return voxelPosition;
	}

	/**
	 *
	 * @param t frame
	 * @param position calibrated
	 */
	public void setPosition( int t, double[] position )
	{
		timeToPosition.put( t, position );
	}

	/**
	 *
	 * @param t frame
	 * @param realPoint calibrated
	 */
	public void setPosition( int t, RealPoint realPoint )
	{
		final double[] doubles = new double[ realPoint.numDimensions() ];
		realPoint.localize( doubles );
		timeToPosition.put( t, doubles );
	}

	/**
	 *
	 * @param t frame
	 * @return calibrated position
	 */
	public double[] getPosition( int t )
	{
		return timeToPosition.get( t );
	}

	/**
	 *
	 * @param t
	 * @return voxel position
	 */
	public long[] getVoxelPosition( int t )
	{
		if ( timeToPosition.containsKey( t ) )
		{
			return uncalibrate( timeToPosition.get( t ) );
		}
		else
		{
			return null;
		}
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
