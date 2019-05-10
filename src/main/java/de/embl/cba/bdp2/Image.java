package de.embl.cba.bdp2;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class Image< R extends RealType< R > & NativeType< R > >
{
	private RandomAccessibleInterval< R > rai;
	private String name;
	private double[] voxelSpacing;
	private String voxelUnit;

	public Image( RandomAccessibleInterval< R > rai,
				  String name,
				  double[] voxelSpacing,
				  String voxelUnit )
	{
		this.rai = rai;
		this.name = name;
		this.voxelSpacing = voxelSpacing;
		this.voxelUnit = voxelUnit;
	}

	public RandomAccessibleInterval< R > getRai()
	{
		return rai;
	}

	public void setRai( RandomAccessibleInterval< R > rai )
	{
		this.rai = rai;
	}

	public double[] getVoxelSpacing()
	{
		return voxelSpacing;
	}

	public void setName( String name )
	{
		this.name = name;
	}

	public void setVoxelSpacing( double... voxelSpacing )
	{
		this.voxelSpacing = voxelSpacing;
	}

	public String getVoxelUnit()
	{
		return voxelUnit;
	}

	public void setVoxelUnit( String voxelUnit )
	{
		this.voxelUnit = voxelUnit;
	}

	public String getName()
	{
		return name;
	}


}
