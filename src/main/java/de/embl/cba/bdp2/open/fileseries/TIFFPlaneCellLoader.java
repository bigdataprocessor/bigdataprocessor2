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
package de.embl.cba.bdp2.open.fileseries;

import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.utils.DimensionOrder;
import net.imglib2.cache.img.SingleCellArrayImg;

import java.io.File;
import java.io.RandomAccessFile;

public class TIFFPlaneCellLoader implements Runnable
{
	private final SingleCellArrayImg cell;
	private Thread t;
	private String threadName;

	// todo: make the compression modes part of the fi object?

	/*
	 * 16-bit signed integer (-32768-32767). Imported signed images
	 * are converted to unsigned by adding 32768.
	 */
	public static final int GRAY16_SIGNED = 1;

	/*
	 * 16-bit unsigned integer (0-65535).
	 */
	public static final int GRAY16_UNSIGNED = 2;

	// uncompress
	// byte[][] symbolTable = new byte[4096][1];

	private final String directory;
	private final BDP2FileInfo fi;
	private final int z;
	private final int bytesPerRow;

	public TIFFPlaneCellLoader( SingleCellArrayImg cell, int z, String directory, BDP2FileInfo fi )
	{
		this.cell = cell;
		this.z = z;
		this.directory = directory;
		this.fi = fi;

		bytesPerRow = fi.width * fi.bytesPerPixel;
	}

	public void run()
	{

		if ( fi == null )
		{
			return; // missing file (z-chunk), that's ok, we leave pixels black
		}

		byte[] bytes;

		int minRowRequested = (int) cell.min( DimensionOrder.Y );
		int numRowsRequested = (int) cell.dimension( DimensionOrder.Y );
		int minColRequested = (int) cell.min( DimensionOrder.X );
		int numColsRequested = (int) cell.dimension( DimensionOrder.X );

		TIFFRowsRawReader rowsReader = new TIFFRowsRawReader();

		try
		{
			File file = new File( new File( fi.directory ).getAbsolutePath(), fi.fileName );
			RandomAccessFile inputStream = new RandomAccessFile( file, "r" );
			bytes = rowsReader.read( fi, inputStream, minRowRequested, numRowsRequested, minColRequested, numColsRequested );
			inputStream.close();
		}
		catch ( Exception e )
		{
			throw new RuntimeException( e );
		}

		// this may differ from ny, because of the presence of (compressed) strips
		int minRowRead = rowsReader.getMinRow();
		int numRowsRead = rowsReader.getNumRows();

		if ( rowsReader.hasStrips() )
		{
			if ( rowsReader.isCompressed() )
			{
				bytes = TIFFDecompressor.decompressStrips( bytes, fi.rowsPerStrip, rowsReader.getStripMin(), rowsReader.getStripMax(), bytesPerRow, fi.stripLengths, fi.compression );
			}
		}
		else // no strips
		{
			if ( fi.compression == TIFFDecompressor.ZIP )
			{
				bytes = TIFFDecompressor.decompressZIP( bytes );
			}
			else if ( fi.compression == TIFFDecompressor.LZW )
			{
				bytes = TIFFDecompressor.decompressLZW( bytes, bytesPerRow * numRowsRead );
			}

			if ( Logger.getLevel().equals( Logger.Level.Debug ) )
			{
				Logger.debug( "z: " + z );
				Logger.debug( "buffer.length : " + bytes.length );
				Logger.debug( "imWidth [bytes] : " + bytesPerRow );
				Logger.debug( "rows read [#] : " + numRowsRead );
			}
		}

		// Read the relevant pixels from the byte buffer into the cell array.
		// Due to reading from strips, the buffer may contain
		// both more rows and columns than the cell array and we need to crop
		// the read data.

		final int cellOffset = ( int ) ( ( z - cell.min( DimensionOrder.Z ) ) * cell.dimension( 0 ) * cell.dimension( 1 ));

		final int rowOffsetInBuffer = minRowRequested - minRowRead;
		final int colOffsetInBuffer = minColRequested - rowsReader.getMinCol();
		final int colSurplusInBuffer = rowsReader.getNumCols() - numColsRequested;

		if ( fi.bytesPerPixel == 1 )
		{
			try
			{
				setBytePixelsCropXY(
						( byte[] ) cell.getStorageArray(),
						cellOffset,
						rowOffsetInBuffer,
						numRowsRequested,
						colOffsetInBuffer,
						numColsRequested,
						colSurplusInBuffer,
						bytesPerRow,
						bytes );
			}
			catch ( Exception e )
			{
				e.printStackTrace();
			}
		}
		else if ( fi.bytesPerPixel == 2 )
		{
			try
			{
				setShortPixelsCropXY(
						( short[] ) cell.getStorageArray(),
						cellOffset,
						rowOffsetInBuffer,
						numRowsRequested,
						colOffsetInBuffer,
						numColsRequested,
						colSurplusInBuffer,
						bytesPerRow,
						bytes );
			}
			catch ( Exception e )
			{
				e.printStackTrace();
			}
		}
		else
		{
			Logger.error( "Unsupported bit depth: " + 8 * fi.bytesPerPixel );
			return;
		}
	}

	private void setShortPixelsCropXY( short[] cellArray, int cellPos, int rowOffset, int numRows, int colOffset, int numCols, int colSurplus, int imByteWidth, byte[] buffer )
	{
		final int bytesPerPixel = fi.bytesPerPixel;
		final boolean intelByteOrder = fi.intelByteOrder;
		final boolean signed = fi.fileType == GRAY16_SIGNED;

		int b = rowOffset * imByteWidth + colOffset * bytesPerPixel;

		for ( int row = 0; row < numRows; row++ )
		{
			if ( intelByteOrder )
			{
				if ( signed )
					for ( int col = 0; col < numCols; ++col, b += 2 )
						cellArray[ cellPos++ ] = ( short ) ( ( ( ( buffer[ b + 1 ] & 0xff ) << 8 ) | ( buffer[ b ] & 0xff ) ) + 32768 );
				else
					for ( int col = 0; col < numCols; ++col, b += 2 )
						cellArray[ cellPos++ ] = ( short ) ( ( ( buffer[ b + 1 ] & 0xff ) << 8 ) | ( buffer[ b ] & 0xff ) );
			}
			else
			{
				if ( signed )
					for ( int col = 0; col < numCols; ++col, b += 2 )
						cellArray[ cellPos++ ] = ( short ) ( ( ( ( buffer[ b ] & 0xff ) << 8 ) | ( buffer[ b + 1 ] & 0xff ) ) + 32768 );
				else
					for ( int col = 0; col < numCols; ++col, b += 2 )
						cellArray[ cellPos++ ] = ( short ) ( ( ( buffer[ b ] & 0xff ) << 8 ) | ( buffer[ b + 1 ] & 0xff ) );
			}

			b += ( colSurplus + colOffset ) * bytesPerPixel;
		}
	}

	private static void setBytePixelsCropXY( byte[] cellArray, int cellPos, final int rowOffset, final int numRows, final int colOffset, final int numCols, final int colSurplus, final int imByteWidth, byte[] buffer )
	{
		int b = rowOffset * imByteWidth + colOffset;

		for ( int row = 0; row < numRows; row++ )
		{
			for ( int col = 0; col < numCols; ++col )
			{
				// TODO: Use System.arrayCopy?
				cellArray[ cellPos++ ] = buffer[ b++ ];
			}
			b += colSurplus + colOffset;
		}
	}

	public void start()
	{
		//info("Starting " +  threadName );
		if ( t == null )
		{
			t = new Thread( this, threadName );
			t.start();
		}
	}

}
