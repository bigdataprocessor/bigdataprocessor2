package tests;

import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.progress.LoggingProgressListener;
import de.embl.cba.bdp2.registration.RegisteredViews;
import de.embl.cba.bdp2.registration.Registration;
import de.embl.cba.bdp2.ui.BigDataProcessor2;
import loci.common.DebugTools;
import net.imagej.ImageJ;
import net.imglib2.FinalInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.junit.Test;

public class TestAlignMoviePhaseCorrelation< R extends RealType< R > & NativeType< R > >
{
	public static boolean showImages = false;

	@Test
	public void run()
	{
		DebugTools.setRootLevel("OFF"); // Bio-Formats

		// short movie
//		final Image< R > image = BigDataProcessor2.openImage(
//				"/Users/tischer/Documents/fiji-plugin-bigDataTools2/src/test/resources/test-data/sift-align-movie",
//				FileInfos.SINGLE_CHANNEL_TIMELAPSE,
//				".*");

		// long movie
		final Image< R > image = BigDataProcessor2.openImage(
				"/Users/tischer/Documents/fiji-plugin-bigDataTools2/src/test/resources/test-data/light-sheet-drift-01",
				FileInfos.SINGLE_CHANNEL_TIMELAPSE,
				".*");

		if ( showImages )
			BigDataProcessor2.showImage( image, false ).setDisplayRange( 100, 200, 0 );

		final FinalInterval hyperSliceInterval = FinalInterval.createMinMax( 0, 0, image.getRai().dimension( 2 ) / 2,
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
			BigDataProcessor2.showImage( alignedImage, false ).setDisplayRange( 100, 200, 0 );

//		final SavingSettings savingSettings = SavingSettings.getDefaults();
//		savingSettings.fileType = SavingSettings.FileType.TIFF_PLANES;
//		savingSettings.numIOThreads = 4;
//		savingSettings.numProcessingThreads = 4;
//		final String dir = "/Users/tischer/Documents/fiji-plugin-bigDataTools2/src/test/resources/test-data/sift-aligned-em";
//		emptyDirectory( dir );
//		savingSettings.volumesFilePath = dir + "/plane";
//		savingSettings.saveVolumes = true;
//		BigDataProcessor2.saveImageAndWaitUntilDone( savingSettings, alignedImage );

	}

	public static void main( String[] args )
	{
		showImages = true;
		new ImageJ().ui().showUI();
		new TestAlignMoviePhaseCorrelation().run();
	}

}
