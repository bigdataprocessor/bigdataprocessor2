package de.embl.cba.bdp2.service;

import de.embl.cba.bdp2.performance.PerformanceMonitor;

public abstract class PerformanceService
{
	private static PerformanceMonitor performanceMonitor;

	public static PerformanceMonitor getPerformanceMonitor()
	{
		if ( performanceMonitor == null )
			performanceMonitor = new PerformanceMonitor();

		return performanceMonitor;
	}
}
