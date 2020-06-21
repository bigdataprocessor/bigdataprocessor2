package de.embl.cba.bdp2.drift.track;

public class TrackPosition
{
	public double[] position;
	public PositionType type;

	public TrackPosition( double[] position, PositionType type )
	{
		this.position = position;
		this.type = type;
	}

	public enum PositionType
	{
		Anchor,
		Interpolated
	}
}
