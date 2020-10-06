package de.embl.cba.bdp2.image;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.embl.cba.bdp2.open.core.FileInfos;
import de.embl.cba.bdp2.utils.DimensionOrder;
import de.embl.cba.bdp2.utils.Utils;
import mpicbg.imglib.multithreading.Stopable;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.util.Util;

import java.util.ArrayList;

public class Image< R extends RealType< R > & NativeType< R > >
{
	public static final String WARNING_VOXEL_SIZE = "Please check voxel size.";

	private RandomAccessibleInterval< R > raiXYZCT;
	private String name;
	private String[] channelNames;
	private double[] voxelSize;
	private String voxelUnit;
	private FileInfos fileInfos;
	private ArrayList< Stopable > stopables = new ArrayList<>(  );
	private ArrayList< String > infos = new ArrayList<>(  );

	public Image( RandomAccessibleInterval< R > raiXYZCT,
				  String name,
				  String[] channelNames,
				  double[] voxelSize,
				  String voxelUnit,
				  FileInfos fileInfos )
	{
		this.raiXYZCT = raiXYZCT;
		this.name = name;
		this.channelNames = channelNames.clone();
		this.voxelSize = voxelSize.clone();
		this.voxelUnit = voxelUnit;
		this.fileInfos = fileInfos;
	}

	public Image( Image< R > image )
	{
		this.raiXYZCT = image.getRai();
		this.name = image.getName();
		this.channelNames = image.getChannelNames().clone();
		this.voxelSize = image.getVoxelSize().clone();
		this.voxelUnit = image.getVoxelUnit();
		this.fileInfos = image.getFileInfos();
	}

	public ArrayList< String > getInfos()
	{
		return infos;
	}

	public void setInfos( ArrayList< String > infos )
	{
		this.infos = infos;
	}

	public long[] getDimensionsXYZCT()
	{
		final long[] longs = new long[ raiXYZCT.numDimensions() ];
		raiXYZCT.dimensions( longs );
		return longs;
	}

	public String getDataTypeString()
	{
		final R type = Util.getTypeFromInterval( raiXYZCT );
		if ( type instanceof UnsignedByteType )
			return "unsigned 8 bit";
		else if ( type instanceof UnsignedShortType )
			return "unsigned 16 bit";
		else
			return "???";
	}

	public double getTotalSizeGB()
	{
		return Utils.getSizeGB( this.getRai() );
	}

	public double getOneVolumeSizeGB()
	{
		long numVolumes = getNumTimePoints() * getNumChannels();
		double totalSizeGB = getTotalSizeGB();

		System.out.println( "Total " + getTotalSizeGB() );
		System.out.println( "Timepoints " + getNumTimePoints() );
		System.out.println( "Channels " + getNumChannels() );

		return totalSizeGB / numVolumes;
	}

	public String[] getChannelNames()
	{
		return channelNames;
	}

	public void setChannelNames( String[] channelNames )
	{
		this.channelNames = channelNames;
	}

	@JsonIgnore
	public FileInfos getFileInfos()
	{
		return fileInfos;
	}

	@JsonIgnore
	public void setFileInfos( FileInfos fileInfos )
	{
		this.fileInfos = fileInfos;
	}

	@JsonIgnore
	public RandomAccessibleInterval< R > getRai()
	{
		return raiXYZCT;
	}

	@JsonIgnore
	public void setRai( RandomAccessibleInterval< R > raiXYZCT )
	{
		this.raiXYZCT = raiXYZCT;
	}

	public double[] getVoxelSize()
	{
		return voxelSize;
	}

	public void setName( String name )
	{
		this.name = name;
	}

	public void setVoxelSize( double... voxelSize )
	{
		this.voxelSize = voxelSize;
		if ( infos.indexOf( WARNING_VOXEL_SIZE ) != -1 ){
			infos.remove( infos.indexOf( WARNING_VOXEL_SIZE ) );
		};
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
		final Image< R > image = new Image<>( raiXYZCT, getName(), getChannelNames(), getVoxelSize(), getVoxelUnit(), getFileInfos() );
		image.setInfos( this.infos );
		return image;
	}

	public int getNumTimePoints()
	{
		return (int) raiXYZCT.dimension( DimensionOrder.T );
	}

	public long getNumChannels()
	{
		return raiXYZCT.dimension( DimensionOrder.C );
	}

	public void addStopableProcess( Stopable stopable )
	{
		stopables.add( stopable );
	}

	public void stopStopableProcesses()
	{
		for ( Stopable stopable : stopables )
		{
			if ( stopable != null ) // might be devel an Garbage collected already
				stopable.stopThread();
		}
	}

	@Override
	public String toString()
	{
		return getName();
	}

	public String getInfo()
	{
		String info = "";
		info += "Image name: " + getName();
		final String[] channelNames = getChannelNames();
		for ( int c = 0; c < channelNames.length; c++ )
		{
			info += "\n  Channel name " + c + ": " + channelNames[ c ];
		}
		info += "\nSize [GB]: " + getTotalSizeGB();
		info += "\nData type: " + getDataTypeString();
		info += "\nSize X,Y,Z [Voxels]: " + Utils.create3DArrayString( getDimensionsXYZCT() );
		info += "\nTime-points: " + getDimensionsXYZCT()[4];
		info += "\nVoxel size ["+ getVoxelUnit() +"]: " + Utils.create3DArrayString( getVoxelSize() );

		return info;
	}

}
