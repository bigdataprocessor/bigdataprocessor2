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
package de.embl.cba.bdp2.log;

import net.imglib2.cache.img.SingleCellArrayImg;
import net.imglib2.type.NativeType;

import java.util.Arrays;

public class CellLoaderLogger< T extends NativeType< T > >
{
	private final SingleCellArrayImg< T, ? > cell;
	private long startNanos;
	private long durationNanos;

	public CellLoaderLogger( SingleCellArrayImg< T, ? > cell )
	{
		this.cell = cell;
	}

	public void start()
	{
		startNanos = System.nanoTime();
	}

	public void stop()
	{
		durationNanos = System.nanoTime() - startNanos;
	}

	public long getDurationNanos()
	{
		return durationNanos;
	}

	public String getBenchmarkLog()
	{
		long[] min = new long[ cell.numDimensions() ];
		long[] max = new long[ cell.numDimensions() ];
		cell.min( min );
		cell.max( max );

		StringBuilder builder = new StringBuilder( "Read " );
		builder.append( Arrays.toString( min ) );
		builder.append( " - "  );
		builder.append( Arrays.toString( max ) );
		builder.append( " in "  );
		builder.append( String.format("%.2f", durationNanos / Math.pow( 10, 6 ) ) );
		builder.append( " ms "  );
		builder.append( " ( "  );
		builder.append( String.format("%.2f", 1.0 * numBytes() / durationNanos * Math.pow( 10, 3 ) ) );
		builder.append( " MB/s )"  );

		return builder.toString();
	}

	private int numBytes()
	{
		Object array = cell.getStorageArray();
		if ( array instanceof byte[] ) return ( ( byte[] ) array ).length * 1;
		else if ( array instanceof short[] ) return ( ( short[] ) array ).length * 2;
		else if ( array instanceof int[] ) return ( ( int[] ) array ).length * 4;
		else if ( array instanceof long[] ) return ( ( long[] ) array ).length * 8;
		else if ( array instanceof float[] ) return ( ( float[] ) array ).length * 4;
		else if ( array instanceof double[] ) return ( ( double[] ) array ).length * 8;
		else throw new RuntimeException( "Unsupported array type: " + array.getClass() );
	}
}
