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

public class TestAlignMovieSIFT< R extends RealType< R > & NativeType< R > >
{
	public static boolean showImages = false;

	//@Test
	public void lazySIFT()
	{
		DebugTools.setRootLevel("OFF"); // Bio-Formats

		final Image< R > image = BigDataProcessor2.openTIFFSeries(
				"/Users/tischer/Documents/fiji-plugin-bigDataProcessor2/src/test/resources/test-data/gustavo-drift",
				NamingSchemes.LOAD_CHANNELS_FROM_FOLDERS );

		if ( showImages )
			BigDataProcessor2.showImage( image, true );

		final FinalInterval hyperSliceInterval = FinalInterval.createMinMax(
				0, 0, image.getRai().dimension( 2 ) / 2, 0,
				image.getRai().max( 0 ), image.getRai().max( 1 ), image.getRai().dimension( 2 ) / 2, 0 );

		final Image< R > alignedImage =
				RegisteredViews.alignMovie(
						image,
						3,
						hyperSliceInterval,
						true,
						new LoggingProgressListener( "SIFT" ),
						Registration.SIFT_CORRESPONDENCES );

		if ( showImages )
			BigDataProcessor2.showImage( alignedImage, false ).setDisplaySettings( 0, 200, 0 );
	}

	public static void main( String[] args )
	{
		showImages = true;
		new ImageJ().ui().showUI();
		new TestAlignMovieSIFT().lazySIFT();
	}

}
