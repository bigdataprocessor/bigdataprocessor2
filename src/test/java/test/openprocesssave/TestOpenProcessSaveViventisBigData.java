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
package test.openprocesssave;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.log.progress.LoggingProgressListener;
import de.embl.cba.bdp2.save.SaveFileType;
import de.embl.cba.bdp2.save.SavingSettings;
import test.Utils;

public class TestOpenProcessSaveViventisBigData
{
	private static Image image;

	public static void main( String[] args )
	{
		Utils.prepareInteractiveMode();
		new TestOpenProcessSaveViventisBigData().run();
		BigDataProcessor2.showImage( image, true );
	}

	//@Test
	public void run()
	{
		image = BigDataProcessor2.openTIFFSeries( "/Volumes/Tischi/big-image-data/viventis/Position 2_Settings 1", "t(?<T>\\d+)_(?<C>.+).tif_NONRECURSIVE" );

		// Crop...
		image = BigDataProcessor2.crop( image, new long[]{214,224,12,0,0,865,825,29,1,1} );
		image.setName( "Position 2_Settings 1-crop" );

// Save...
		SavingSettings savingSettings = SavingSettings.getDefaults();
		savingSettings.volumesFilePathStump = "/Volumes/Tischi/big-image-data/deleteme/volumes/Position 2_Settings 1-crop";
		savingSettings.projectionsFilePathStump = "/Volumes/Tischi/big-image-data/deleteme/projections/Position 2_Settings 1-crop";
		savingSettings.numIOThreads = 1;
		savingSettings.numProcessingThreads = 4;
		savingSettings.fileType = SaveFileType.TIFFVolumes;
		savingSettings.saveProjections = true;
		savingSettings.saveVolumes = true;
		savingSettings.compression = "None";
		savingSettings.tStart = 0;
		savingSettings.tEnd = 1;
		BigDataProcessor2.saveImage( image, savingSettings, new LoggingProgressListener( "Progress" ) );
	}
}
