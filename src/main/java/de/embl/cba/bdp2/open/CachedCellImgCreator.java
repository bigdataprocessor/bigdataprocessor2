package de.embl.cba.bdp2.open;

import bdv.util.RandomAccessibleSource;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.cache.img.DiskCachedCellImgOptions;
import net.imglib2.cache.img.optional.CacheOptions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import ome.units.quantity.Length;
import ome.units.unit.Unit;


/**
 *
 * @param <R>
 */
public interface CachedCellImgCreator< R extends RealType< R > & NativeType< R > >
{
	String getImageName();

	String[] getChannelNames();

	ARGBType[] getChannelColors();

	double[] getVoxelSize();

	Unit< Length > getVoxelUnit();

	int[] getDefaultCellDimsXYZCT(); // "default": good for fast browsing in BDV

	//CachedCellImg< R, ? > createCachedCellImg( int[] cellDimsXYZCT, CacheOptions.CacheType cacheType, long cacheSize );

	RandomAccessibleInterval< R > createCachedCellImg( int[] cellDimsXYZCT, CacheOptions.CacheType cacheType, long cacheSize );
}
