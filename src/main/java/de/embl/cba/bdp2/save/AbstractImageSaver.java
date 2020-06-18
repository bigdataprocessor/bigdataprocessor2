package de.embl.cba.bdp2.save;

import de.embl.cba.bdp2.log.progress.ProgressListener;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractImageSaver implements ImageSaver
{
	protected List< ProgressListener > progressListeners = new ArrayList<>(  );
	public static int TIME_OUT_SECONDS = 10;

	public void addProgressListener( ProgressListener progressListener )
	{
		progressListeners.add( progressListener );
	}

}
