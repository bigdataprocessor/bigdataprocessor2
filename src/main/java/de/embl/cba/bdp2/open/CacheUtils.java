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
package de.embl.cba.bdp2.open;

import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.open.fileseries.FileInfos;
import de.embl.cba.bdp2.open.fileseries.FileSeriesFileType;

import static de.embl.cba.bdp2.open.fileseries.TIFFDecompressor.NONE;

public abstract class CacheUtils
{
	public static final int MAX_ARRAY_LENGTH = Integer.MAX_VALUE - 100;

	public static boolean isPlaneWiseCompressed( FileInfos fileInfos )
	{
		if ( fileInfos.fileType.equals( FileSeriesFileType.TIFF_PLANES ) || fileInfos.fileType.equals( FileSeriesFileType.TIFF_STACKS ) )
		{
			if ( fileInfos.numTIFFStrips == 1 && fileInfos.compression != NONE )
			{
			   return true;
			}
		}
		return false;
	}

	public static int[] volumeWiseCellDims( long[] imageDimsXYZ )
	{
		long cellDimX = imageDimsXYZ[ 0 ];
		long cellDimY = imageDimsXYZ[ 1 ];
		long cellDimZ = imageDimsXYZ[ 2 ];

		long numPixels = cellDimX * cellDimY * cellDimZ;

		if ( numPixels > MAX_ARRAY_LENGTH )
		{
			Logger.info( "Adapting cache cell size in Z to comply with java array indexing limit.");
			Logger.info( "Desired cell size in Z: " + cellDimZ );
			cellDimZ = MAX_ARRAY_LENGTH / ( cellDimX * cellDimY ) ;
			Logger.info( "Adapted cache cell size in Z: " + cellDimZ );
		}

		return new int[]{ (int) cellDimX, (int) cellDimY, (int) cellDimZ, 1, 1 };
	}

	public static int[] planeWiseCellDims( long[] imageDimsXYZ, int bitDepth, boolean loadWholePlane )
	{
		int[] cellDimsXYZCT = new int[ 5 ];

		if ( loadWholePlane )
		{
			cellDimsXYZCT[ 0 ] = (int) imageDimsXYZ[ 0 ];
			cellDimsXYZCT[ 1 ] = (int) imageDimsXYZ[ 1 ];
		}
		else
		{
			// try to be smart and not load the whole plane for faster updates

			// load whole rows
			// TODO: is this always good? For TIFF images probably for sure
			cellDimsXYZCT[ 0 ] = (int) imageDimsXYZ[ 0 ];

			// TODO: check more whether this makes sense
			final int bytesPerRow = (int) imageDimsXYZ[ 0 ] * bitDepth / 8;
			final double megaBitsPerPlane = imageDimsXYZ[ 0 ] * imageDimsXYZ[ 1 ] * bitDepth / 1000000.0;
			final int numRowsPerFileSystemBlock = 4096 / bytesPerRow;

			if ( megaBitsPerPlane > 10.0 ) // would take longer to load than one second at 10 MBit/s bandwidth
			{
				cellDimsXYZCT[ 1 ] = ( int ) Math.ceil( imageDimsXYZ[ 1 ] / 3.0 ); // TODO: find a better value?
			}
			else
			{
				cellDimsXYZCT[ 1 ] = (int) imageDimsXYZ[ 1 ];
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
}
