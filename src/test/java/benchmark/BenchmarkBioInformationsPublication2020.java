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
package benchmark;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.BigDataProcessor2Command;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.save.SaveFileType;
import de.embl.cba.bdp2.save.SavingSettings;
import net.imagej.ImageJ;

public class BenchmarkBioInformationsPublication2020
{
	public static void main( String[] args )
	{
		ImageJ imageJ = new ImageJ();
		imageJ.ui().showUI();
		imageJ.command().run( BigDataProcessor2Command.class, true );
		Logger.setLevel( Logger.Level.Benchmark );

		String root = "/Users/tischer/Desktop/bpd2-benchmark/h5";
//		String root = "/Users/tischer/Desktop/bpd2-benchmark/tif";

//		String root = "/Volumes/cba/exchange/bigdataprocessor/data/benchmark";

		Image image = BigDataProcessor2.openHDF5Series( root + "/in",".*stack_6_(?<C1>channel_.*)/(?<C2>Cam_.*)_(?<T>\\d+).h5","Data" );

//		Image image = BigDataProcessor2.openTIFFSeries( root + "/in", "(?<T>.*).tif" );
//
		//image = BigDataProcessor2.bin( image, new long[]{3,3,1,1,1} );
//
		BigDataProcessor2.showImage( image );

//		SavingSettings savingSettings = new SavingSettings();
//		savingSettings.volumesFilePathStump = root + "/out/volumes";
//		savingSettings.numIOThreads = 1;
//		savingSettings.numProcessingThreads = 4;
//		savingSettings.fileType = SaveFileType.TIFFVolumes;
//		savingSettings.saveProjections = false;
//		savingSettings.saveVolumes = true;
//		savingSettings.compression = savingSettings.COMPRESSION_NONE;
//		savingSettings.tStart = 0;
//		savingSettings.tEnd = 9;
//		BigDataProcessor2.saveImageAndWaitUntilDone( image, savingSettings );
	}
}
