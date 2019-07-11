package de.embl.cba.bdp2.progress;

import de.embl.cba.bdp2.logging.Logger;
import de.embl.cba.bdp2.utils.Utils;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by tischi on 11/04/17.
 */
public class Progress
{

    public static void informProgressListener( List< Future > futures,
                                               int updateFrequencyMilliseconds,
                                               ProgressListener progressListener ) {

        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        int numFinishedFutures = 0;
        while( numFinishedFutures != futures.size() )
        {
            numFinishedFutures = 0;
            for ( Future f : futures )
            {
                if ( f.isDone() )
                {
                    try
                    {
                        f.get();
                    } catch ( InterruptedException e )
                    {
                        e.printStackTrace();
                        numFinishedFutures = futures.size();
                        break;
                    }
                    catch ( ExecutionException e )
                    {
                        e.printStackTrace();
                        numFinishedFutures = futures.size();
                        break;
                    }
                    numFinishedFutures++;
                }
            }

            if ( progressListener != null )
                progressListener.progress( numFinishedFutures, futures.size() );

            try {
                Thread.sleep(updateFrequencyMilliseconds);
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void waitUntilDoneAndLogProgress( DefaultProgressListener progress, int progressUpdateMillis, String text )
    {

        while ( ! progress.isFinished() )
        {
            Logger.log( text + ": " + progress.getCurrent() + " / " + progress.getTotal() );
            Utils.sleepMillis( progressUpdateMillis );
        }
        Logger.log( text + ": " + progress.getCurrent() + " / " + progress.getTotal() );
    }

    public static void waitUntilDone( DefaultProgressListener progress, int progressUpdateMillis )
    {
        while ( ! progress.isFinished() )
            Utils.sleepMillis( progressUpdateMillis );
        Logger.log( "Done: " + progress.getCurrent() + " / " + progress.getTotal() );
    }
}
