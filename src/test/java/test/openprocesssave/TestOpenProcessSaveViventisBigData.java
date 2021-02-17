package test.openprocesssave;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.log.progress.LoggingProgressListener;
import de.embl.cba.bdp2.save.SaveFileType;
import de.embl.cba.bdp2.save.SavingSettings;
import org.junit.Test;

public class TestOpenProcessSaveViventisBigData
{
	public static void main( String[] args )
	{
		new TestOpenProcessSaveViventisBigData().run();
	}

	@Test
	public void run()
	{
		Image image = BigDataProcessor2.openTIFFSeries( "/Volumes/Tischi/big-image-data/viventis/Position 2_Settings 1", "t(?<T>\\d+)_(?<C>.+).tif_NONRECURSIVE" );
		BigDataProcessor2.showImage( image, true );

		// Crop...
		image = BigDataProcessor2.crop( image, new long[]{214,224,12,0,0,865,825,29,1,1} );
		image.setName( "Position 2_Settings 1-crop" );

// Save...
		SavingSettings savingSettings = SavingSettings.getDefaults();
		savingSettings.volumesFilePathStump = "/Volumes/Tischi/big-image-data/deleteme/volumes/Position 2_Settings 1-crop";
		savingSettings.projectionsFilePathStump = "/Volumes/Tischi/big-image-data/deleteme/projections/Position 2_Settings 1-crop";
		savingSettings.numIOThreads = 1;
		savingSettings.numProcessingThreads = 4;
		savingSettings.fileType = SaveFileType.TIFFVolumes;
		savingSettings.saveProjections = true;
		savingSettings.saveVolumes = true;
		savingSettings.compression = "None";
		savingSettings.tStart = 0;
		savingSettings.tEnd = 1;
		BigDataProcessor2.saveImage( image, savingSettings, new LoggingProgressListener( "Files saved" ) );
	}
}
