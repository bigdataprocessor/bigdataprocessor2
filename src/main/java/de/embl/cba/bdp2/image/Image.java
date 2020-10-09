package de.embl.cba.bdp2.image;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.embl.cba.bdp2.open.fileseries.FileInfos;
import de.embl.cba.bdp2.utils.DimensionOrder;
import de.embl.cba.bdp2.utils.Utils;
import mpicbg.imglib.multithreading.Stopable;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.util.Util;

import java.util.ArrayList;

public class Image< R extends RealType< R > & NativeType< R > >
{
	public static final String WARNING_VOXEL_SIZE = "Please check voxel size.";

	/**
	 * The cachedCellImg loads the data for the rai.
	 * This must be 5D with dimension order XYZCT.
	 *
	 * The optimal sizes of the cells depend on the use-case
	 */
	private CachedCellImg< R, ? > cachedCellImg;

	/**
	 * The rai holds the (processed) image data.
	 * Initially, the rai simply is above cachedCellImg,
	 * but as more and more processing steps are applied,
	 * the rai accumulates a cascade of views and conversions.
	 * The loading of the actual image data is however still
	 * backed by above cachedCellImg.
	 */
	private RandomAccessibleInterval< R > rai;

	private String name;
	private String[] channelNames;
	private double[] voxelSize;
	private String voxelUnit;

	// Note: currently not used, consider moving to a branch
	private ArrayList< Stopable > stopables = new ArrayList<>(  );

	public Image( CachedCellImg< R, ? > cachedCellImg,
				  String name,
				  String[] channelNames,
				  double[] voxelSize,
				  String voxelUnit )
	{
		this.cachedCellImg = cachedCellImg;
		this.rai = cachedCellImg;
		this.name = name;
		this.channelNames = channelNames.clone();
		this.voxelSize = voxelSize.clone();
		this.voxelUnit = voxelUnit;
	}

	/**
	 * Copy constructor.
	 *
	 * @param image
	 */
	public Image( Image< R > image )
	{
		this.cachedCellImg = image.cachedCellImg; // want to use same cache, thus by-reference
		this.rai = image.rai; // don't know how copy this, thus by-reference... TODO?
		this.name = image.name; // immutable anyway
		this.channelNames = image.channelNames.clone();
		this.voxelSize = image.voxelSize.clone();
		this.voxelUnit = image.getVoxelUnit(); // immutable anyway
	}

	public long[] getDimensionsXYZCT()
	{
		final long[] longs = new long[ rai.numDimensions() ];
		rai.dimensions( longs );
		return longs;
	}

	public String getDataTypeString()
	{
		final R type = Util.getTypeFromInterval( rai );
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
	public RandomAccessibleInterval< R > getRai()
	{
		return rai;
	}

	@JsonIgnore
	/**
	 * This method should be used to update the rai
	 * in case a processing step was added.
	 *
	 * Typically, before doing that one would make a
	 * new instance of the image using the copy constructor.
	 * In case the processing changed the voxel size
	 * one must also adapt this.
	 */
	public void setRai( RandomAccessibleInterval< R > raiXYZCT )
	{
		this.rai = raiXYZCT;
	}

	public double[] getVoxelSize()
	{
		return voxelSize;
	}

	public void setName( String name )
	{
		this.name = name;
	}

	/**
	 * Adapt the voxel size, e.g., in case a processing step like
	 * binning was applied.
	 *
	 * @param voxelSize
	 */
	public void setVoxelSize( double... voxelSize )
	{
		this.voxelSize = voxelSize;
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

	public int getNumTimePoints()
	{
		return (int) rai.dimension( DimensionOrder.T );
	}

	public long getNumChannels()
	{
		return rai.dimension( DimensionOrder.C );
	}

	public CachedCellImg< R, ? > getCachedCellImg()
	{
		return cachedCellImg;
	}

	public void setCachedCellImg( CachedCellImg< R, ? > cachedCellImg )
	{
		this.cachedCellImg = cachedCellImg;
	}

	public void addStopableProcess( Stopable stopable )
	{
		stopables.add( stopable );
	}

	public void stopStopableProcesses()
	{
		for ( Stopable stopable : stopables )
		{
			if ( stopable != null ) // might be Garbage collected already
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

	public int[] getCellDims()
	{
		int[] cellDims = new int[ 5 ];
		cachedCellImg.getCellGrid().cellDimensions( cellDims );
		return cellDims;
	}
}
