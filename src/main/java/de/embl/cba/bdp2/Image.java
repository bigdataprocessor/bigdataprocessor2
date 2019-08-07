package de.embl.cba.bdp2;

import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.utils.DimensionOrder;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class Image< R extends RealType< R > & NativeType< R > >
{
	// TODO: keep track of the dimensions
	//  of the image in the voxel space of the fileInfos

	private RandomAccessibleInterval< R > raiXYZCT;
	private String name;
	private double[] voxelSpacing;
	private String voxelUnit;
	private FileInfos fileInfos;

	public Image( RandomAccessibleInterval< R > raiXYZCT,
				  String name,
				  double[] voxelSpacing,
				  String voxelUnit )
	{
		this.raiXYZCT = raiXYZCT;
		this.name = name;
		this.voxelSpacing = voxelSpacing;
		this.voxelUnit = voxelUnit;
	}

	public Image( RandomAccessibleInterval< R > raiXYZCT,
				  String name,
				  double[] voxelSpacing,
				  String voxelUnit,
				  FileInfos fileInfos )
	{
		this( raiXYZCT, name, voxelSpacing, voxelUnit );
		this.fileInfos = fileInfos;
	}

	public FileInfos getFileInfos()
	{
		return fileInfos;
	}

	public void setFileInfos( FileInfos fileInfos )
	{
		this.fileInfos = fileInfos;
	}

	public RandomAccessibleInterval< R > getRai()
	{
		return raiXYZCT;
	}

	public void setRai( RandomAccessibleInterval< R > raiXYZCT )
	{
		this.raiXYZCT = raiXYZCT;
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

	public Image< R > newImage( RandomAccessibleInterval< R > raiXYZCT )
	{
		return new Image<>( raiXYZCT, getName(), getVoxelSpacing(), getVoxelUnit(), getFileInfos() );
	}

	public long numTimePoints()
	{
		return raiXYZCT.dimension( DimensionOrder.T );
	}

	public long numChannels()
	{
		return raiXYZCT.dimension( DimensionOrder.C );
	}
}
