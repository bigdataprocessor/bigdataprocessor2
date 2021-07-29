/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2021 EMBL
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
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.save.Projector;
import de.embl.cba.bdp2.save.SavingSettings;
import de.embl.cba.bdp2.utils.RAISlicer;
import de.embl.cba.bdp2.utils.DimensionOrder;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.imaris.H5DataCubeWriter;
import de.embl.cba.imaris.ImarisDataSet;
import ij.ImagePlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static de.embl.cba.bdp2.save.Projector.maxProjectAndSaveAsTIFF;

public class ImarisFrameSaver< R extends RealType< R > & NativeType< R >> implements Runnable {
    private int t;
    private final int nFrames;
    private final int nChannels;
    private AtomicInteger counter;
    private SavingSettings settings;
    private final long startTime;
    private ImarisDataSet imarisDataSetProperties;
    private final AtomicBoolean stop;
    private final Image< R > image;

    public ImarisFrameSaver(
            Image< R > image,
            SavingSettings settings,
            ImarisDataSet imarisDataSet,
            int t,
            AtomicInteger counter,
            long startTime,
            AtomicBoolean stop)
    {
        this.image = image;
        this.nFrames = Math.toIntExact( image.getRai().dimension( DimensionOrder.T ) );
        this.nChannels = Math.toIntExact( image.getRai().dimension( DimensionOrder.C ) );
        this.settings = settings;
        this.t = t;
        this.counter = counter;
        this.startTime = startTime;
        this.imarisDataSetProperties = imarisDataSet;
        this.stop = stop;
    }

    @Override
    public void run()
    {
        Logger.debug( "# ImarisFrameSaver..." );

        long start;

        for ( int c = 0; c < nChannels; c++ )
        {
            if ( stop.get() )
            {
                settings.saveVolumes = false;
                Logger.progress( "Stopped save thread: ", "" + this.t );
                return;
            }

            RandomAccessibleInterval< R > raiXYZ = RAISlicer.createVolumeCopy( image.getRai(), c, t, settings.numProcessingThreads, image.getType() );

            ImagePlus imp3D = Utils.asImagePlus( raiXYZ, image, c );

            // Save volume
            if ( settings.saveVolumes )
            {
                start = System.currentTimeMillis();
                H5DataCubeWriter writer = new H5DataCubeWriter();
                writer.writeImarisCompatibleResolutionPyramid(
                        imp3D,
                        imarisDataSetProperties,
                        c,
                        t );
                Logger.benchmark( "Saved volume in [ ms ]: " + ( System.currentTimeMillis() - start ) );
            }

            // Save projections
            if ( settings.saveProjections )
            {
                Projector.saveProjections( imp3D, c, t, settings.projectionsFilePathStump, settings.projectionMode );
            }

            counter.incrementAndGet();

//            if (!stop.get())
//                ProgressHelpers.logProgress( totalFiles, counter, startTime, "Saved file " );

            imp3D = null;
            System.gc();
        }
    }
}
