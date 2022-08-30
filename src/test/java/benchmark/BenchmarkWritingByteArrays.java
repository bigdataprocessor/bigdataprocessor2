package benchmark;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BenchmarkWritingByteArrays
{
	public static final int GIGA = ( 1000 * 1000 * 1000 );

	public static void main( String[] args ) throws IOException
	{
		final File dir = new File( "/Users/tischer/Desktop/delete" );

		final byte[][] data = new byte[1000][1000*1000];

		for ( int rep = 0; rep < 10; rep++ )
		{
			// write to one file, appending
			Path path = Paths.get( dir.toString(), "data.bin" );
			long start = System.currentTimeMillis();
			FileOutputStream stream = new FileOutputStream( path.toFile() );
			for ( int i = 0; i < data.length; i++ )
				stream.write( data[ i ] );
			stream.close();
			long duration = System.currentTimeMillis() - start;
			log( data, duration, "One file: " );

			// write to multiple files
			start = System.currentTimeMillis();
			for ( int i = 0; i < data.length; i++ )
			{
				path = Paths.get( dir.toString(), "data" + i + ".bin" );
				Files.write( path, data[ i ] );
			}
			duration = System.currentTimeMillis() - start;
			log( data, duration, data.length + " files: " );

			deleteAllFiles( dir, data );
		}
	}

	private static void deleteAllFiles( File dir, byte[][] data ) throws IOException
	{
		Path path;
		// clean up
		Files.delete( Paths.get( dir.toString(), "data.bin" ) );
		for ( int i = 0; i < data.length; i++ )
		{
			path = Paths.get( dir.toString(), "data" + i + ".bin" );
			Files.delete( path );
		}
	}

	private static void log( byte[][] data, long duration, String s )
	{
		System.out.println( s + duration + " ms; " + getGB( data ) + " Bytes; "  + getGBs( data, duration ) + " GB/s" );
	}

	private static String getGBs( byte[][] data, long duration )
	{
		final double gbPerSecond = getGB( data ) / duration * 1000 / GIGA;
		return String.format("%.2f", gbPerSecond);
	}

	private static double getGB( byte[][] data )
	{
		return 1.0 * data.length * data[ 0 ].length;
	}
}
