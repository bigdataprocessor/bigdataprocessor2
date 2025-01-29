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
package de.embl.cba.bdp2.save.imaris;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.open.fileseries.FileInfos;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.save.AbstractImageSaver;
import de.embl.cba.bdp2.save.SavingSettings;
import de.embl.cba.bdp2.utils.DimensionOrder;
import de.embl.cba.bdp2.log.progress.Progress;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.imaris.ImarisDataSet;
import de.embl.cba.imaris.ImarisUtils;
import de.embl.cba.imaris.ImarisWriter;
import ij.ImagePlus;
import net.imagej.patcher.LegacyInjector;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ImarisImageSaver< R extends RealType< R > & NativeType< R > > extends AbstractImageSaver
{
    static { LegacyInjector.preinit(); }

    private final Image< R > image;
    private SavingSettings savingSettings;
    private ExecutorService es;
    private AtomicBoolean stop;

    public ImarisImageSaver( Image< R > image, SavingSettings savingSettings, ExecutorService es) {
        this.image = image;
        this.savingSettings = savingSettings;
        this.es = es;
        this.stop = new AtomicBoolean(false);
    }

    @Override
    public void startSave() {
        List<Future> futures = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger(0);
        Logger.info( "Configuring Imaris dataset..." );
        ImarisDataSet imarisDataSet = createImarisDataSet( savingSettings, stop, image );
        final long startTime = System.currentTimeMillis();

        int tStart = Math.max( savingSettings.tStart, 0 );
        int tEnd = (int) Math.min( savingSettings.tEnd, image.getRai().dimension( DimensionOrder.T ) - 1 );

        for (int t = tStart; t <= tEnd; t++) {
            futures.add(
                    es.submit(
                            new ImarisFrameSaver<R>(
                                    image,
                                    savingSettings,
                                    imarisDataSet,
                                    t, counter, startTime, stop)
                ));
        }

        // Monitor the progress
        // Todo: one could also use the counter for the progress, rather than the futures
        Thread thread = new Thread(() -> Progress.informProgressListeners(
                futures,
                FileInfos.PROGRESS_UPDATE_MILLISECONDS,
				progressListeners ));
        thread.start();
    }

    @Override
    public void stopSave() {
        this.stop.set(true);
        Utils.shutdownThreadPack( es, TIME_OUT_SECONDS );
    }

    private static ImarisDataSet createImarisDataSet( SavingSettings settings, AtomicBoolean stop, Image< ? > image ) {
        final String directory = new File( settings.volumesFilePathStump ).getParent();
        final String filename = new File( settings.volumesFilePathStump ).getName();

        ImagePlus imp = Utils.asImagePlus( image, settings.tStart, settings.tEnd );

        int[] binning = new int[]{1,1,1};

        ImarisDataSet imarisDataSet = new ImarisDataSet(
                imp,
                binning,
                directory,
                filename );

        if (stop.get())  return null;

        ImarisWriter.writeHeaderFile(
                imarisDataSet,
                directory,
                filename + ".ims"
        );

        ArrayList<File> imarisFiles = ImarisUtils.getImarisFiles( directory );

        if (imarisFiles.size() > 1)
            ImarisWriter.writeCombinedHeaderFile(imarisFiles, "meta.ims");

        Logger.info("Image sizes at different resolutions:");
        Utils.logArrayList(imarisDataSet.getDimensions());
        Logger.info("Image chunking:");
        Utils.logArrayList(imarisDataSet.getChunks());
        return imarisDataSet;
    }
}
