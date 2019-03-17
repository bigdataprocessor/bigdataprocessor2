package de.embl.cba.bdp2.utils;

import de.embl.cba.bdp2.ui.BigDataProcessor;
import de.embl.cba.bdp2.logging.IJLazySwingLogger;
import de.embl.cba.bdp2.logging.Logger;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Created by tischi on 11/04/17.
 */
public class MonitorThreadPoolStatus {

    private static Logger logger = new IJLazySwingLogger();

    public static void showProgressAndWaitUntilDone(List<Future> futures,Integer saveId,
                                                    String message,
                                                    int updateFrequencyMilliseconds) {
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        long start = System.currentTimeMillis();
        System.out.println("Monitor save id "+saveId);
        int i = 0;
        while( i != futures.size() )
        {
            i = 0;
            for ( Future f : futures ) {
                if (f.isDone() ) i++;
            }
            logger.progress(message, null, start, i, futures.size());
            BigDataProcessor.progressTracker.put(saveId,i*100/futures.size()); //Updating UI progress bar.
            try {
                Thread.sleep(updateFrequencyMilliseconds);
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }

        }

    }

}
