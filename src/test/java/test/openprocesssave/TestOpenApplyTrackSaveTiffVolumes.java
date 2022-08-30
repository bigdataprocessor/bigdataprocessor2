/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2022 EMBL
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
package test.openprocesssave;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.log.progress.LoggingProgressListener;
import de.embl.cba.bdp2.save.SavingSettings;
import org.junit.jupiter.api.Test;
import test.Utils;

import java.io.File;

import static de.embl.cba.bdp2.open.NamingSchemes.MULTI_CHANNEL_VOLUMES;
import static de.embl.cba.bdp2.open.NamingSchemes.TIF;

public class TestOpenApplyTrackSaveTiffVolumes
{
    public static Image image;
    public static Image trackedImage;

    public static void main(String[] args)
    {
        Utils.prepareInteractiveMode();
        new TestOpenApplyTrackSaveTiffVolumes().run();
        BigDataProcessor2.showImage( image, true );
        BigDataProcessor2.showImage( trackedImage, true );
    }

    @Test
    public void run()
    {
        final String directory = "src/test/resources/test/tiff-nc2-nt6";

        // open
        image = BigDataProcessor2.openTIFFSeries( directory, MULTI_CHANNEL_VOLUMES + TIF );
        image.setVoxelDimensions( new double[]{1.0, 1.0, 1.0} );

        // track
        trackedImage = BigDataProcessor2.applyTrack( new File( "src/test/resources/test/tracks/tiff-nc2-nt6-with-z-drift.json" ), TestOpenApplyTrackSaveTiffVolumes.image, false );

        // save
        final SavingSettings settings = SavingSettings.getDefaults();
        settings.volumesFilePathStump = "src/test/resources/test/output/tracked-tiff/" + trackedImage.getName();
        settings.tStart = 0;
        settings.tEnd = trackedImage.getNumTimePoints() - 1;
        BigDataProcessor2.saveImage( trackedImage, settings, new LoggingProgressListener( "Progress" ) );
    }
}
