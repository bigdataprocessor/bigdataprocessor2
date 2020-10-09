package de.embl.cba.bdp2.open;

import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.cache.img.DiskCachedCellImgOptions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public interface CachedCellImgCreator< R extends RealType< R > & NativeType< R > >
{
	String getImageName();

	String[] getChannelNames();

	double[] getVoxelSize();

	String getVoxelUnit(); // TODO: maybe rather something with a controlled vocabulary here?

	CachedCellImg< R, ? > createCachedCellImg( int[] cellDimsXYZCT, DiskCachedCellImgOptions.CacheType cacheType, long cacheSize );

	// TODO: maybe add something like getDisplaySettings();
}
