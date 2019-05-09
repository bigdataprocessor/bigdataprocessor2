package de.embl.cba.bdp2.saving;

import de.embl.cba.bdp2.progress.ProgressListener;

public abstract class AbstractImgSaver implements ImgSaver
{
	protected  ProgressListener progressListener;
	public static int TIME_OUT_SECONDS = 10;

	public void setProgressListener( ProgressListener l )
	{
		progressListener = l;
	}

}
