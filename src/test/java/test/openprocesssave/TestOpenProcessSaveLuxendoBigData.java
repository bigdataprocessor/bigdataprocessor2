package test.openprocesssave;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.log.progress.LoggingProgressListener;
import de.embl.cba.bdp2.save.SaveFileType;
import de.embl.cba.bdp2.save.SavingSettings;
import test.Utils;

public class TestOpenProcessSaveLuxendoBigData
{
	public static void main( String[] args )
	{
		Utils.prepareInteractiveMode();

		new TestOpenProcessSaveLuxendoBigData().run();
	}

	//@Test
	public void run()
	{
		Image image = BigDataProcessor2.openHDF5Series( "/Volumes/Tischi/big-image-data/luxendo-publication-figure/mouse_2_Cam", ".*stack_6_(?<C1>channel_.*)/(?<C2>Cam_.*)_(?<T>\\d+).h5", "Data", new String[]{"channel_2_Cam_Long","channel_2_Cam_Short"} );
		BigDataProcessor2.showImage( image, true );

		// Bin...
		image = BigDataProcessor2.bin( image, new long[]{5,5,1,1,1});
		image.setName( "mouse_2_Cam-bin" );

		// Crop...
		image = BigDataProcessor2.crop( image, new long[]{116,99,0,0,0,346,328,99,1,143} );
		image.setName( "mouse_2_Cam-bin-crop" );

// Save...
		SavingSettings savingSettings = SavingSettings.getDefaults();
		savingSettings.volumesFilePathStump = "/Volumes/Tischi/big-image-data/deleteme/volumes/mouse_2_Cam-bin-crop";
		savingSettings.projectionsFilePathStump = "/Volumes/Tischi/big-image-data/deleteme/projections/mouse_2_Cam-bin-crop";
		savingSettings.numIOThreads = 1;
		savingSettings.numProcessingThreads = 4;
		savingSettings.fileType = SaveFileType.TIFFVolumes;
		savingSettings.saveProjections = false;
		savingSettings.saveVolumes = true;
		savingSettings.compression = "None";
		savingSettings.tStart = 0;
		savingSettings.tEnd = 143;
		BigDataProcessor2.saveImage( image, savingSettings, new LoggingProgressListener( "Files saved" ) );

	}
}
