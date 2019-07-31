package de.embl.cba.bdp2.tracking;

import de.embl.cba.bdp2.progress.ProgressListener;

public abstract class AbstractObjectTracker {

    protected ProgressListener progressListener;
    public void setProgressListener( ProgressListener l )
    {
        progressListener = l;
    }
    public abstract Track computeTrack();
    public abstract void stopTrack();

}
