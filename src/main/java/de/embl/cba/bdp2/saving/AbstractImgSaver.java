package de.embl.cba.bdp2.saving;

import de.embl.cba.bdp2.progress.ProgressListener;

public class AbstractImgSaver
{
	ProgressListener progressListener;

	public void setProgressListener( ProgressListener l )
	{
		progressListener = l;
	}

}
