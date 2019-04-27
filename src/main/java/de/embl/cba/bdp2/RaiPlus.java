package de.embl.cba.bdp2;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class RaiPlus< R extends RealType< R > & NativeType< R > >
{
	private final RandomAccessibleInterval< R > rai;
	private final String name;
	private final double[] voxelSize;
	private final String voxelSizeUnit;

	public RaiPlus( RandomAccessibleInterval< R > rai,
					String name,
					double[] voxelSize,
					String voxelSizeUnit )
	{
		this.rai = rai;
		this.name = name;
		this.voxelSize = voxelSize;
		this.voxelSizeUnit = voxelSizeUnit;
	}

	public RandomAccessibleInterval< R > getRai()
	{
		return rai;
	}

	public double[] getVoxelSize()
	{
		return voxelSize;
	}

	public String getVoxelSizeUnit()
	{
		return voxelSizeUnit;
	}

	public String getName()
	{
		return name;
	}


}
