package de.embl.cba.bdp2.open;

import de.embl.cba.bdp2.image.Image;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public interface CachedCellImageCreator < R extends RealType< R > & NativeType< R > >
{
	/**
	 *
	 * @return
	 * 			Image based on a CachedCellImg that is created by below method.
	 */
	Image< R > createImage();

	/**
	 * This method is exposed here such that additional cachedCellImgs
	 * with alternative caching parameters can be created.
	 *
	 * The cachedCellImg should use the bounded strategy.
	 *
	 * The initial caching used to create above Image should be optimal
	 * for plane-wise browsing of the data.
	 *
	 * See also: CacheUtils.planeWiseCellDims(...)
	 *
	 * @param	cellDimsXYZCT
	 * @param	cacheSize
	 * @return
	 * 			A CachedCellImg that can be used to create above Image.
	 */
	CachedCellImg< R, ? > createCachedCellImg( int[] cellDimsXYZCT, long cacheSize );
}
