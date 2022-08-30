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
