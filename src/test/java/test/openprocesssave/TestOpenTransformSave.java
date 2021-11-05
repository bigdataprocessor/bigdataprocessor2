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
package test.openprocesssave;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.BigDataProcessor2UI;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.log.progress.LoggingProgressListener;
import de.embl.cba.bdp2.process.transform.TransformConstants;
import de.embl.cba.bdp2.save.SaveFileType;
import de.embl.cba.bdp2.save.SavingSettings;
import net.imagej.ImageJ;
import org.junit.Test;
import test.Utils;

import java.util.ArrayList;

public class TestOpenTransformSave
{
	private static ImageJ imageJ;
	private static boolean interactive = false;

	public static void main( String[] args )
	{
		interactive = true;
		new TestOpenTransformSave().run();
	}

	@Test
	public void run()
	{
		imageJ = Utils.initContext();

		if ( interactive )
		{
			imageJ.ui().showUI();
			BigDataProcessor2UI.showUI();;
		}

		Image image = BigDataProcessor2.openTIFFSeries( "src/test/resources/test/tiff-nc2-nt2-16bit", ".*--C(?<C>\\d+)--T(?<T>\\d+).tif" );

		image = BigDataProcessor2.setVoxelSize( image, new double[]{2.0,2.0,2.0}, "µm" );

		image = BigDataProcessor2.rename( image, "image", new String[]{"ch0","ch1"} );

		image = BigDataProcessor2.transform( image, new double[]{
				2.0, 0.0, 0.0, 0.0,   0.0, 1.0, 0.0, 0.0,   0.0, 0.0, 1.0, 0.0 }, TransformConstants.LINEAR );

		final SavingSettings settings = SavingSettings.getDefaults();
		settings.volumesFilePathStump = "src/test/resources/test/output/tiff/" + image.getName();
		settings.fileType = SaveFileType.TIFFVolumes;
		settings.numProcessingThreads = 4;
		settings.numIOThreads = 1;
		settings.compression = SavingSettings.COMPRESSION_NONE;
		settings.tStart = 0;
		settings.tEnd = image.getNumTimePoints() - 1;

		BigDataProcessor2.saveImage( image, settings, new LoggingProgressListener( "Progress" ) );
	}
}
