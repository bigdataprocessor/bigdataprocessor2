package de.embl.cba.bdp2.log.progress;

import java.util.concurrent.atomic.AtomicInteger;

public class LoggingProgressListener implements ProgressListener
{
	private long current;
	private long total;
	private long startTimeMillis;
	private final String msg;

	public LoggingProgressListener( String msg )
	{
		this.msg = msg;
		this.startTimeMillis = System.currentTimeMillis();
		reset();
	}

	@Override
	public void progress( long current, long total )
	{
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
