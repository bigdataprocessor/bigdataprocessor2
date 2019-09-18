package de.embl.cba.bdp2.progress;

import java.util.concurrent.atomic.AtomicInteger;

public class LoggingProgressListener implements ProgressListener
{
	private long current;
	private long total;
	private long startTimeMillis = -1;
	private final String msg;

	public LoggingProgressListener( String msg )
	{
		this.msg = msg;
		reset();
	}

	@Override
	public void progress( long current, long total )
	{
		if ( startTimeMillis == -1 )
			startTimeMillis = System.currentTimeMillis();

		this.current = current;
		this.total = total;

		new Thread( () ->
			ProgressHelpers.logProgress(
				total, new AtomicInteger( (int) current ), startTimeMillis, msg )
		).start();
	}

	public boolean isFinished()
	{
		return current >= total;
	}

	public long getCurrent()
	{
		return current;
	}
	
	public long getTotal()
	{
		return total;
	}

	public void reset()
	{
		current = -1;
		total = 0;
	}
}
