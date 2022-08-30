/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2022 EMBL
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package de.embl.cba.bdp2.track;

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

	/*
	 *
	 * @param t frame
	 * @param position calibrated
	 */
	public void setPosition( int t, double[] position )
	{
		setPosition( t, position, TrackPosition.PositionType.Anchor );
	}

	public void setPosition( int t, double[] position, TrackPosition.PositionType positionType )
	{
		timeToTrackPosition.put( t, new TrackPosition( position, positionType ) );
	}

	public void setPosition( int t, RealPoint realPoint )
	{
		setPosition( t, realPoint, TrackPosition.PositionType.Anchor );
	}

	public void setPosition( int t, RealPoint realPoint, TrackPosition.PositionType positionType )
	{
		final double[] doubles = new double[ realPoint.numDimensions() ];
		realPoint.localize( doubles );
		setPosition( t, doubles, positionType );
	}

	public double[] getPosition( int t )
	{
		return timeToTrackPosition.get( t ).position;
	}

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
