package de.embl.cba.bdp2.log;

import de.embl.cba.bdp2.BigDataProcessor2UserInterface;

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
		// TODO: handle this differently, maybe isHeadless()
		if ( false ) // FileSeriesCachedCellImageCreator.isReadingVolumes )
		{
			Logger.debug( "Read " + toMB( numBytes ) + " MB in " + toSeconds( timeMillis ) + " seconds; Speed [MB/s] = " + mbps );
		}

		BigDataProcessor2UserInterface.setReadPerformanceInformation( mbps, getMedianReadPerformance() );
	}

	/**
	 *
	 * @param bytes
	 * @param millis
	 * @return mega bits per second
	 */
	private double toMBPS( long bytes, long millis )
	{
		return toMB( bytes ) / toSeconds( millis );
	}

	private double toSeconds( long timeMillis )
	{
		return timeMillis / 1000.0;
	}

	private double toMB( long bytes )
	{
		return 1.0 * bytes * 8 / MEGA;
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
