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
		bridgeCommand.useBioFormatsCacheBlockSize = true;
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
