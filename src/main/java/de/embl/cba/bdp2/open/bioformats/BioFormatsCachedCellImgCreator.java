package de.embl.cba.bdp2.open.bioformats;

import de.embl.cba.bdp2.open.CachedCellImgCreator;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.cache.img.DiskCachedCellImgOptions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import ome.units.quantity.Length;
import ome.units.unit.Unit;

public class BioFormatsCachedCellImgCreator < R extends RealType< R > & NativeType< R > > implements CachedCellImgCreator< R >
{
	@Override
	public String getImageName()
	{
		return null;
	}

	@Override
	public String[] getChannelNames()
	{
		return new String[ 0 ];
	}

	@Override
	public ARGBType[] getChannelColors()
	{
		return new ARGBType[ 0 ];
	}

	@Override
	public double[] getVoxelSize()
	{
		return new double[ 0 ];
	}

	@Override
	public Unit< Length > getVoxelUnit()
	{
		return null;
	}

	@Override
	public int[] getDefaultCellDimsXYZCT()
	{
		return new int[ 0 ];
	}

	@Override
	public CachedCellImg< R, ? > createCachedCellImg( int[] cellDimsXYZCT, DiskCachedCellImgOptions.CacheType cacheType, long cacheSize )
	{
		return null;
	}
}
