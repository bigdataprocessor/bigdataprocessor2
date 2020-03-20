package de.embl.cba.bdp2.log.progress;

public class DefaultProgressListener implements ProgressListener
{
	private long current;
	private long total;

	public DefaultProgressListener()
	{
		reset();
	}

	@Override
	public void progress( long current, long total )
	{
		this.current = current;
		this.total = total;
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
