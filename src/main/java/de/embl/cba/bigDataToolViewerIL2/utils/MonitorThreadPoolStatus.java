package de.embl.cba.bigDataToolViewerIL2.utils;

import de.embl.cba.bigDataToolViewerIL2.logging.IJLazySwingLogger;
import de.embl.cba.bigDataToolViewerIL2.logging.Logger;

import java.util.List;
import java.util.concurrent.Future;

/**
 * Created by tischi on 11/04/17.
 */
public class MonitorThreadPoolStatus {

    private static Logger logger = new IJLazySwingLogger();

    public static void showProgressAndWaitUntilDone(List<Future> futures,
                                                    String message,
                                                    int updateFrequencyMilliseconds) {

        long start = System.currentTimeMillis();

        int i = 0;
        while( i != futures.size() )
        {
            i = 0;
            for ( Future f : futures )
            {
                if (f.isDone() ) i++;
            }

            logger.progress( message, null, start, i, futures.size() );

            try {
                Thread.sleep(updateFrequencyMilliseconds);
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }

        }

    }

}
