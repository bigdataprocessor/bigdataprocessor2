package de.embl.cba.bdp2.open.fileseries;

import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.utils.DimensionOrder;
import net.imglib2.cache.img.SingleCellArrayImg;

import java.io.File;
import java.io.RandomAccessFile;

public class TiffPlaneCellLoader implements Runnable
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
	private final int z;
	private final int bytesPerRow;

	public TiffPlaneCellLoader( SingleCellArrayImg cell, int z, String directory, BDP2FileInfo fi )
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
		int yMaxRequested = (int) cell.max( DimensionOrder.Y );
		int minColRequested = (int) cell.min( DimensionOrder.X );
		int colsRequested = (int) cell.dimension( DimensionOrder.X );
		int rowsRequested = (int) cell.dimension( DimensionOrder.Y );

		TiffRowsReader rowsReader = new TiffRowsReader();

		try
		{
			File file = new File( new File( directory, fi.directory ).getAbsolutePath(), fi.fileName );
			RandomAccessFile inputStream = new RandomAccessFile( file, "r" );
			bytes = rowsReader.read( fi, inputStream, minRowRequested, yMaxRequested );
			inputStream.close();
		}
		catch ( Exception e )
		{
			throw new RuntimeException( e );
		}

		// this may differ from ny, because of the presence of (compressed) strips
		int minRowRead = rowsReader.getRowMin();
		int rowsRead = rowsReader.getRowMax() - minRowRead;

		if ( rowsReader.hasStrips() )
		{
			final int rps = fi.rowsPerStrip;
			final int stripStart = rowsReader.getStripMin();
			final int stripEnd = rowsReader.getStripMax();

			if ( ( fi.compression == TiffDecompressor.NONE ) || ( fi.compression == 0 ) )
			{
				// do nothing
			}
			else if ( fi.compression == TiffDecompressor.LZW || fi.compression == TiffDecompressor.PACK_BITS || fi.compression == TiffDecompressor.ZIP )
			{
				bytes = TiffDecompressor.decompressStrips( bytes, rps, stripStart, stripEnd, bytesPerRow, fi.stripLengths, fi.compression );
			}
			else
			{
				throw new RuntimeException( "Tiff compression not implemented: " + fi.compression );
			}
		}
		else // no strips
		{
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
				// do nothing
			}

			if ( Logger.getLevel().equals( Logger.Level.Debug ) )
			{
				Logger.debug( "z: " + z );
				Logger.debug( "buffer.length : " + bytes.length );
				Logger.debug( "imWidth [bytes] : " + bytesPerRow );
				Logger.debug( "rows read [#] : " + rowsRead );
			}
		}

		// Read the relevant pixels from the byte buffer into the cell array.
		// Due to reading from strips, the buffer may contain
		// both more rows and columns than the cell array and we need to crop
		// the read data.

		final int cellOffset = ( int ) ( ( z - cell.min( DimensionOrder.Z ) ) * cell.dimension( 0 ) * cell.dimension( 1 ));

		final int rowOffset = minRowRequested - minRowRead;

		if ( fi.bytesPerPixel == 1 )
		{
			try
			{
				setBytePixelsCropXY(
						( byte[] ) cell.getStorageArray(),
						cellOffset,
						rowOffset,
						rowsRequested,
						minColRequested,
						colsRequested,
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
						rowOffset,
						rowsRequested,
						minColRequested,
						colsRequested,
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
