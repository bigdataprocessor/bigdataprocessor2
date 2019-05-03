package de.embl.cba.bdp2.progress;

public interface ProgressListener
{
	void progress( long current, long total );
}
