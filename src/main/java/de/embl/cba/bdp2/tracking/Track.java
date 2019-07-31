package de.embl.cba.bdp2.tracking;

public class Track
{
	private final String id;
	private double[] voxelSpacings;

	public Track( String id )
	{
		this.id = id;
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
		return new double[ 0 ];
	}
}
