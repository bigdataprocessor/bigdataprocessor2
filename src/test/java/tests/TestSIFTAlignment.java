package tests;

import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.registration.SIFTAlignedViews;
import de.embl.cba.bdp2.saving.SavingSettings;
import de.embl.cba.bdp2.ui.BigDataProcessor2;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import net.imagej.ImageJ;
import net.imglib2.RandomAccess;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.junit.Test;

import java.io.File;

public class TestSIFTAlignment < R extends RealType< R > & NativeType< R > >
{

	@Test
	public void lazySIFT()
	{
		new ImageJ().ui().showUI();

		final Image< R > image = BigDataProcessor2.openImage(
				"/Users/tischer/Documents/fiji-plugin-bigDataTools2/src/test/resources/test-data/em-2d-sift-align-01",
				FileInfos.TIFF_SLICES,
				".*.tif" );

		final Image< R > alignedImage =
				SIFTAlignedViews.lazySIFTAlignFirstVolume( image, 20 );

		final BdvImageViewer viewer = BigDataProcessor2.showImage( alignedImage, false );
		viewer.setDisplayRange( 0, 65535, 0  );

		final SavingSettings savingSettings = SavingSettings.getDefaults();
		savingSettings.fileType = SavingSettings.FileType.TIFF_PLANES;
		savingSettings.numIOThreads = 4;
		savingSettings.numProcessingThreads = 4;
		savingSettings.volumesFilePath =
				"/Users/tischer/Documents/fiji-plugin-bigDataTools2/src/test/resources/test-data/sift-aligned-em/plane";
		savingSettings.saveVolumes = true;

//		final File testVolumeFile =
//				new File( savingSettings.volumesFilePath + "--C00--T00000.tif" );
//		if ( testVolumeFile.exists() ) testVolumeFile.delete();
//
//		final File testProjectionsFile = new File( savingSettings.projectionsFilePath + "--xyz-max-projection--C00--T00002.tif" );
//		if ( testProjectionsFile.exists() ) testProjectionsFile.delete();

		BigDataProcessor2.saveImageAndWaitUntilDone( savingSettings, alignedImage );

	}

	public static void main( String[] args )
	{
		new TestSIFTAlignment().lazySIFT();
	}

}
