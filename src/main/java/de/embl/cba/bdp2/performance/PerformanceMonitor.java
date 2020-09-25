package de.embl.cba.bdp2.performance;

import de.embl.cba.bdp2.ui.BigDataProcessor2UI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PerformanceMonitor
{
	public static final int MEGA = 10000000;
	private List< Double > readPerformances;
	private List< Double > copyPerformances;

	public PerformanceMonitor()
	{
		readPerformances = Collections.synchronizedList(new ArrayList<>( ));
		copyPerformances = Collections.synchronizedList(new ArrayList<>( ));
	}

	public synchronized void addReadPerformance( long numBytes, long timeMillis )
	{
		final double mbps = toMBPS( numBytes, timeMillis );
		synchronized ( readPerformances )
		{
			readPerformances.add( mbps );
		}
		BigDataProcessor2UI.setReadPerformanceInformation( mbps, getMedianReadPerformance() );
	}

	/**
	 *
	 * @param numBytes
	 * @param timeMillis
	 * @return mega bits per second
	 */
	private double toMBPS( long numBytes, long timeMillis )
	{
		return 1.0 * numBytes * 8 / MEGA / ( timeMillis / 1000.0 );
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
		copyPerformances.add( toMBPS( numBytes, timeMillis ) );
	}

	public double getAverageCopyPerformance()
	{
		return readPerformances.stream().mapToDouble( x -> x ).average().getAsDouble();
	}
}
