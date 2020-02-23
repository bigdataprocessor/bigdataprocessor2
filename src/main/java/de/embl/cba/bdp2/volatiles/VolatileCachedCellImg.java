package de.embl.cba.bdp2.volatiles;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.cache.img.RandomAccessibleCacheLoader;
import net.imglib2.img.basictypeaccess.AccessFlags;
import net.imglib2.img.basictypeaccess.array.ShortArray;
import net.imglib2.img.basictypeaccess.volatiles.array.VolatileShortArray;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.util.Intervals;
import net.imglib2.cache.ref.SoftRefLoaderCache;
import net.imglib2.img.cell.Cell;

public class VolatileCachedCellImg
{
	/**
	 * TODO: Make work for other than ShortType
	 * @param raiXYZCT
	 * @return
	 */
	public static CachedCellImg asVolatileCachedCellImg( RandomAccessibleInterval< ShortType > raiXYZCT )
	{
		final CellGrid grid = new CellGrid( Intervals.dimensionsAsLongArray( raiXYZCT ),
				new int[]{ 16, 16, 16, 1, 1 } );

		final RandomAccessibleCacheLoader< ShortType, ShortArray, VolatileShortArray > loader 				= RandomAccessibleCacheLoader.get(
						grid,
						raiXYZCT,
						AccessFlags.setOf( AccessFlags.VOLATILE ) );

		final CachedCellImg cachedCellImg = new CachedCellImg(
				grid,
				new ShortType(),
				new SoftRefLoaderCache< Long, Cell< VolatileShortArray > >().withLoader( loader ),
				new VolatileShortArray( 1, true ) );

		return cachedCellImg;
	}
}
