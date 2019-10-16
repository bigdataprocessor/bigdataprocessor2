package tests;

import bdv.ViewerImgLoader;
import bdv.viewer.Source;
import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.progress.LoggingProgressListener;
import de.embl.cba.bdp2.registration.RegisteredViews;
import de.embl.cba.bdp2.saving.SavingSettings;
import de.embl.cba.bdp2.ui.BigDataProcessor2;
import loci.common.DebugTools;
import net.imagej.ImageJ;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.junit.Test;

public class TestEMProcessingAndSaving< R extends RealType< R > & NativeType< R > >
{

	@Test
	public void processAndSave()
	{
		//new ImageJ().ui().showUI();

		DebugTools.setRootLevel("OFF"); // Bio-Formats

		Image< R > image = BigDataProcessor2.openImage(
				"/Users/tischer/Documents/fiji-plugin-bigDataProcessor2/src/test/resources/test-data/em-2d-sift-align-01",
				FileInfos.TIFF_SLICES,
				".*.tif" );

//		image = align( image );

		image = BigDataProcessor2.convert( image, 65535, 0 );

		image = BigDataProcessor2.bin( image, new long[]{ 3, 3, 1, 1, 1} );

		final SavingSettings savingSettings = getSavingSettings();

		BigDataProcessor2.saveImage(
				image,
				savingSettings,
				new LoggingProgressListener( "Saved files" ));

	}

	private SavingSettings getSavingSettings()
	{
		final SavingSettings savingSettings = SavingSettings.getDefaults();
		savingSettings.fileType = SavingSettings.FileType.TIFF_PLANES;
		savingSettings.numIOThreads = 4;
		savingSettings.numProcessingThreads = 4;

		String directory = "/Users/tischer/Documents/fiji-plugin-bigDataProcessor2/src/test/resources/test-data/sift-aligned-em";

		de.embl.cba.bdp2.utils.FileUtils.emptyDirectory( directory );

		savingSettings.volumesFilePath = directory +"/plane";

		savingSettings.saveVolumes = true;
		savingSettings.compression = SavingSettings.COMPRESSION_ZLIB;
		return savingSettings;
	}

	private Image< R > align( Image< R > image )
	{
		return RegisteredViews.siftAlignFirstVolume(
						image,
						20,
						true,
						new LoggingProgressListener( "SIFT" ) );
	}

	public static void main( String[] args )
	{
		new ImageJ().ui().showUI(); // for the logging
		new TestEMProcessingAndSaving().processAndSave();
	}

}
