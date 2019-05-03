package de.embl.cba.bdp2.saving;

import de.embl.cba.bdp2.progress.ProgressListener;

public abstract class AbstractImgSaver
{
	protected  ProgressListener progressListener;
	public static int TIME_OUT_SECONDS = 10;

	public void setProgressListener( ProgressListener l )
	{
		progressListener = l;
	}

	public abstract void startSave();

	public abstract void stopSave();

}
