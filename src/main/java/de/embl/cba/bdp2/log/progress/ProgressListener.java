package de.embl.cba.bdp2.log.progress;

public interface ProgressListener
{
	void progress( long current, long total );
}
