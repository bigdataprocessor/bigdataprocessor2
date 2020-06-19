package de.embl.cba.bdp2.drift.devel;

import de.embl.cba.bdp2.log.progress.ProgressListener;

public abstract class AbstractObjectTracker {

    protected ProgressListener progressListener;
    public void setProgressListener( ProgressListener l )
    {
        progressListener = l;
    }
    public abstract OldTrack computeTrack();
    public abstract void stopTrack();

}
