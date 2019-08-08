package de.embl.cba.bdp2.progress;

import de.embl.cba.bdp2.logging.Logger;
import ij.IJ;

import java.util.concurrent.atomic.AtomicInteger;

public class ProgressHelpers
{
	public static void logProgress( long total, AtomicInteger counter, final long startTimeMillis )
	{
		logProgress( total, counter, startTimeMillis, "" );
	}

	public static void logProgress( long total, AtomicInteger counter, final long startTimeMillis, String msg )
	{
		float secondsSpent = (1.0F * System.currentTimeMillis() - startTimeMillis ) / (1000.0F);
		float secondsPerTask = secondsSpent / counter.get();
		float secondsLeft = (total - counter.get()) * secondsPerTask;

		String unit = "s";
		float divisor = 60;


		if ( secondsSpent > 3 * 60 )
		{
			unit = "min";
			divisor = 60;
		}

		Logger.progress( msg,
				"" + counter.get() + "/" + total
						+ "; time ( spent, left, task ) [ " + unit + " ]: "
						+ ( int ) ( secondsSpent / divisor )
						+ ", " + ( int ) ( secondsLeft / divisor )
						+ ", " + secondsPerTask / divisor
						+ "; memory: "
						+ IJ.freeMemory() );


	}
}
