package de.embl.cba.bdp2.drift.track;

import net.imglib2.RealPoint;

import java.util.HashMap;
import java.util.Set;

public class Track
{
	private String name;
	private double[] voxelSpacings;
	private HashMap< Integer, TrackPosition > timeToTrackPosition;

	public Track( String name, double[] voxelSpacings )
	{
		this.name = name;
		this.voxelSpacings = voxelSpacings;
		timeToTrackPosition = new HashMap<>();
	}

	public TrackPosition.PositionType getType( int t )
	{
		return timeToTrackPosition.get( t ).type;
	}

	public double[] getVoxelSpacings()
	{
		return voxelSpacings;
	}

	public void setVoxelSpacings( double[] voxelSpacings )
	{
		this.voxelSpacings = voxelSpacings;
	}

	public HashMap< Integer, TrackPosition > getTimeToTrackPosition()
	{
		return timeToTrackPosition;
	}

	public void setTimeToTrackPosition( HashMap< Integer, TrackPosition > timeToTrackPosition )
	{
		this.timeToTrackPosition = timeToTrackPosition;
	}

	public void setVoxelSpacing( double[] voxelSpacings )
	{
		this.voxelSpacings = voxelSpacings;
	}

	public String getName()
	{
		return name;
	}

	public void setName( String name )
	{
		this.name = name;
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
		setPosition( t, position, TrackPosition.PositionType.Anchor );
	}

	/**
	 *
	 * @param t frame
	 * @param position calibrated
	 * @param positionType
	 */
	public void setPosition( int t, double[] position, TrackPosition.PositionType positionType )
	{
		timeToTrackPosition.put( t, new TrackPosition( position, positionType ) );
	}

	/**
	 *
	 * @param t frame
	 * @param realPoint calibrated
	 */
	public void setPosition( int t, RealPoint realPoint )
	{
		setPosition( t, realPoint, TrackPosition.PositionType.Anchor );
	}


	/**
	 *
	 * @param t frame
	 * @param realPoint calibrated
	 * @param positionType
	 */
	public void setPosition( int t, RealPoint realPoint, TrackPosition.PositionType positionType )
	{
		final double[] doubles = new double[ realPoint.numDimensions() ];
		realPoint.localize( doubles );
		setPosition( t, doubles, positionType );
	}
	/**
	 *
	 * @param t frame
	 * @return calibrated position
	 */
	public double[] getPosition( int t )
	{
		return timeToTrackPosition.get( t ).position;
	}

	/**
	 *
	 * @param t
	 * @return voxel position
	 */
	public long[] getVoxelPosition( int t )
	{
		if ( timeToTrackPosition.containsKey( t ) )
		{
			return uncalibrate( timeToTrackPosition.get( t ).position );
		}
		else
		{
			return null;
		}
	}

	public Set< Integer > getTimePoints()
	{
		return timeToTrackPosition.keySet();
	}

	public int numDimensions()
	{
		return timeToTrackPosition.values().iterator().next().position.length;
	}

	public int tMin()
	{
		int tMin = Integer.MAX_VALUE;
		for ( int t : timeToTrackPosition.keySet() )
			if ( t < tMin ) tMin = t;

		return tMin;
	}

	public int tMax()
	{
		int tMax = Integer.MIN_VALUE;
		for ( int t : timeToTrackPosition.keySet() )
			if ( t > tMax ) tMax = t;

		return tMax;
	}

	@Override
	public String toString()
	{
		return name;
	}
}
