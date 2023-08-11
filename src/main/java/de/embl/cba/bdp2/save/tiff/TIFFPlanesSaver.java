/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2023 EMBL
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
package de.embl.cba.bdp2.save.tiff;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.log.progress.Progress;
import de.embl.cba.bdp2.open.fileseries.FileInfos;
import de.embl.cba.bdp2.save.AbstractImageSaver;
import de.embl.cba.bdp2.save.SavingSettings;
import de.embl.cba.bdp2.utils.DimensionOrder;
import de.embl.cba.bdp2.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public class TIFFPlanesSaver extends AbstractImageSaver
{
    private final Image image;
    private SavingSettings settings;
    private ExecutorService es;
    private AtomicBoolean stop;

    public TIFFPlanesSaver( Image image, SavingSettings settings, ExecutorService es ) {
        this.image = image;
        this.settings = settings;
        this.es = es;
        this.stop = new AtomicBoolean(false);
    }

    public void startSave() {
        List<Future> futures = new ArrayList<>();
        final long numChannels = image.getRai().dimension(DimensionOrder.C);
        final long numFrames = image.getRai().dimension(DimensionOrder.T);
        final long numPlanes = image.getRai().dimension(DimensionOrder.Z);

        for (int c = 0; c < numChannels; c++) {
            for (int t = 0; t < numFrames; t++) {
                for (int z = 0; z < numPlanes; z++) {
                    futures.add( es.submit(
                            new TIFFPlaneSaver( c, t, z, image, settings, stop )
                    ));
                }
            }
        }

        // Monitor the progress
        Thread thread = new Thread(() -> Progress.informProgressListeners(
                futures,
                FileInfos.PROGRESS_UPDATE_MILLISECONDS,
				progressListeners ));

        thread.start();
    }

    @Override
    public void stopSave() {
        stop.set(true);
        Utils.shutdownThreadPack(es,TIME_OUT_SECONDS);
    }
}
