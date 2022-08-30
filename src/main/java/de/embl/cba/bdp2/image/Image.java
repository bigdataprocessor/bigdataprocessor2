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
package de.embl.cba.bdp2.image;

import ch.epfl.biop.bdv.bioformats.BioFormatsMetaDataHelper;
import de.embl.cba.bdp2.dialog.DisplaySettings;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.open.CacheUtils;
import de.embl.cba.bdp2.open.CachedCellImgCreator;
import de.embl.cba.bdp2.open.bioformats.BioFormatsCachedCellImgCreator;
import de.embl.cba.bdp2.save.CachedCellImgReplacer;
import de.embl.cba.bdp2.utils.DimensionOrder;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.bdp2.viewer.ImageViewer;
import mpicbg.imglib.multithreading.Stopable;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.DiskCachedCellImgOptions;
import net.imglib2.cache.img.optional.CacheOptions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Util;
import ome.units.quantity.Length;
import ome.units.unit.Unit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// TODO: make an interface and get rid of CachedCellImgCreator?!!

public class Image< R extends RealType< R > & NativeType< R > >
{
	public static final String WARNING_VOXEL_SIZE = "Please check voxel size.";

	/*
	 * The cachedCellImg loads the data for the rai.
	 * This must be 5D with dimension order XYZCT.
	 *
	 * The optimal sizes of the cells depend on the use-case
	 */
	private final CachedCellImgCreator< R > cachedCellImgCreator;
	private int[] cachedCellDims;
	private long[] rawDataDimensions;

	/*
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
	private double[] voxelDimensions;
	private Unit< Length > voxelUnit;

	// Note: currently not used, consider moving to a branch
	private ArrayList< Stopable > stopables = new ArrayList<>(  );
	private ImageViewer< R > viewer;
	private R type;
	private boolean supportsMultiThreadedReading = false;

	public Image( CachedCellImgCreator< R > cachedCellImgCreator )
	{
		this.cachedCellImgCreator = cachedCellImgCreator;
		this.name = cachedCellImgCreator.getImageName();
		this.channelNames = cachedCellImgCreator.getChannelNames();
		this.voxelDimensions = cachedCellImgCreator.getVoxelSize();
		this.voxelUnit = cachedCellImgCreator.getVoxelUnit();

		// cache size is ignored for SOFTREF
		setCache( cachedCellImgCreator.getDefaultCellDimsXYZCT(), CacheOptions.CacheType.SOFTREF, 1000 );
		this.rai = cachedCellImgCreator.createCachedCellImg( cachedCellDims, CacheOptions.CacheType.BOUNDED, 100 );
		this.rawDataDimensions = this.getDimensionsXYZCT();
		this.type = Util.getTypeFromInterval( rai );
	}

	public boolean supportsMultiThreadedReading()
	{
		return supportsMultiThreadedReading;
	}

	public void supportsMultiThreadedReading( boolean supportsMultiThreadedReading )
	{
		this.supportsMultiThreadedReading = supportsMultiThreadedReading;
	}

	/*
	 * Copy constructor.
	 */
	public Image( Image< R > image )
	{
		this.cachedCellImgCreator = image.cachedCellImgCreator; // use same cache, thus by reference
		this.rai = image.rai; // practically immutable
		this.name = image.name; // immutable
		this.channelNames = image.channelNames.clone();
		this.voxelDimensions = image.voxelDimensions.clone();
		this.voxelUnit = image.getVoxelUnit();
		this.cachedCellDims = image.cachedCellDims.clone();
		this.rawDataDimensions = image.rawDataDimensions.clone();
		this.viewer = image.viewer;
		this.type = image.type;
	}

	public long[] getDimensionsXYZCT()
	{
		final long[] longs = new long[ rai.numDimensions() ];
		rai.dimensions( longs );
		return longs;
	}

	public String getTypeAsString()
	{
		final R type = Util.getTypeFromInterval( rai );
		if ( type instanceof UnsignedByteType )
			return "unsigned 8 bit";
		else if ( type instanceof UnsignedShortType )
			return "unsigned 16 bit";
		else if ( type instanceof FloatType )
			return "float";
		else
			throw new RuntimeException("Could not determine the bit-depth.");
	}

	public R getType()
	{
		return type;
	}

	public int getBitDepth()
	{
		if ( type instanceof UnsignedByteType )
			return 8;
		else if ( type instanceof UnsignedShortType )
			return 16;
		else if ( type instanceof FloatType )
			return 32;
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

	/*
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
		type = Util.getTypeFromInterval( rai );
	}

	public double[] getVoxelDimensions()
	{
		return voxelDimensions;
	}

	public void setName( String name )
	{
		this.name = name;
	}

	// Adapt the voxel size, e.g., in case a processing step like
	// binning was applied.
	public void setVoxelDimensions( double... voxelDimensions )
	{
		this.voxelDimensions = voxelDimensions;
	}

	public Unit< Length > getVoxelUnit()
	{
		return voxelUnit;
	}

	public void setVoxelUnit( Unit< Length > voxelUnit )
	{
		this.voxelUnit = voxelUnit;
	}

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
		info += "\nData type: " + getTypeAsString();
		info += "\nSize X,Y,Z,C,T [Voxels]: " + Arrays.toString( getDimensionsXYZCT() );
		if ( getVoxelUnit() == null )
		{
			info += "\nVoxel size [???]: " + Arrays.toString( getVoxelDimensions() );
		}
		else
		{
			info += "\nVoxel size [" + getVoxelUnit().getSymbol() + "]: " + Arrays.toString( getVoxelDimensions() );
		}
		info += "\nCache size X,Y,Z,C,T [Voxels]: " + Arrays.toString( getCachedCellDims() );

		return info;
	}

	public int[] getCachedCellDims()
	{
		return cachedCellDims;
	}

    // Replaces the cachedCellImg that backs the rai of this image,
	// leaving all modifications (views and conversions) intact.
	public void setCache( int[] cellDims, CacheOptions.CacheType cacheType, int cacheSize )
	{
//		if ( cachedCellImgCreator instanceof BioFormatsCachedCellImgCreator )
//		{
//			Logger.info( "Adapting cache size currently not supported when loading with Bio-Formats." );
//			return; // changing the cache does currently not work
//		}

		this.cachedCellDims = cellDims;

		RandomAccessibleInterval< R > cachedCellImg = cachedCellImgCreator.createCachedCellImg( cellDims, cacheType, cacheSize );

		if ( rai == null )
			rai = cachedCellImg;
		else
			rai = new CachedCellImgReplacer( rai, cachedCellImg ).get();
	}

	// Sets the cache dimensions such that one cell is one volume.
	public void setVolumeCache( CacheOptions.CacheType cacheType, int cacheSize )
	{
		if ( cachedCellImgCreator instanceof BioFormatsCachedCellImgCreator )
		{
			Logger.info( "[WARN] Adapting cache size currently not supported when loading with Bio-Formats. The saving may thus be slower than when reading without Bio-Formats." );
			return; // changing the cache does currently not work
		}

		cachedCellDims = CacheUtils.volumeWiseCellDims( rawDataDimensions );

		if ( cachedCellDims[ DimensionOrder.Z ] < ( int ) rawDataDimensions[ DimensionOrder.Z ] )
		{
			cacheSize = (int) Math.ceil( 1.0D * ( int ) rawDataDimensions[ DimensionOrder.Z ] / cachedCellDims[ DimensionOrder.Z ] );
			Logger.info("Adapting cache size to " + cacheSize +" volumes." );
		}

		setCache( cachedCellDims, cacheType, cacheSize );
	}

	// TODO: this could be removed if we used SourceAndConverter instead of the current Image class
	public void setViewer( ImageViewer< R > viewer )
	{
		this.viewer = viewer;
	}

	// TODO: this could be removed if we used SourceAndConverter instead of the current Image class
	public List< DisplaySettings > getDisplaySettings()
	{
		if ( viewer != null ) return viewer.getDisplaySettings();
		else return null;
	}
}
