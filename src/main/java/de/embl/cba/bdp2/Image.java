package de.embl.cba.bdp2;

import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.utils.DimensionOrder;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class Image< R extends RealType< R > & NativeType< R > >
{
	private RandomAccessibleInterval< R > rai;
	private String name;
	private double[] voxelSpacing;
	private String voxelUnit;
	private FileInfos fileInfos;

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

	public Image( RandomAccessibleInterval< R > rai,
				  String name,
				  double[] voxelSpacing,
				  String voxelUnit,
				  FileInfos fileInfos )
	{
		this( rai, name, voxelSpacing, voxelUnit );
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

	public Image< R > newImage( RandomAccessibleInterval< R > rai )
	{
		return new Image<>( rai, getName(), getVoxelSpacing(), getVoxelUnit(), getFileInfos() );
	}

	public long numTimePoints()
	{
		return rai.dimension( DimensionOrder.T );
	}

	public long numChannels()
	{
		return rai.dimension( DimensionOrder.C );
	}
}
