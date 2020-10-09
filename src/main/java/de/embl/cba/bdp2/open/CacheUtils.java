package de.embl.cba.bdp2.open;

import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.open.fileseries.FileInfos;

import static de.embl.cba.bdp2.open.fileseries.TiffAndHdf5Opener.COMPRESSION_NONE;

public abstract class CacheUtils
{
	public static final int MAX_ARRAY_LENGTH = Integer.MAX_VALUE - 100;

	public static boolean isPlaneWiseCompressed( FileInfos fileInfos )
	{
		if ( fileInfos.fileType.equals( OpenFileType.TIFF_PLANES ) || fileInfos.fileType.equals( OpenFileType.TIFF_STACKS ) )
		{
			if ( fileInfos.numTiffStrips == 1 && fileInfos.compression != COMPRESSION_NONE )
			{
			   return true;
			}
		}
		return false;
	}

	/**
	 * Compute cell dimensions that are good for fast z-plane-wise loading
	 *
	 * @param imageDimsXYZ
	 * @param bitDepth
	 * @param loadWholePlane
	 * @return cellDimsXYZCT
	 */
	public static int[] planeWiseCellDims( int[] imageDimsXYZ, int bitDepth, boolean loadWholePlane )
	{
		int[] cellDimsXYZCT = new int[ 5 ];

		if ( loadWholePlane )
		{
			cellDimsXYZCT[ 0 ] = imageDimsXYZ[ 0 ];
			cellDimsXYZCT[ 1 ] = imageDimsXYZ[ 1 ];
		}
		else
		{
			// try to be smart and not load the whole plane for faster updates

			// load whole rows
			// TODO: is this always good? For Tiff images probably for sure
			cellDimsXYZCT[ 0 ] = imageDimsXYZ[ 0 ];

			// TODO: check more whether this makes sense
			final int bytesPerRow = imageDimsXYZ[ 0 ] * bitDepth / 8;
			final double megaBitsPerPlane = imageDimsXYZ[ 0 ] * imageDimsXYZ[ 1 ] * bitDepth / 1000000.0;
			final int numRowsPerFileSystemBlock = 4096 / bytesPerRow;

			if ( megaBitsPerPlane > 10.0 ) // would take longer to load than one second at 10 MBit/s bandwidth
			{
				cellDimsXYZCT[ 1 ] = ( int ) Math.ceil( imageDimsXYZ[ 1 ] / 3.0 ); // TODO: find a better value?
			}
			else
			{
				cellDimsXYZCT[ 1 ] = imageDimsXYZ[ 1 ];
			}
		}

		// load one plane
		cellDimsXYZCT[ 2 ] = 1;

		// load one channel
		cellDimsXYZCT[ 3 ] = 1;

		// load one timepoint
		cellDimsXYZCT[ 4 ] = 1;

		return cellDimsXYZCT;
	}

	/**
	 * Create cell dimensions to load a whole channel and time point in one go.
	 *
	 * This method takes care that the cells for one such volume are not too large
	 * in terms of the Java array indexing limit.
	 *
 	 * @param imageDimsXYZ
	 * @return
	 */
	public static int[] volumeWiseCellDims( int[] imageDimsXYZ )
	{
		int cellDimX = imageDimsXYZ[ 0 ];
		int cellDimY = imageDimsXYZ[ 1 ];
		int cellDimZ = imageDimsXYZ[ 2 ];

		long numPixels = (long) cellDimX * (long) cellDimY * (long) cellDimZ;

		if ( numPixels > MAX_ARRAY_LENGTH )
		{
			Logger.info( "Adapting cell size in Z to satisfy java array indexing limit.");
			Logger.info( "Desired cell size in Z: " + cellDimZ );
			cellDimZ = MAX_ARRAY_LENGTH / ( cellDimX * cellDimY ) ;
			Logger.info( "Adapted cell size in Z: " + cellDimZ );
		}

		return new int[]{ cellDimX, cellDimY, cellDimZ, 1, 1 };
	}
}
