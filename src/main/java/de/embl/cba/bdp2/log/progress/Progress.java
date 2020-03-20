package de.embl.cba.bdp2.log.progress;

import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.utils.Utils;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by tischi on 11/04/17.
 */
public class Progress
{

    public static void informProgressListeners( List< Future > futures,
                                                int updateFrequencyMilliseconds,
                                                List< ProgressListener > progressListeners ) {

        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        int numFinishedFutures = 0;
        int previousFinishedFutures = 0;

        boolean error = false;

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
                        error = true;
                        break;
                    }
                    catch ( ExecutionException e )
                    {
                        e.printStackTrace();
                        numFinishedFutures = futures.size();
                        error = true;
                        break;
                    }
                    numFinishedFutures++;
                }
            }

            if ( error )
            {
                Logger.error( "There was an error in one of the threads.\n" +
                        "Please see the Console for more details.\n" +
                        "In case of an out-of-memory error, please increase the RAM and/or " +
                        "reduce the number of threads.");
            }


            if ( numFinishedFutures != previousFinishedFutures )
                if ( progressListeners != null )
                    for ( ProgressListener listener : progressListeners )
                        listener.progress( numFinishedFutures, futures.size() );

            previousFinishedFutures = numFinishedFutures;

            try {
                Thread.sleep( updateFrequencyMilliseconds );
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

    public static void waitUntilDone(
            LoggingProgressListener progress,
            int progressUpdateMillis )
    {
        while ( ! progress.isFinished() )
            Utils.sleepMillis( progressUpdateMillis );
        Logger.log( "Done: " + progress.getCurrent() + " / " + progress.getTotal() );
    }
}
