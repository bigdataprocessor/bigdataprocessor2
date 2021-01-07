package stuff;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.log.progress.LoggingProgressListener;
import de.embl.cba.bdp2.open.NamingSchemes;
import de.embl.cba.bdp2.devel.register.RegisteredViews;
import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.viewer.ImageViewer;
import loci.common.DebugTools;
import net.imagej.ImageJ;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class TestAlignVolumeSIFT< R extends RealType< R > & NativeType< R > >
{
	public static boolean showImages = false;

	//@Test
	public void lazySIFT()
	{
		DebugTools.setRootLevel("OFF"); // Bio-Formats

		final Image< R > image = BigDataProcessor2.openTIFFSeries(
				"/Users/tischer/Documents/fiji-plugin-bigDataProcessor2/src/test/resources/test-data/em-2d-sift-align-01",
				NamingSchemes.TIFF_SLICES );

		final Image< R > alignedImage =
				RegisteredViews.siftAlignFirstVolume(
						image,
						20,
						true,
						new LoggingProgressListener( "SIFT" ) );

		if ( showImages )
		{
			final ImageViewer viewer = BigDataProcessor2.showImage( alignedImage, false );
			viewer.setDisplaySettings( 0, 65535,0 );
		}

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
		new TestAlignVolumeSIFT().lazySIFT();
	}

}
