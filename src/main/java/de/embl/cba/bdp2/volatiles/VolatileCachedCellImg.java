package de.embl.cba.bdp2.volatiles;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.read.CachedCellImgReader;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.cache.img.RandomAccessibleCacheLoader;
import net.imglib2.img.basictypeaccess.AccessFlags;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.img.basictypeaccess.array.ShortArray;
import net.imglib2.img.basictypeaccess.volatiles.array.VolatileByteArray;
import net.imglib2.img.basictypeaccess.volatiles.array.VolatileShortArray;
import net.imglib2.type.Type;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.util.Intervals;
import net.imglib2.cache.ref.SoftRefLoaderCache;
import net.imglib2.img.cell.Cell;
import net.imglib2.util.Util;

public class VolatileCachedCellImg
{
	/**
	 * TODO: Make work for other than ShortType
	 * @param raiXYZCT
	 * @param grid
	 * @return
	 */
	public static CachedCellImg< UnsignedShortType, VolatileShortArray > asVolatileShortTypeCachedCellImg(
			RandomAccessibleInterval< UnsignedShortType > raiXYZCT, CellGrid grid )
	{
		final RandomAccessibleCacheLoader< UnsignedShortType, ShortArray, VolatileShortArray > loader = RandomAccessibleCacheLoader.get(
				grid,
				raiXYZCT,
				AccessFlags.setOf( AccessFlags.VOLATILE ) );

		final CachedCellImg< UnsignedShortType, VolatileShortArray > cachedCellImg = new CachedCellImg(
				grid,
				new UnsignedShortType(),
				new SoftRefLoaderCache< Long, Cell< VolatileShortArray > >().withLoader( loader ),
				new VolatileShortArray( 1, true ) );

		return cachedCellImg;
	}

	public static CachedCellImg< UnsignedByteType, VolatileByteArray > asVolatileByteTypeCachedCellImg(
			RandomAccessibleInterval< UnsignedByteType > raiXYZCT, CellGrid grid )
	{
		final RandomAccessibleCacheLoader< UnsignedByteType, ByteArray, VolatileByteArray > loader = RandomAccessibleCacheLoader.get(
				grid,
				raiXYZCT,
				AccessFlags.setOf( AccessFlags.VOLATILE ) );

		final CachedCellImg< UnsignedByteType, VolatileByteArray > cachedCellImg = new CachedCellImg(
				grid,
				new UnsignedByteType(),
				new SoftRefLoaderCache< Long, Cell< VolatileByteArray > >().withLoader( loader ),
				new VolatileByteArray( 1, true ) );

		return cachedCellImg;
	}

	public static CachedCellImg getVolatileCachedCellImg( Image< ? > image )
	{
		final Type typeFromInterval = Util.getTypeFromInterval( image.getRai() );
		final CellGrid grid = new CellGrid(
				Intervals.dimensionsAsLongArray( image.getRai() ),
				CachedCellImgReader.getCellDimsXYZCT( image.getRai() ) );

		CachedCellImg cachedCellImg;

		if( UnsignedShortType.class.isInstance( typeFromInterval ) )
		{
			cachedCellImg = asVolatileShortTypeCachedCellImg(
					( RandomAccessibleInterval ) image.getRai(), grid );
		}
		else if ( UnsignedByteType.class.isInstance( typeFromInterval ) )
		{
			cachedCellImg = asVolatileByteTypeCachedCellImg(
					( RandomAccessibleInterval ) image.getRai(), grid );
		}
		else
		{
			throw new UnsupportedOperationException( "Cannot yet create CachedCellImg for type:" + typeFromInterval );
		}

		return cachedCellImg;
	}
}
