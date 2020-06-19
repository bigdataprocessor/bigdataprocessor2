package de.embl.cba.bdp2.drift.track;

import net.imglib2.RealPoint;

import java.util.HashMap;
import java.util.Set;

public class Track
{
	private String trackName;
	private double[] voxelSpacings;

	private class TrackPoint
	{
		public double[] position;
		public PositionType type;

		public TrackPoint( double[] position, PositionType type )
		{
			this.position = position;
			this.type = type;
		}
	}

	private HashMap< Integer, TrackPoint > timeToTrackPoint;

	public enum PositionType
	{
		Anchor,
		Interpolated
	}

	public Track( String trackName, double[] voxelSpacings )
	{
		this.trackName = trackName;
		this.voxelSpacings = voxelSpacings;
		timeToTrackPoint = new HashMap<>();
	}

	public PositionType getType( int t )
	{
		return timeToTrackPoint.get( t ).type;
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
		setPosition( t, position, PositionType.Anchor );
	}


	/**
	 *
	 * @param t frame
	 * @param position calibrated
	 * @param positionType
	 */
	public void setPosition( int t, double[] position, PositionType positionType )
	{
		timeToTrackPoint.put( t, new TrackPoint( position, positionType ) );
	}

	/**
	 *
	 * @param t frame
	 * @param realPoint calibrated
	 */
	public void setPosition( int t, RealPoint realPoint )
	{
		setPosition( t, realPoint, PositionType.Anchor );
	}


	/**
	 *
	 * @param t frame
	 * @param realPoint calibrated
	 * @param positionType
	 */
	public void setPosition( int t, RealPoint realPoint, PositionType positionType )
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
		return timeToTrackPoint.get( t ).position;
	}

	/**
	 *
	 * @param t
	 * @return voxel position
	 */
	public long[] getVoxelPosition( int t )
	{
		if ( timeToTrackPoint.containsKey( t ) )
		{
			return uncalibrate( timeToTrackPoint.get( t ).position );
		}
		else
		{
			return null;
		}
	}

	public Set< Integer > getTimePoints()
	{
		return timeToTrackPoint.keySet();
	}

	public int numDimensions()
	{
		return timeToTrackPoint.values().iterator().next().position.length;
	}

	public int tMin()
	{
		int tMin = Integer.MAX_VALUE;
		for ( int t : timeToTrackPoint.keySet() )
			if ( t < tMin ) tMin = t;

		return tMin;
	}

	public int tMax()
	{
		int tMax = Integer.MIN_VALUE;
		for ( int t : timeToTrackPoint.keySet() )
			if ( t > tMax ) tMax = t;

		return tMax;
	}

	@Override
	public String toString()
	{
		return trackName;
	}
}
