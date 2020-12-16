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
