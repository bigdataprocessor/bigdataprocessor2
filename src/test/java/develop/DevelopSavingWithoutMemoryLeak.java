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
package develop;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.utils.RAISlicer;
import net.imagej.ImageJ;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.view.Views;

import static de.embl.cba.bdp2.open.NamingSchemes.LUXENDO;

public class DevelopSavingWithoutMemoryLeak
{
	public static void main( String[] args )
	{
		final ImageJ imageJ = new ImageJ();
		imageJ.ui().showUI();

		String regExp = LUXENDO.replace( "STACK", "" + 6 );

		// /Volumes/cba/exchange/bigdataprocessor/data/mouse_2cam_publication_figure/stack_6_channel_2
		final Image image = BigDataProcessor2.openHDF5Series(
				"/Volumes/cba/exchange/bigdataprocessor/data/mouse_2cam_publication_figure",
				regExp,
				"Data" );

		RandomAccessibleInterval volumeView = Views.dropSingletonDimensions(  RAISlicer.getVolumeView( image.getRai(), 0, 10 ) );

		// TODO: this method always accesses the first image! remove this!
		// create RandomAccess
		final RandomAccess< ? > randomAccess = volumeView.randomAccess( );

		// place it at the first pixel

		// volumeView.min( randomAccess );

		//Util.getTypeFromInterval(  )

		//Object o = randomAccess.get();

//		BigDataProcessor2.showImage( image );
//
//		final SavingSettings savingSettings = SavingSettings.getDefaults();
//		savingSettings.saveFileType = SavingSettings.SaveFileType.Tiff_VOLUMES;
//		savingSettings.numIOThreads = 1;
//		savingSettings.saveProjections = false;
//		savingSettings.saveVolumes = true;
//		savingSettings.volumesFilePathStump = "/Volumes/cba/exchange/bigdataprocessor/data/tmp/volumes-";
//
//		BigDataProcessor2.saveImage( image, savingSettings, new LoggingProgressListener( "Progress" ) );
	}
}
