package users.giulia;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.open.core.NamingScheme;
import de.embl.cba.bdp2.save.SavingSettings;
import de.embl.cba.bdp2.BigDataProcessor2;
import net.imagej.ImageJ;
import net.imglib2.FinalInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import static junit.framework.Assert.assertTrue;

public class ProcessEMfromServer
{

	public < R extends RealType< R > & NativeType< R > > void invertEM()
	{
		final Image< R > image = BigDataProcessor2.openImage(
				"/Volumes/emcf/Mizzon/projects/Julian_FIBSEM/fib-SEM/20190730_batch6-blockB-prep/20190730_02UA_01GA_cell1",
				NamingScheme.TIFF_SLICES,
				".*.tif" );

		BigDataProcessor2.showImage( image);

		boolean process = false;

		if ( process )
		{

			final Image< R > crop = BigDataProcessor2.crop( image, new FinalInterval(
					new long[]{ 1500, 3600, 880, 0, 0 },
					new long[]{ 4050, 4800, 910, 0, 0 }
			) );

			final Image< R > convert = BigDataProcessor2.convert( crop, 65535, 0 );

			final SavingSettings savingSettings = SavingSettings.getDefaults();
			savingSettings.fileType = SavingSettings.FileType.TIFF_PLANES;
			savingSettings.numIOThreads = 4;
			savingSettings.numProcessingThreads = 4;
			savingSettings.saveProjections = false;
			savingSettings.saveVolumes = true;
			savingSettings.compression = SavingSettings.COMPRESSION_NONE;
			savingSettings.rowsPerStrip = 50;
			savingSettings.volumesFilePathStump =
					"/Users/tischer/Desktop/giulia/plane";

			//final File testVolumeFile = new File( savingSettings.volumesFilePath + "--C00--T00000.ome.tif" );
			//if ( testVolumeFile.exists() ) testVolumeFile.delete();

			//BigDataProcessor2.saveImageAndWaitUntilDone( savingSettings, crop );

			savingSettings.volumesFilePathStump =
					"/Users/tischer/Desktop/giulia-zlib/plane";
			savingSettings.compression = SavingSettings.COMPRESSION_LZW;
			BigDataProcessor2.saveImageAndWaitUntilDone( convert, savingSettings );
		}

		System.out.println("Done.");
	}


	public static void main( String[] args )
	{
		new ImageJ().ui().showUI();
		new ProcessEMfromServer().invertEM();
	}
}
