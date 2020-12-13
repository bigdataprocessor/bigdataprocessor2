package de.embl.cba.bdp2.open.fileseries;

import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.utils.DimensionOrder;
import net.imglib2.cache.img.SingleCellArrayImg;

import java.io.File;
import java.io.RandomAccessFile;

public class PartialTiffPlaneCellLoader implements Runnable
{
	private final SingleCellArrayImg cell;
	private Thread t;
	private String threadName;

	// todo: make the compression modes part of the fi object?

	/**
	 * 16-bit signed integer (-32768-32767). Imported signed images
	 * are converted to unsigned by adding 32768.
	 */
	public static final int GRAY16_SIGNED = 1;

	/**
	 * 16-bit unsigned integer (0-65535).
	 */
	public static final int GRAY16_UNSIGNED = 2;

	// uncompress
	// byte[][] symbolTable = new byte[4096][1];

	private final String directory;
	private final BDP2FileInfo fi;
	int z;
	private int bytesPerRow;

	public PartialTiffPlaneCellLoader( SingleCellArrayImg cell, int z, String directory, BDP2FileInfo fi )
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

		int ys = (int) cell.min( DimensionOrder.Y );
		int ye = (int) cell.max( DimensionOrder.Y );
		int xs = (int) cell.min( DimensionOrder.X );
		int nx = (int) cell.dimension( DimensionOrder.X );
		int ny = (int) cell.dimension( DimensionOrder.Y );

		TiffRowsReader rowsReader = new TiffRowsReader();

		try
		{
			File file = new File( new File( directory, fi.directory ).getAbsolutePath(), fi.fileName );
			RandomAccessFile inputStream = new RandomAccessFile( file, "r" );
			bytes = rowsReader.read( fi, inputStream, ys, ye );
			inputStream.close();
		}
		catch ( Exception e )
		{
			throw new RuntimeException( e );
		}

		// this may differ from ny, because of the presence of (compressed) strips
		int rowsRead = rowsReader.getRowMax() - rowsReader.getRowMin();

		if ( rowsReader.hasStrips() )
		{
			final int rps = fi.rowsPerStrip;
			final int stripStart = rowsReader.getStripMin();
			final int stripEnd = rowsReader.getStripMax();

			if ( ( fi.compression == TiffDecompressor.NONE ) || ( fi.compression == 0 ) )
			{
				// do nothing
			}
			else if ( fi.compression == TiffDecompressor.LZW )
			{
				bytes = lzwUncompress( bytes, rps, stripStart, stripEnd );
			}
			else if ( fi.compression == TiffDecompressor.PACK_BITS )
			{
				bytes = packbitUncompress( bytes, rps, stripStart, stripEnd );
			}
			else if ( fi.compression == TiffDecompressor.ZIP )
			{
				bytes = zipUncompress( bytes, rps, stripStart, stripEnd );
			}
			else
			{
				throw new RuntimeException( "Tiff compression not implemented: " + fi.compression );
			}
		}
		else // no strips
		{
			// NOTE:
			//   if the data is compressed without any strips, afaik, this
			//   always means that the full plane
			if ( fi.compression == TiffDecompressor.ZIP )
			{
				bytes = TiffDecompressor.decompressZIP( bytes );
			}
			else if ( fi.compression == TiffDecompressor.LZW )
			{
				bytes = TiffDecompressor.decompressLZW( bytes, bytesPerRow * rowsRead );
			}
			else
			{
				//
			}

			if ( Logger.getLevel().equals( Logger.Level.Debug ) )
			{
				Logger.debug( "z: " + z );
				Logger.debug( "buffer.length : " + bytes.length );
				Logger.debug( "imWidth [bytes] : " + bytesPerRow );
				Logger.debug( "rows read [#] : " + rowsRead );
			}
		}



		// read the relevant pixels from the byte buffer into the cell array
		// due to the reading from strips, the buffer may contain
		// both more rows and columns than the cell array

		int cellOffset = ( int ) ( ( z - cell.min( DimensionOrder.Z ) ) * cell.dimension( 0 ) * cell.dimension( 1 ));

		int byteBufferRowOffset = ys - rowsReader.getRowMin();

		if ( fi.bytesPerPixel == 1 )
		{
			setBytePixelsCropXY(
					( byte[] ) cell.getStorageArray(),
					cellOffset,
					byteBufferRowOffset,
					ny,
					xs,
					nx,
					bytesPerRow,
					bytes  );
		}
		else if ( fi.bytesPerPixel == 2 )
		{
			setShortPixelsCropXY(
					( short[] ) cell.getStorageArray(),
					cellOffset,
					byteBufferRowOffset,
					ny,
					xs,
					nx,
					bytesPerRow,
					bytes );
		}
		else
		{
			Logger.error( "Unsupported bit depth: " + 8 * fi.bytesPerPixel );
			return;
		}
	}

	private byte[] lzwUncompress( byte[] bytes, int rps, int ss, int se )
	{
		// init to hold all data present in the uncompressed strips
		byte[] unCompressedBuffer = new byte[ ( se - ss + 1 ) * rps * bytesPerRow ];

		int pos = 0;
		for ( int s = ss; s <= se; s++ )
		{
			int stripLength = ( int ) fi.stripLengths[ s ];
			byte[] strip = new byte[ stripLength ];

			try
			{
				System.arraycopy( bytes, pos, strip, 0, stripLength );
			}
			catch ( Exception e )
			{
				Logger.info( "" + e.toString() );
				Logger.info( "------- s [#] : " + s );
				Logger.info( "stripLength [bytes] : " + strip.length );
				Logger.info( "pos [bytes] : " + pos );
				Logger.info( "pos + stripLength [bytes] : " + ( pos + stripLength ) );
				Logger.info( "buffer[.length : " + bytes.length );
				Logger.info( "imWidth [bytes] : " + bytesPerRow );
				Logger.info( "rows per strip [#] : " + rps );
				Logger.info( "(s - ss) * imByteWidth * rps [bytes] : " + ( ( s - ss ) * bytesPerRow *
						rps ) );
				Logger.info( "unCompressedBuffer.length [bytes] : " + unCompressedBuffer.length );
			}

			//info("strip.length " + strip.length);
			// uncompress strip

			strip = TiffDecompressor.decompressLZW( strip, bytesPerRow * rps );

			// put uncompressed strip into large array
			System.arraycopy( strip, 0, unCompressedBuffer, ( s - ss ) * bytesPerRow * rps, bytesPerRow * rps );

			pos += stripLength;
		}

		bytes = unCompressedBuffer;
		return bytes;
	}

	private byte[] packbitUncompress( byte[] bytes, int rps, int ss, int se )
	{
		// init to hold all data present in the uncompressed strips
		byte[] uncompressedBytes = new byte[ ( se - ss + 1 ) * rps * bytesPerRow ];

		int pos = 0;
		for ( int s = ss; s <= se; s++ )
		{
			int stripLength = ( int ) fi.stripLengths[ s ];
			byte[] strip = new byte[ stripLength ];

			// get strip from core data
			try
			{
				System.arraycopy( bytes, pos, strip, 0, stripLength );
			} catch ( Exception e )
			{
				Logger.info( "" + e.toString() );
				Logger.info( "------- s [#] : " + s );
				Logger.info( "stripLength [bytes] : " + strip.length );
				Logger.info( "pos [bytes] : " + pos );
				Logger.info( "pos + stripLength [bytes] : " + ( pos + stripLength ) );
				Logger.info( "buffer.length : " + bytes.length );
				Logger.info( "imWidth [bytes] : " + bytesPerRow );
				Logger.info( "rows per strip [#] : " + rps );
				Logger.info( "(s - ss) * imByteWidth * rps [bytes] : " + ( ( s - ss ) * bytesPerRow *
						rps ) );
				Logger.info( "unCompressedBuffer.length [bytes] : " + uncompressedBytes.length );
			}

			// TODO: maybe implement a faster version without dynamic byte array allocation?
			strip =  TiffDecompressor.packBitsUncompressFast( strip, bytesPerRow * rps );

			// put uncompressed strip into large array
			System.arraycopy( strip, 0, uncompressedBytes, ( s - ss ) * bytesPerRow * rps, bytesPerRow * rps );

			pos += stripLength;
		}

		bytes = uncompressedBytes;
		return bytes;
	}

	private byte[] zipUncompress( byte[] bytes, int rps, int ss, int se )
	{
		// init to hold all data present in the uncompressed strips
		byte[] unCompressedBuffer = new byte[ ( se - ss + 1 ) * rps * bytesPerRow ];

		int pos = 0;

		for ( int s = ss; s <= se; s++ )
		{
			// TODO: multithreading here?
			int compressedStripLength = ( int ) fi.stripLengths[ s ];
			byte[] strip = new byte[ compressedStripLength ];

			try
			{
				System.arraycopy( bytes, pos, strip, 0, compressedStripLength );
			}
			catch ( Exception e )
			{
				Logger.info( "" + e.toString() );
				Logger.info( "------- s [#] : " + s );
				Logger.info( "stripLength [bytes] : " + strip.length );
				Logger.info( "pos [bytes] : " + pos );
				Logger.info( "pos + stripLength [bytes] : " + ( pos + compressedStripLength ) );

				Logger.info( "buffer[z-zs].length : " + bytes.length );
				Logger.info( "imWidth [bytes] : " + bytesPerRow );
				Logger.info( "rows per strip [#] : " + rps );
				Logger.info( "(s - ss) * imByteWidth * rps [bytes] : " + ( ( s - ss ) * bytesPerRow *
						rps ) );
				Logger.info( "unCompressedBuffer.length [bytes] : " + unCompressedBuffer.length );
			}

			/** TIFF Adobe ZIP support contributed by Jason Newton. */
			strip = TiffDecompressor.decompressZIP( strip );

			// put uncompressed strip into large array
			System.arraycopy(
					strip,
					0,
					unCompressedBuffer,
					( s - ss ) * bytesPerRow * rps,
					bytesPerRow * rps );

			pos += compressedStripLength;
		}

		bytes = unCompressedBuffer;

		return bytes;
	}

	private void setShortPixelsCropXY( short[] cellArray, int cellOffset, int ys, int ny, int xs, int nx, int imByteWidth, byte[] buffer )
	{
		final int bytesPerPixel = fi.bytesPerPixel;
		final boolean intelByteOrder = fi.intelByteOrder;
		final boolean signed = fi.fileType == GRAY16_SIGNED;

		if ( bytesPerPixel != 2 )
		{
			Logger.error( "Unsupported bit depth: " + bytesPerPixel * 8 );
		}

		int i = cellOffset;
		int bs, be;

		for ( int y = ys; y < ys + ny; y++ )
		{
			bs = y * imByteWidth + xs * bytesPerPixel;
			be = bs + nx * bytesPerPixel;

			if ( intelByteOrder )
			{
				if ( signed )
					for ( int x = bs; x < be; x += 2 )
						cellArray[ i++ ] = ( short ) ( ( ( ( buffer[ x + 1 ] & 0xff ) << 8 ) | ( buffer[ x ] & 0xff ) ) + 32768 );
				else
					for ( int x = bs; x < be; x += 2 )
						cellArray[ i++ ] = ( short ) ( ( ( buffer[ x + 1 ] & 0xff ) << 8 ) | ( buffer[ x ] & 0xff ) );
			}
			else
			{
				if ( signed )
					for ( int x = bs; x < be; x += 2 )
						cellArray[ i++ ] = ( short ) ( ( ( ( buffer[ x ] & 0xff ) << 8 ) | ( buffer[ x + 1 ] & 0xff ) ) + 32768 );
				else
					for ( int x = bs; x < be; x += 2 )
						cellArray[ i++ ] = ( short ) ( ( ( buffer[ x ] & 0xff ) << 8 ) | ( buffer[ x + 1 ] & 0xff ) );
			}
		}
	}

	private static void setBytePixelsCropXY( byte[] cellArray, int cellArrayPixelPosition, int ys, int ny, int xs, int nx, int imByteWidth, byte[] buffer )
	{
		int bs, be;

		for ( int y = ys; y < ys + ny; y++ )
		{
			bs = y * imByteWidth + xs;
			be = bs + nx;
			for ( int x = bs; x < be; ++x )
			{
				// TODO: Use System.arrayCopy?
				cellArray[ cellArrayPixelPosition++ ] = buffer[ x ];
			}
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
