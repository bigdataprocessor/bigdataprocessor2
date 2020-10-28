package de.embl.cba.bdp2.image;

import ch.epfl.biop.bdv.bioformats.BioFormatsMetaDataHelper;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.open.CachedCellImgCreator;
import de.embl.cba.bdp2.save.CachedCellImgReplacer;
import de.embl.cba.bdp2.utils.DimensionOrder;
import de.embl.cba.bdp2.utils.Utils;
import mpicbg.imglib.multithreading.Stopable;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.cache.img.DiskCachedCellImgOptions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.util.Util;
import ome.units.quantity.Length;
import ome.units.unit.Unit;

import java.util.ArrayList;
import java.util.Arrays;

// TODO: make an interface and get rid of CachedCellImgCreator?!!

public class Image< R extends RealType< R > & NativeType< R > >
{
	public static final String WARNING_VOXEL_SIZE = "Please check voxel size.";

	/**
	 * The cachedCellImg loads the data for the rai.
	 * This must be 5D with dimension order XYZCT.
	 *
	 * The optimal sizes of the cells depend on the use-case
	 */
	private final CachedCellImgCreator< R > cachedCellImgCreator;
	private long[] rawDataDimensions;
	private int[] cellDims;

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
	private Unit< Length > voxelUnit;

	// Note: currently not used, consider moving to a branch
	private ArrayList< Stopable > stopables = new ArrayList<>(  );

	public Image( CachedCellImgCreator< R > cachedCellImgCreator )
	{
		this.cachedCellImgCreator = cachedCellImgCreator;
		this.name = cachedCellImgCreator.getImageName();
		this.channelNames = cachedCellImgCreator.getChannelNames();
		this.voxelSize = cachedCellImgCreator.getVoxelSize();
		this.voxelUnit = cachedCellImgCreator.getVoxelUnit();

		setCache( cachedCellImgCreator.getDefaultCellDimsXYZCT(), DiskCachedCellImgOptions.CacheType.BOUNDED, 100 );
		this.rai = cachedCellImgCreator.createCachedCellImg( cellDims, DiskCachedCellImgOptions.CacheType.BOUNDED, 100 );
		this.rawDataDimensions = this.getDimensionsXYZCT();
	}

	/**
	 * Copy constructor.
	 *
	 * @param image
	 */
	public Image( Image< R > image )
	{
		this.cachedCellImgCreator = image.cachedCellImgCreator; // want to use same cache, thus by reference
		this.rai = image.rai; // practically immutable
		this.name = image.name; // immutable
		this.channelNames = image.channelNames.clone();
		this.voxelSize = image.voxelSize.clone();
		this.voxelUnit = image.getVoxelUnit();
		this.cellDims = image.cellDims.clone();
		this.rawDataDimensions = image.rawDataDimensions.clone();
	}

	public long[] getDimensionsXYZCT()
	{
		final long[] longs = new long[ rai.numDimensions() ];
		rai.dimensions( longs );
		return longs;
	}

	public String getDataType()
	{
		final R type = Util.getTypeFromInterval( rai );
		if ( type instanceof UnsignedByteType )
			return "unsigned 8 bit";
		else if ( type instanceof UnsignedShortType )
			return "unsigned 16 bit";
		else
			throw new RuntimeException("Could not determine the bit-depth.");
	}

	public int getBitDepth()
	{
		final R type = Util.getTypeFromInterval( rai );
		if ( type instanceof UnsignedByteType )
			return 8;
		else if ( type instanceof UnsignedShortType )
			return 16;
		else
			throw new RuntimeException("Could not determine the bit-depth.");
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

	public RandomAccessibleInterval< R > getRai()
	{
		return rai;
	}

	/**
	 * This method should be used to update the rai
	 * in case a processing step was added.
	 *
	 * The rai that is set here must use the same
	 * backing cachedCellImg as the image. // TODO: how to enforce this?
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

	public Unit< Length > getVoxelUnit()
	{
		return voxelUnit;
	}

	public void setVoxelUnit( Unit< Length > voxelUnit )
	{
		this.voxelUnit = voxelUnit;
	}

	// use method above in the future
	@Deprecated
	public void setVoxelUnit( String voxelUnit )
	{
		this.voxelUnit = BioFormatsMetaDataHelper.getUnitFromString( voxelUnit );
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
		info += "\nData type: " + getDataType();
		info += "\nSize X,Y,Z [Voxels]: " + Arrays.toString( getDimensionsXYZCT() );
		info += "\nTime-points: " + getDimensionsXYZCT()[4];
		if ( getVoxelUnit() == null )
		{
			info += "\nVoxel size [???]: " + Arrays.toString( getVoxelSize() );
		}
		else
		{
			info += "\nVoxel size [" + getVoxelUnit().getSymbol() + "]: " + Arrays.toString( getVoxelSize() );
		}

		return info;
	}

	public int[] getCellDims()
	{
		return cellDims;
	}

	/**
	 * Replaces the cachedCellImg that backs the rai of this image,
	 * leaving all modifications (views and conversions) intact.
	 *
	 * @param cellDims
	 * @param cacheType
	 * @param cacheSize
	 */
	public void setCache( int[] cellDims, DiskCachedCellImgOptions.CacheType cacheType, int cacheSize )
	{
		Logger.info( "Setting cache of image: " + getName() );
		Logger.info( "  Cache cell dimensions: " + Arrays.toString( cellDims ) );
		Logger.info( "  Cache size: " + cacheSize );
		Logger.info( "  Cache type: " + cacheType.toString() );

		this.cellDims = cellDims;
		RandomAccessibleInterval< R > cachedCellImg = cachedCellImgCreator.createCachedCellImg( cellDims, cacheType, cacheSize );

		if ( rai == null )
			rai = cachedCellImg;
		else
			rai = new CachedCellImgReplacer( rai, cachedCellImg ).get();
	}

	public void setVolumeCache( DiskCachedCellImgOptions.CacheType cacheType, int cacheSize )
	{
		this.cellDims[ DimensionOrder.X ] = (int) rawDataDimensions[ DimensionOrder.X ];
		this.cellDims[ DimensionOrder.Y ] = (int) rawDataDimensions[ DimensionOrder.Y ];
		this.cellDims[ DimensionOrder.Z ] = (int) rawDataDimensions[ DimensionOrder.Z ];
		this.cellDims[ DimensionOrder.C ] = 1;
		this.cellDims[ DimensionOrder.T ] = 1;
		setCache( cellDims, cacheType, cacheSize );
	}
}
