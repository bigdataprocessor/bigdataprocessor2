package de.embl.cba.bdp2.performance;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.BigDataProcessor2UI;

import java.util.ArrayList;
import java.util.List;

public class PerformanceMonitor
{
	public static final int MEGA = 10000000;
	private List< Double > readPerformances;
	private List< Double > copyPerformances;

	public PerformanceMonitor()
	{
		readPerformances = new ArrayList<>( );
		copyPerformances = new ArrayList<>( );
	}

	public synchronized void addReadPerformance( int numBytes, long timeMillis )
	{
		final double mbps = toMBPS( numBytes, timeMillis );
		readPerformances.add( mbps );
		BigDataProcessor2UI.setReadPerformanceInformation( mbps, getAverageReadPerformance() );
	}

	/**
	 *
	 * @param numBytes
	 * @param timeMillis
	 * @return mega bits per second
	 */
	private double toMBPS( int numBytes, long timeMillis )
	{
		return 1.0 * numBytes * 8 / MEGA / ( timeMillis / 1000.0 );
	}

	public double getAverageReadPerformance()
	{
		return readPerformances.stream().mapToDouble( x -> x ).average().getAsDouble();
	}

	public synchronized void addCopyPerformance( int numBytes, long timeMillis )
	{
		copyPerformances.add( toMBPS( numBytes, timeMillis ) );
	}

	public double getAverageCopyPerformance()
	{
		return readPerformances.stream().mapToDouble( x -> x ).average().getAsDouble();
	}
}
