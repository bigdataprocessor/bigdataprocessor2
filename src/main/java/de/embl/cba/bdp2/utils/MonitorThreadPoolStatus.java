package de.embl.cba.bdp2.utils;

import de.embl.cba.bdp2.progress.ProgressListener;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Created by tischi on 11/04/17.
 */
public class MonitorThreadPoolStatus {

    public static void showProgressAndWaitUntilDone( List< Future > futures,
                                                     int updateFrequencyMilliseconds,
                                                     ProgressListener progressListener ) {
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        int numFinishedFutures = 0;
        while( numFinishedFutures != futures.size() )
        {
            numFinishedFutures = 0;
            for ( Future f : futures )
                if (f.isDone() ) numFinishedFutures++;

            if ( progressListener != null )
                progressListener.progress( numFinishedFutures, futures.size() );

            try {
                Thread.sleep(updateFrequencyMilliseconds);
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }

        }

    }

}
