package de.embl.cba.bdp2.open.fileseries;

import de.embl.cba.bdp2.log.Logger;

import java.io.RandomAccessFile;

public class TiffRowsReader
{
	private int stripMin;
	private int stripMax;
	private boolean hasStrips = false;
	private int rowMin;
	private int rowMax;

	/**
	 * TODO: Is RandomAccessFile really the fastest here?
	 * @return
	 */
	public byte[] read( BDP2FileInfo fi, RandomAccessFile in, int requestedRowMin, int requestedRowMax )
	{
		int readLength;
		long readStart;
		this.rowMin = requestedRowMin;
		this.rowMax = requestedRowMax;

		if ( fi.stripOffsets != null && fi.stripOffsets.length > 1 )
		{
			hasStrips = true;
		}

		if ( hasStrips )
		{
			// convert rows to strips
			int rps = fi.rowsPerStrip;
			stripMin = ( int ) ( 1.0 * requestedRowMin / rps );
			stripMax = ( int ) ( 1.0 * requestedRowMax / rps );
			// adapt to how many rows we actually rea
			// d
			rowMin = stripMin * rps;
			rowMax = stripMax * rps;

			readStart = fi.stripOffsets[ stripMin ];

			readLength = 0;
			if ( stripMax >= fi.stripLengths.length )
				Logger.warn( "Strip is out of bounds" );

			for ( int s = stripMin; s <= stripMax; s++ )
				readLength += fi.stripLengths[ s ];
		}
		else // no strips (or all data in one compressed strip)
		{
			if ( fi.compression == TiffDecompressor.ZIP || fi.compression == TiffDecompressor.LZW || fi.compression == TiffDecompressor.PACK_BITS )
			{
				// read all data
				readStart = fi.offset;
				readLength = ( int ) fi.stripLengths[ 0 ];
			}
			else
			{
				// read subset
				// convert rows to bytes
				readStart = fi.offset + requestedRowMin * fi.width * fi.bytesPerPixel;
				readLength = ( ( requestedRowMax - requestedRowMin ) + 1 ) * fi.width * fi.bytesPerPixel; // requestedRowMax is -1 sometimes why?
			}
		}

		if ( readLength <= 0 )
		{
			Logger.warn( "file type: Tiff" );
			Logger.warn( "hasStrips: " + hasStrips );
			Logger.warn( "core from [bytes]: " + readStart );
			Logger.warn( "core to [bytes]: " + ( readStart + readLength - 1 ) );
			Logger.warn( "ys: " + requestedRowMin );
			Logger.warn( "requestedRowMax: " + requestedRowMax );
			Logger.warn( "fileInfo.compression: " + fi.compression );
			Logger.warn( "fileInfo.height: " + fi.height );
			Logger.error( "Error during file reading. See log window for more information" );
			return ( null );
		}

		byte[] buffer = new byte[ readLength ];

		try
		{
			if ( readStart + readLength - 1 <= in.length() )
			{
				in.seek( readStart ); // TODO: is this really slow??
				in.readFully( buffer );
			}
			else
			{
				Logger.warn( "The requested data exceeds the file length; no data was read." );
				Logger.warn( "file type: Tiff" );
				Logger.warn( "hasStrips: " + hasStrips );
				Logger.warn( "file length [bytes]: " + in.length() );
				Logger.warn( "attempt to read until [bytes]: " + ( readStart + readLength - 1 ) );
				Logger.warn( "ys: " + requestedRowMin );
				Logger.warn( "requestedRowMax: " + requestedRowMax );
				Logger.warn( "fileInfo.compression: " + fi.compression );
				Logger.warn( "fileInfo.height: " + fi.height );
			}
		}
		catch ( Exception e )
		{
			Logger.warn( e.toString() );
		}

		return buffer;
	}

	public int getStripMin()
	{
		return stripMin;
	}

	public int getStripMax()
	{
		return stripMax;
	}

	public boolean hasStrips()
	{
		return hasStrips;
	}

	public int getRowMin()
	{
		return rowMin;
	}

	public int getRowMax()
	{
		return rowMax;
	}
}
