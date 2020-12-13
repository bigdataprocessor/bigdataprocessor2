package de.embl.cba.bdp2.open.fileseries;

import de.embl.cba.bdp2.log.Logger;
import ij.IJ;
import ij.ImageStack;
import ij.io.BitBuffer;
import net.imglib2.cache.img.SingleCellArrayImg;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.RandomAccessFile;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class PartialTiffPlaneCellLoader implements Runnable
{
	private final SingleCellArrayImg cell;
	private Thread t;
	private String threadName;

	// todo: make the compression modes part of the fi object?

	public static final int JPEG = 4;
	public static final int PACK_BITS = 5;
	public static final int ZIP = 6;
	private static final int CLEAR_CODE = 256;
	private static final int EOI_CODE = 257;

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
	byte[][] symbolTable = new byte[ 16384 ][ 1 ]; // enlarged to be compatible with larger images

	// input
	ImageStack stack;
	byte[][] buffer;
	BDP2FileInfo[] info;
	BDP2FileInfo fi;
	RandomAccessFile in;
	private String directory;
	private final BDP2FileInfo fi;
	int z;
	int zs;
	int ze;
	int dz;
	int ys;
	int ye;
	int ny;
	int xs;
	int xe;
	int nx;
	int imByteWidth;

	public PartialTiffPlaneCellLoader( SingleCellArrayImg cell, int z, String directory, BDP2FileInfo fi )
	{
		this.cell = cell;
		this.z = z;
		this.directory = directory;
		this.fi = fi;
	}

	public void run()
	{
		RandomAccessFile inputStream = null;

		if ( fi == null )
		{Logger.info("Missing file; providing pixels with zeros.");
			return; // leave pixels in the stack black
		}

		File file = new File( new File( directory, fi.directory ).getAbsolutePath(), fi.fileName );
		//File file = new File(directory + fi.fileName);
		try
		{
			inputStream = new RandomAccessFile( file, "r" );

			if ( inputStream == null )
			{
				Logger.error( "Could not open file: " + fi.directory + fi.fileName );
				throw new IllegalArgumentException( "could not open file" );
			}

			if (       ( fi.compression != TiffCellLoader.COMPRESSION_UNKNOWN )
					&& ( fi.compression != TiffCellLoader.COMPRESSION_NONE )
					&& ( fi.compression != TiffCellLoader.LZW )
					&& ( fi.compression != ZIP )
					&& ( fi.compression != PACK_BITS ) )
			{
				Logger.error( "Tiff compression not implemented: fi.compression = " + fi.compression );
				return;
			}

			//startTime = System.currentTimeMillis();
			buffer[ ( z - zs ) / dz ] = readRowsFromTiff( fi, inputStream, ys, ye );
			//readingTime += (System.currentTimeMillis() - startTime);
			inputStream.close();
		}
		catch ( Exception e )
		{
			IJ.handleException( e );
		}

		boolean hasStrips = false;

		if ( ( fi.stripOffsets != null && fi.stripOffsets.length > 1 ) )
		{
			hasStrips = true;
		}

		if ( hasStrips )
		{
			// check what we have core
			int rps = fi.rowsPerStrip;
			int ss = ys / rps; // the int is doing a floor()
			int se = ye / rps;

			if ( ( fi.compression == TiffCellLoader.COMPRESSION_NONE ) || ( fi.compression == 0 ) )
			{
				// do nothing
			}
			else if ( fi.compression == TiffCellLoader.LZW )
			{
				// init to hold all data present in the uncompressed strips
				byte[] unCompressedBuffer = new byte[ ( se - ss + 1 ) * rps * imByteWidth ];

				int pos = 0;
				for ( int s = ss; s <= se; s++ )
				{
					int stripLength = ( int ) fi.stripLengths[ s ];
					byte[] strip = new byte[ stripLength ];

					// get strip from core data
					try
					{
						System.arraycopy( buffer[ ( z - zs ) / dz ], pos, strip, 0, stripLength );
					} catch ( Exception e )
					{
						Logger.info( "" + e.toString() );
						Logger.info( "------- s [#] : " + s );
						Logger.info( "stripLength [bytes] : " + strip.length );
						Logger.info( "pos [bytes] : " + pos );
						Logger.info( "pos + stripLength [bytes] : " + ( pos + stripLength ) );
						Logger.info( "z-zs : " + ( z - zs ) );
						Logger.info( "z-zs/dz : " + ( z - zs ) / dz );
						Logger.info( "buffer[z-zs].length : " + buffer[ z - zs ].length );
						Logger.info( "imWidth [bytes] : " + imByteWidth );
						Logger.info( "rows per strip [#] : " + rps );
						Logger.info( "ny [#] : " + ny );
						Logger.info( "(s - ss) * imByteWidth * rps [bytes] : " + ( ( s - ss ) * imByteWidth *
								rps ) );
						Logger.info( "unCompressedBuffer.length [bytes] : " + unCompressedBuffer.length );
					}

					//info("strip.length " + strip.length);
					// uncompress strip

					strip = lzwUncompress( strip, imByteWidth * rps );

					// put uncompressed strip into large array
					System.arraycopy( strip, 0, unCompressedBuffer, ( s - ss ) * imByteWidth * rps, imByteWidth * rps );

					pos += stripLength;
				}

				buffer[ ( z - zs ) / dz ] = unCompressedBuffer;

			}
			else if ( fi.compression == PACK_BITS )
			{
				// init to hold all data present in the uncompressed strips
				byte[] unCompressedBuffer = new byte[ ( se - ss + 1 ) * rps * imByteWidth ];

				int pos = 0;
				for ( int s = ss; s <= se; s++ )
				{
					int stripLength = ( int ) fi.stripLengths[ s ];
					byte[] strip = new byte[ stripLength ];

					// get strip from core data
					try
					{
						System.arraycopy( buffer[ ( z - zs ) / dz ], pos, strip, 0, stripLength );
					} catch ( Exception e )
					{
						Logger.info( "" + e.toString() );
						Logger.info( "------- s [#] : " + s );
						Logger.info( "stripLength [bytes] : " + strip.length );
						Logger.info( "pos [bytes] : " + pos );
						Logger.info( "pos + stripLength [bytes] : " + ( pos + stripLength ) );
						Logger.info( "z-zs : " + ( z - zs ) );
						Logger.info( "z-zs/dz : " + ( z - zs ) / dz );
						Logger.info( "buffer[z-zs].length : " + buffer[ z - zs ].length );
						Logger.info( "imWidth [bytes] : " + imByteWidth );
						Logger.info( "rows per strip [#] : " + rps );
						Logger.info( "ny [#] : " + ny );
						Logger.info( "(s - ss) * imByteWidth * rps [bytes] : " + ( ( s - ss ) * imByteWidth *
								rps ) );
						Logger.info( "unCompressedBuffer.length [bytes] : " + unCompressedBuffer.length );
					}

					// TODO: maybe implement a faster version without dynamic byte array allocation?
					strip = packBitsUncompressFast( strip, imByteWidth * rps );

					// put uncompressed strip into large array
					System.arraycopy( strip, 0, unCompressedBuffer, ( s - ss ) * imByteWidth * rps, imByteWidth * rps );

					pos += stripLength;
				}

				buffer[ ( z - zs ) / dz ] = unCompressedBuffer;
			}
			else if ( fi.compression == ZIP )
			{
				// init to hold all data present in the uncompressed strips
				byte[] unCompressedBuffer = new byte[ ( se - ss + 1 ) * rps * imByteWidth ];

				int pos = 0;

				for ( int s = ss; s <= se; s++ )
				{
					// TODO: multithreading here?
					int compressedStripLength = ( int ) fi.stripLengths[ s ];
					byte[] strip = new byte[ compressedStripLength ];

					// get strip from core data
					try
					{
						System.arraycopy( buffer[ ( z - zs ) / dz ], pos, strip, 0, compressedStripLength );
					} catch ( Exception e )
					{
						Logger.info( "" + e.toString() );
						Logger.info( "------- s [#] : " + s );
						Logger.info( "stripLength [bytes] : " + strip.length );
						Logger.info( "pos [bytes] : " + pos );
						Logger.info( "pos + stripLength [bytes] : " + ( pos + compressedStripLength ) );
						Logger.info( "z-zs : " + ( z - zs ) );
						Logger.info( "z-zs/dz : " + ( z - zs ) / dz );
						Logger.info( "buffer[z-zs].length : " + buffer[ z - zs ].length );
						Logger.info( "imWidth [bytes] : " + imByteWidth );
						Logger.info( "rows per strip [#] : " + rps );
						Logger.info( "ny [#] : " + ny );
						Logger.info( "(s - ss) * imByteWidth * rps [bytes] : " + ( ( s - ss ) * imByteWidth *
								rps ) );
						Logger.info( "unCompressedBuffer.length [bytes] : " + unCompressedBuffer.length );
					}

					/** TIFF Adobe ZIP support contributed by Jason Newton. */
					ByteArrayOutputStream imageBuffer = new ByteArrayOutputStream();
					byte[] tmpBuffer = new byte[ 1024 ];

					Inflater decompressor = new Inflater();

					decompressor.setInput( strip );
					try
					{
						while ( !decompressor.finished() )
						{
							int rlen = decompressor.inflate( tmpBuffer );
							imageBuffer.write( tmpBuffer, 0, rlen );
						}
					} catch ( DataFormatException e )
					{
						Logger.error( e.toString() );
					}
					decompressor.end();

					strip = imageBuffer.toByteArray();

					// put uncompressed strip into large array
					System.arraycopy(
							strip,
							0,
							unCompressedBuffer,
							( s - ss ) * imByteWidth * rps,
							imByteWidth * rps );

					pos += compressedStripLength;
				}

				buffer[ ( z - zs ) / dz ] = unCompressedBuffer;
			}
			else
			{

				Logger.error( "Tiff compression not implemented: fi.compression = " + fi.compression );
				return;

			}

			ys = ys % rps; // we might have to skip a few rows in the beginning because the strips can hold several rows

		}
		else // no strips
		{
			if ( fi.compression == ZIP )
			{
				/** TIFF Adobe ZIP support contributed by Jason Newton. */
				ByteArrayOutputStream imageBuffer = new ByteArrayOutputStream();
				byte[] tmpBuffer = new byte[ 1024 ];
				Inflater decompressor = new Inflater();

				decompressor.setInput( buffer[ ( z - zs ) / dz ] );
				try
				{
					while ( !decompressor.finished() )
					{
						int rlen = decompressor.inflate( tmpBuffer );
						imageBuffer.write( tmpBuffer, 0, rlen );
					}
				} catch ( DataFormatException e )
				{
					Logger.error( e.toString() );
				}
				decompressor.end();

				buffer[ ( z - zs ) / dz ] = imageBuffer.toByteArray();

				//setShortPixelsCropXY((short[]) stack.getPixels((z - zs)/dz + 1), ys, ny, xs, nx, imByteWidth, buffer[(z - zs)/dz]);
			}
			else if ( fi.compression == TiffCellLoader.LZW )
			{
				buffer[ ( z - zs ) / dz ] = lzwUncompress( buffer[ ( z - zs ) / dz ], imByteWidth * ny  );
				ys = 0; // buffer contains full y-range
			}
			else
			{
				ys = 0; // buffer contains full y-range
			}

			if ( Logger.isShowDebug() )
			{
				Logger.info( "z: " + z );
				Logger.info( "zs: " + zs );
				Logger.info( "dz: " + dz );
				Logger.info( "(z - zs)/dz: " + ( z - zs ) / dz );
				Logger.info( "buffer.length : " + buffer.length );
				Logger.info( "buffer[z-zs].length : " + buffer[ z - zs ].length );
				Logger.info( "imWidth [bytes] : " + imByteWidth );
				Logger.info( "ny [#] : " + ny );
			}
		}

		//
		// Copy (crop of) xy data from buffer into image stack
		//

		if ( fi.bytesPerPixel == 1 )
		{
			setBytePixelsCropXY( ( byte[] ) stack.getPixels( ( z - zs ) / dz + 1 ), ys, ny, xs, nx, imByteWidth, buffer[ ( z - zs ) / dz ] );
		}
		else if ( fi.bytesPerPixel == 2 )
		{
			setShortPixelsCropXY( ( short[] ) stack.getPixels( ( z - zs ) / dz + 1 ), ys, ny, xs, nx, imByteWidth, buffer[ ( z - zs ) / dz ] );
		} else
		{
			Logger.error( "Unsupported bit depth." );
			return;
		}
	}

	/**
	 * A growable array of bytes.
	 */
	class ByteVector
	{
		private byte[] data;
		private int size;

		public ByteVector()
		{
			data = new byte[ 10 ];
			size = 0;
		}

		public ByteVector( int initialSize )
		{
			data = new byte[ initialSize ];
			size = 0;
		}

		public ByteVector( byte[] byteBuffer )
		{
			data = byteBuffer;
			size = 0;
		}

		public void add( byte x )
		{
			if ( size >= data.length )
			{
				doubleCapacity();
				add( x );
			} else
				data[ size++ ] = x;
		}

		public int size()
		{
			return size;
		}

		public void add( byte[] array )
		{
			int length = array.length;
			while ( data.length - size < length )
				doubleCapacity();
			System.arraycopy( array, 0, data, size, length );
			size += length;
		}

		void doubleCapacity()
		{
			byte[] tmp = new byte[ data.length * 2 + 1 ];
			System.arraycopy( data, 0, tmp, 0, data.length );
			data = tmp;
		}

		public void clear()
		{
			size = 0;
		}

		public byte[] toByteArray()
		{
			byte[] bytes = new byte[ size ];
			System.arraycopy( data, 0, bytes, 0, size );
			return bytes;
		}

	}

	/**
	 * Based on the Bio-Formats PackbitsCodec written by Melissa Linkert.
	 */
	public byte[] packBitsUncompress( byte[] input, int expected )
	{
		if ( expected == 0 ) expected = Integer.MAX_VALUE;
		ByteVector output = new ByteVector( 1024 );
		int index = 0;
		while ( output.size() < expected && index < input.length )
		{
			byte n = input[ index++ ];
			if ( n >= 0 )
			{ // 0 <= n <= 127
				byte[] b = new byte[ n + 1 ];
				for ( int i = 0; i < n + 1; i++ )
					b[ i ] = input[ index++ ];
				output.add( b );
				b = null;
			} else if ( n != -128 )
			{ // -127 <= n <= -1
				int len = -n + 1;
				byte inp = input[ index++ ];
				for ( int i = 0; i < len; i++ ) output.add( inp );
			}
		}
		return output.toByteArray();
	}


	/**
	 * Based on the Bio-Formats PackbitsCodec written by Melissa Linkert.
	 */
	public byte[] packBitsUncompressFast( byte[] input, int expected )
	{
		if ( expected == 0 ) expected = Integer.MAX_VALUE;
		int inputIndex = 0;
		int outputIndex = 0;

		final byte[] output = new byte[ expected ];
		while ( inputIndex < expected && inputIndex < input.length )
		{
			byte n = input[ inputIndex++ ];
			if ( n >= 0 )
			{ // 0 <= n <= 127
				for ( int i = 0; i < n + 1; i++ )
					output[ outputIndex++ ] = input[ inputIndex++ ];
			} else if ( n != -128 )
			{ // -127 <= n <= -1
				int len = -n + 1;
				byte inp = input[ inputIndex++ ];
				for ( int i = 0; i < len; i++ ) output[ outputIndex++ ] = inp;
			}
		}
		return output;
	}

	public byte[] lzwUncompress( byte[] input, int byteCount )
	{
		long startTimeGlob = System.nanoTime();

		if ( input == null || input.length == 0 )
			return input;

		int bitsToRead = 9;
		int nextSymbol = 258;
		int code;
		int symbolLength, symbolLengthMax = 0;
		int oldCode = -1;
		//ByteVector out = new ByteVector(8192);
		byte[] out = new byte[ byteCount ];
		int iOut = 0, i;
		int k = 0;
		BitBuffer bb = new BitBuffer( input );

		byte[] byteBuffer1 = new byte[ 16 ];
		byte[] byteBuffer2 = new byte[ 16 ];

		while ( iOut < byteCount )
		{
			//startTime2 = System.nanoTime();
			code = bb.getBits( bitsToRead );
			//totalTime2 += (System.nanoTime() - startTime2);
			if ( code == EOI_CODE || code == -1 )
				break;
			if ( code == CLEAR_CODE )
			{
				//startTime4 = System.nanoTime();
				// initialize symbol jTableSpots
				for ( i = 0; i < 256; i++ )
					symbolTable[ i ][ 0 ] = ( byte ) i;
				nextSymbol = 258;
				bitsToRead = 9;
				code = bb.getBits( bitsToRead );
				if ( code == EOI_CODE || code == -1 )
					break;
				//out.add(symbolTable[code]);
				System.arraycopy( symbolTable[ code ], 0, out, iOut, symbolTable[ code ].length );
				iOut += symbolTable[ code ].length;
				oldCode = code;
				//totalTime4 += (System.nanoTime() - startTime4);

			} else
			{
				if ( code < nextSymbol )
				{
					//startTime6 = System.nanoTime();
					// code is in jTableSpots
					//startTime5 = System.nanoTime();
					//out.add(symbolTable[code]);
					symbolLength = symbolTable[ code ].length;
					System.arraycopy( symbolTable[ code ], 0, out, iOut, symbolLength );
					iOut += symbolLength;
					//totalTime5 += (System.nanoTime() - startTime5);
					// add string to jTableSpots

					//ByteVector symbol = new ByteVector(byteBuffer1);
					//symbol.add(symbolTable[oldCode]);
					//symbol.add(symbolTable[code][0]);
					int lengthOld = symbolTable[ oldCode ].length;

					//byte[] newSymbol = new byte[lengthOld+1];
					symbolTable[ nextSymbol ] = new byte[ lengthOld + 1 ];
					System.arraycopy( symbolTable[ oldCode ], 0, symbolTable[ nextSymbol ], 0, lengthOld );
					symbolTable[ nextSymbol ][ lengthOld ] = symbolTable[ code ][ 0 ];
					//symbolTable[nextSymbol] = newSymbol;

					oldCode = code;
					nextSymbol++;
					//totalTime6 += (System.nanoTime() - startTime6);

				}
				else
				{
					//startTime3 = System.nanoTime();
					// out of jTableSpots
					ByteVector symbol = new ByteVector( byteBuffer2 );
					symbol.add( symbolTable[ oldCode ] );
					symbol.add( symbolTable[ oldCode ][ 0 ] );
					byte[] outString = symbol.toByteArray();
					//out.add(outString);
					System.arraycopy( outString, 0, out, iOut, outString.length );
					iOut += outString.length;
					symbolTable[ nextSymbol ] = outString; //**
					oldCode = code;
					nextSymbol++;
					//totalTime3 += (System.nanoTime() - startTime3);

				}
				if ( nextSymbol == 511 )
				{
					bitsToRead = 10;
				}
				if ( nextSymbol == 1023 )
				{
					bitsToRead = 11;
				}
				if ( nextSymbol == 2047 )
				{
					bitsToRead = 12;
				}
				if ( nextSymbol == 4095 )
				{
					bitsToRead = 13;
				}
				if ( nextSymbol == 8191 )
				{
					bitsToRead = 14;
				}
				if ( nextSymbol == 16383 )
				{
					Logger.error( "Symbol table of LZW uncompression became too large." +
							"\nThe next symbol would have been: " + nextSymbol +
							"\nPlease contact tischitischer@gmail.com" );
					return null;
				}
				;
			}

		}

		return out;
	}

	public void setShortPixelsCropXY( short[] pixels, int ys, int ny, int xs, int nx, int imByteWidth, byte[] buffer )
	{
		int ip = 0;
		int bs, be;
		if ( fi.bytesPerPixel != 2 )
		{
			Logger.error( "Unsupported bit depth: " + fi.bytesPerPixel * 8 );
		}

		for ( int y = ys; y < ys + ny; y++ )
		{
			bs = y * imByteWidth + xs * fi.bytesPerPixel;
			be = bs + nx * fi.bytesPerPixel;

			if ( fi.intelByteOrder )
			{
				if ( fi.fileType == GRAY16_SIGNED )
					for ( int j = bs; j < be; j += 2 )
						pixels[ ip++ ] = ( short ) ( ( ( ( buffer[ j + 1 ] & 0xff ) << 8 ) | ( buffer[ j ] & 0xff ) ) + 32768 );
				else
					for ( int j = bs; j < be; j += 2 )
						pixels[ ip++ ] = ( short ) ( ( ( buffer[ j + 1 ] & 0xff ) << 8 ) | ( buffer[ j ] & 0xff ) );
			} else
			{
				if ( fi.fileType == GRAY16_SIGNED )
					for ( int j = bs; j < be; j += 2 )
						pixels[ ip++ ] = ( short ) ( ( ( ( buffer[ j ] & 0xff ) << 8 ) | ( buffer[ j + 1 ] & 0xff ) ) + 32768 );
				else
					for ( int j = bs; j < be; j += 2 )
						pixels[ ip++ ] = ( short ) ( ( ( buffer[ j ] & 0xff ) << 8 ) | ( buffer[ j + 1 ] & 0xff ) );
			}
		}
	}

	public void setBytePixelsCropXY( byte[] pixels, int ys, int ny, int xs, int nx, int imByteWidth, byte[] buffer )
	{
		int ip = 0;
		int bs, be;

		for ( int y = ys; y < ys + ny; y++ )
		{
			bs = y * imByteWidth + xs * fi.bytesPerPixel;
			be = bs + nx * fi.bytesPerPixel;
			for ( int j = bs; j < be; j += 1 )
			{
				pixels[ ip++ ] = buffer[ j ];
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

	private byte[] readRowsFromTiff( BDP2FileInfo fi, RandomAccessFile in, int ys, int ye )
	{
		boolean hasStrips = false;
		int readLength;
		long readStart;
		byte[] buffer;

		if ( fi.stripOffsets != null && fi.stripOffsets.length > 1 )
			hasStrips = true;

		if ( hasStrips )
		{
			// convert rows to strips
			int rps = fi.rowsPerStrip;
			int ss = ( int ) ( 1.0 * ys / rps );
			int se = ( int ) ( 1.0 * ye / rps );
			readStart = fi.stripOffsets[ ss ];

			readLength = 0;
			if ( se >= fi.stripLengths.length )
				Logger.warn( "Strip is out of bounds" );

			for ( int s = ss; s <= se; s++ )
				readLength += fi.stripLengths[ s ];
		}
		else // none or one strip
		{
			if ( fi.compression == ZIP || fi.compression == TiffCellLoader.LZW || fi.compression == PACK_BITS )
			{
				// core all data
				readStart = fi.offset;
				readLength = ( int ) fi.stripLengths[ 0 ];
			}
			else
			{
				// core subset
				// convert rows to bytes
				readStart = fi.offset + ys * fi.width * fi.bytesPerPixel;
				readLength = ( ( ye - ys ) + 1 ) * fi.width * fi.bytesPerPixel; // ye is -1 sometimes why?
			}
		}

		if ( readLength <= 0 )
		{
			Logger.warn( "file type: Tiff" );
			Logger.warn( "hasStrips: " + hasStrips );
			Logger.warn( "core from [bytes]: " + readStart );
			Logger.warn( "core to [bytes]: " + ( readStart + readLength - 1 ) );
			Logger.warn( "ys: " + ys );
			Logger.warn( "ye: " + ye );
			Logger.warn( "fileInfo.compression: " + fi.compression );
			Logger.warn( "fileInfo.height: " + fi.height );
			Logger.error( "Error during file reading. See log window for more information" );
			return ( null );
		}

		buffer = new byte[ readLength ];

		try
		{
			if ( readStart + readLength - 1 <= in.length() )
			{
				in.seek( readStart ); // TODO: is this really slow??
				in.readFully( buffer );
			}
			else
			{
				Logger.warn( "The requested data exceeds the file length; no data was core." );
				Logger.warn( "file type: Tiff" );
				Logger.warn( "hasStrips: " + hasStrips );
				Logger.warn( "file length [bytes]: " + in.length() );
				Logger.warn( "attempt to core until [bytes]: " + ( readStart + readLength - 1 ) );
				Logger.warn( "ys: " + ys );
				Logger.warn( "ye: " + ye );
				Logger.warn( "fileInfo.compression: " + fi.compression );
				Logger.warn( "fileInfo.height: " + fi.height );
			}
		} catch ( Exception e )
		{
			Logger.warn( e.toString() );
		}

		return buffer;
	}
}
