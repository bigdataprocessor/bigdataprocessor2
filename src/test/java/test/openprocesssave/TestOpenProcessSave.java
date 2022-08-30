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
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.log.progress.LoggingProgressListener;
import de.embl.cba.bdp2.save.SaveFileType;
import de.embl.cba.bdp2.save.SavingSettings;
import org.junit.jupiter.api.Test;
import test.Utils;

import java.util.ArrayList;

public class TestOpenProcessSave
{
	private static Image image;

	public static void main( String[] args )
	{
		Utils.prepareInteractiveMode();
		new TestOpenProcessSave().run();
		BigDataProcessor2.showImage( image );
	}

	@Test
	public void run()
	{
		Logger.useSystemOut( true );

		image = BigDataProcessor2.openTIFFSeries( "src/test/resources/test/tiff-nc2-nt2-16bit", ".*--C(?<C>\\d+)--T(?<T>\\d+).tif" );

		image = BigDataProcessor2.setVoxelSize( image, new double[]{2.0,2.0,2.0}, "µm" );

		image = BigDataProcessor2.rename( image, "image", new String[]{"ch0","ch1"} );

		ArrayList< long[] > shiftsXYZC = new ArrayList< long[] >();
		shiftsXYZC.add( new long[]{0,13,0,0} );
		shiftsXYZC.add( new long[]{0,1,5,0} );
		image = BigDataProcessor2.alignChannels( image, shiftsXYZC );

		image = BigDataProcessor2.bin( image, new long[]{2,2,1,1,1} );
		image.setName( "image-binned" );

		image = BigDataProcessor2.convertToUnsignedByteType( image, new double[]{173.0,103.0}, new double[]{445.0,259.0} );
		image.setName( "image-binned-8bit" );

		image = BigDataProcessor2.crop( image, new long[]{5,24,0,0,0,47,58,82,1,1} );
		image.setName( "image-binned-8bit-crop" );

		final SavingSettings settings = SavingSettings.getDefaults();
		settings.volumesFilePathStump = "src/test/resources/test/output/imaris/" + image.getName();
		settings.fileType = SaveFileType.ImarisVolumes;
		settings.numProcessingThreads = 4;
		settings.numIOThreads = 1;
		settings.compression = SavingSettings.COMPRESSION_NONE;
		settings.tStart = 0;
		settings.tEnd = image.getNumTimePoints() - 1;

		BigDataProcessor2.saveImage( image, settings, new LoggingProgressListener( "Progress" ) );
	}
}
