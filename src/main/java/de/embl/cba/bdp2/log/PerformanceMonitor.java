/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2025 EMBL
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

import de.embl.cba.bdp2.BigDataProcessor2UI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PerformanceMonitor
{
	public static final int MEGA = 1000000;
	private List< Double > readPerformances;
	private List< Double > copyPerformances;

	public PerformanceMonitor()
	{
		readPerformances = Collections.synchronizedList(new ArrayList<>( ));
		copyPerformances = Collections.synchronizedList(new ArrayList<>( ));
	}

	public synchronized void addReadPerformance( Object storageArray, double timeMillis )
	{
		double speed;

		if ( storageArray instanceof byte[] )
			speed = toMBytePerSecond( ( ( byte[] ) storageArray ).length, timeMillis );
		else if ( storageArray instanceof short[] )
			speed = toMBytePerSecond( 2 * ( ( short[] ) storageArray ).length, timeMillis );
		else if ( storageArray instanceof float[] )
			speed = toMBytePerSecond( 4 * ( ( float[] ) storageArray ).length, timeMillis );
		else
			throw new RuntimeException( "Unsupported storage array type " + storageArray.getClass() );


		synchronized ( readPerformances )
		{
			readPerformances.add( speed );
		}

		BigDataProcessor2UI.setReadPerformanceInformation( speed, getMedianReadPerformance() );
	}

	private double toMBitPerSecond( long bytes, double millis )
	{
		return toMBit( bytes ) / toSeconds( millis );
	}

	private double toMBytePerSecond( long bytes, double millis )
	{
		return toMByte( bytes ) / toSeconds( millis );
	}

	private double toSeconds( double timeMillis )
	{
		return timeMillis / 1000.0;
	}

	private double toMBit( long bytes )
	{
		return 1.0 * bytes * 8 / MEGA;
	}

	private double toMByte( long bytes )
	{
		return 1.0 * bytes / MEGA;
	}

	public double getMedianReadPerformance()
	{
		if ( readPerformances.size() == 0 ) return 0;

		synchronized ( readPerformances )
		{
			final double median = readPerformances.stream().mapToDouble( x -> x ).sorted().skip(readPerformances.size()/2).findFirst().getAsDouble();
			return median;
		}
	}

	public void addCopyPerformance( int numBytes, long timeMillis )
	{
		copyPerformances.add( toMBitPerSecond( numBytes, timeMillis ) );
	}

	public double getAverageCopyPerformance()
	{
		return readPerformances.stream().mapToDouble( x -> x ).average().getAsDouble();
	}
}
