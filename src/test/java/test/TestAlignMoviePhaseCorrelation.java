package test;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.log.progress.LoggingProgressListener;
import de.embl.cba.bdp2.open.core.NamingSchemes;
import de.embl.cba.bdp2.register.RegisteredViews;
import de.embl.cba.bdp2.register.Registration;
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
		final Image< R > image = BigDataProcessor2.openImage(
				"/Users/tischer/Documents/fiji-plugin-bigDataProcessor2/src/test/resources/test-data/light-sheet-drift-01",
				NamingSchemes.SINGLE_CHANNEL_TIMELAPSE,
				".*");

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
//		savingSettings.fileType = SavingSettings.FileType.TIFF_PLANES;
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

		final Image< R > image = BigDataProcessor2.openImage(
				"/Users/tischer/Documents/fiji-plugin-bigDataProcessor2/src/test/resources/test-data/gustavo-drift",
				NamingSchemes.LOAD_CHANNELS_FROM_FOLDERS,
				".*");

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
//		savingSettings.fileType = SavingSettings.FileType.TIFF_PLANES;
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
