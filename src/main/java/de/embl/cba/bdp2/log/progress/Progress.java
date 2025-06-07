/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2025 EMBL
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package de.embl.cba.bdp2.log.progress;

import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.scijava.Services;
import de.embl.cba.bdp2.utils.Utils;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/*
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
                String msg = "There was an error in one of the threads.\n" +
                        "Please see the Console for more details.\n" +
                        "In case of an out-of-memory error, please increase the RAM and/or " +
                        "reduce the number of threads.";

                if ( Services.getUiService() != null && Services.getUiService().isHeadless()) {
                    Logger.info( msg + "\nError in headless mode: exiting..." );
                    try {
                        Services.getContext().dispose();
                    }
                    finally {
                        System.exit(1);
                    }
                }
                else
                {
                    Logger.error( msg );
                }
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
