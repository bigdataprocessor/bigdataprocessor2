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
package develop;

import bdv.BigDataViewer;
import bdv.tools.brightness.ConverterSetup;
import bdv.util.BdvFunctions;
import bdv.viewer.Source;
import bdv.viewer.SourceAndConverter;
import ch.epfl.biop.bdv.bioformats.bioformatssource.BioFormatsBdvOpener;
import ch.epfl.biop.bdv.bioformats.command.OpenFilesWithBigdataviewerBioformatsBridgeCommand;
import ch.epfl.biop.bdv.bioformats.export.spimdata.BioFormatsConvertFilesToSpimData;
import de.embl.cba.bdv.utils.BdvUtils;
import loci.common.DebugTools;
import mpicbg.spim.data.generic.AbstractSpimData;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imagej.ImageJ;
import net.imglib2.RandomAccessibleInterval;
import org.renjin.gnur.api.R;
import org.scijava.command.CommandModule;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class DevelopCZIOpening
{
	public static void main( String[] args ) throws ExecutionException, InterruptedException
	{
		DebugTools.setRootLevel("OFF"); // Bio-Formats

		OpenFilesWithBigdataviewerBioformatsBridgeCommand bridgeCommand = new OpenFilesWithBigdataviewerBioformatsBridgeCommand();
		bridgeCommand.usebioformatscacheblocksize = true;
		BioFormatsBdvOpener opener = bridgeCommand.getOpener( new File( "/Volumes/cba/exchange/bigdataprocessor/data/czi/20180125CAGtdtomato_ERT2CreLuVeLu_notamox_03_Average_Subset.czi" ) );

		AbstractSpimData< ? > spimData = BioFormatsConvertFilesToSpimData.getSpimData( opener );
		//BdvFunctions.show( spimData );

		Map viewDescriptions = spimData.getSequenceDescription().getViewDescriptions();
		List< ConverterSetup > converterSetups = new ArrayList<>();
		List< SourceAndConverter< ? > > sources = new ArrayList<>();
		BigDataViewer.initSetups( spimData, converterSetups, sources );
		RandomAccessibleInterval< ? > rai = sources.get( 0 ).getSpimSource().getSource( 0, 0 );
		VoxelDimensions voxelDimensions = sources.get( 0 ).getSpimSource().getVoxelDimensions();
		RandomAccessibleInterval< ? > volatileRai = sources.get( 0 ).asVolatile().getSpimSource().getSource( 0, 0 );
		int a = 1;

	}
}
