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
package stuff;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.log.progress.LoggingProgressListener;
import de.embl.cba.bdp2.open.NamingSchemes;
import de.embl.cba.bdp2.devel.register.RegisteredViews;
import de.embl.cba.bdp2.devel.register.Registration;
import de.embl.cba.bdp2.BigDataProcessor2;
import loci.common.DebugTools;
import net.imagej.ImageJ;
import net.imglib2.FinalInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class TestAlignMoviePhaseCorrelation< R extends RealType< R > & NativeType< R > >
{
	public static boolean showImages = false;

	public void isabell()
	{
		DebugTools.setRootLevel("OFF"); // Bio-Formats

		// short movie
//		final Image< R > image = BigDataProcessor2.openImage(
//				"/Users/tischer/Documents/fiji-plugin-bigDataProcessor2/src/test/resources/test-data/sift-align-movie",
//				FileInfos.SINGLE_CHANNEL_TIMELAPSE,
//				".*");

		// long movie
		final Image< R > image = BigDataProcessor2.openTIFFSeries(
				"/Users/tischer/Documents/fiji-plugin-bigDataProcessor2/src/test/resources/test-data/light-sheet-drift-01",
				NamingSchemes.SINGLE_CHANNEL_TIMELAPSE );

		if ( showImages )
			BigDataProcessor2.showImage( image, false ).setDisplaySettings( 100, 200, 0 );

		final FinalInterval hyperSliceInterval = FinalInterval.createMinMax(
				0, 0, image.getRai().dimension( 2 ) / 2,
				image.getRai().max( 0 ), image.getRai().max( 1 ), image.getRai().dimension( 2 ) / 2 );

		final Image< R > alignedImage =
				RegisteredViews.alignMovie(
						image,
						3,
						hyperSliceInterval,
						true,
						new LoggingProgressListener( "PhaseCorrelation" ),
						Registration.PHASE_CORRELATION );

		if ( showImages )
			BigDataProcessor2.showImage( alignedImage, false ).setDisplaySettings( 100, 200, 0 );

//		final SavingSettings savingSettings = SavingSettings.getDefaults();
//		savingSettings.fileType = SavingSettings.FileType.Tiff_PLANES;
//		savingSettings.numIOThreads = 4;
//		savingSettings.numProcessingThreads = 4;
//		final String dir = "/Users/tischer/Documents/fiji-plugin-bigDataProcessor2/src/test/resources/test-data/sift-aligned-em";
//		emptyDirectory( dir );
//		savingSettings.volumesFilePath = dir + "/plane";
//		savingSettings.saveVolumes = true;
//		BigDataProcessor2.saveImageAndWaitUntilDone( savingSettings, alignedImage );

	}


	public void gustavo()
	{
		DebugTools.setRootLevel("OFF"); // Bio-Formats

		final Image< R > image = BigDataProcessor2.openTIFFSeries(
				"/Users/tischer/Documents/fiji-plugin-bigDataProcessor2/src/test/resources/test-data/gustavo-drift",
				NamingSchemes.LOAD_CHANNELS_FROM_FOLDERS );

		if ( showImages )
			BigDataProcessor2.showImage( image, false ).setDisplaySettings( 0, 200,  0 );

		long channel = 1;

		final FinalInterval hyperSliceInterval = FinalInterval.createMinMax(
				0, 0, image.getRai().dimension( 2 ) / 2, channel,
				image.getRai().max( 0 ), image.getRai().max( 1 ), image.getRai().dimension( 2 ) / 2, channel );

		final Image< R > alignedImage =
				RegisteredViews.alignMovie(
						image,
						3,
						hyperSliceInterval,
						true,
						new LoggingProgressListener( "PhaseCorrelation" ),
						Registration.PHASE_CORRELATION );

		if ( showImages )
			BigDataProcessor2.showImage( alignedImage, false ).setDisplaySettings( 0, 200, 0 );

//		final SavingSettings savingSettings = SavingSettings.getDefaults();
//		savingSettings.fileType = SavingSettings.FileType.Tiff_PLANES;
//		savingSettings.numIOThreads = 4;
//		savingSettings.numProcessingThreads = 4;
//		final String dir = "/Users/tischer/Documents/fiji-plugin-bigDataProcessor2/src/test/resources/test-data/sift-aligned-em";
//		emptyDirectory( dir );
//		savingSettings.volumesFilePath = dir + "/plane";
//		savingSettings.saveVolumes = true;
//		BigDataProcessor2.saveImageAndWaitUntilDone( savingSettings, alignedImage );

	}

	public static void main( String[] args )
	{
		showImages = true;
		new ImageJ().ui().showUI();
		new TestAlignMoviePhaseCorrelation().gustavo();
		//new TestAlignMoviePhaseCorrelation().isabell();
	}

}
