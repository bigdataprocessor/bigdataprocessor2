package users.alessandro;

import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.progress.LoggingProgressListener;
import de.embl.cba.bdp2.saving.SavingSettings;
import de.embl.cba.bdp2.ui.BigDataProcessor2;

public class OpenAndSaveLargeStacks202002
{
	public static void main( String[] args )
	{
		final Image image = BigDataProcessor2.openHdf5Image( "/Users/tischer/Desktop/2020-02-11_124401/stack_0_channel_4",
				FileInfos.SINGLE_CHANNEL_TIMELAPSE,
				FileInfos.PATTERN_LUXENDO_LEFT_CAM,
				"Data"
		);

		BigDataProcessor2.showImage( image );

		final SavingSettings savingSettings = SavingSettings.getDefaults();
		savingSettings.fileType = SavingSettings.FileType.TIFF_STACKS;
		savingSettings.numIOThreads = 1;
		savingSettings.numProcessingThreads = 1;
		savingSettings.voxelSpacing = image.getVoxelSpacing();
		savingSettings.voxelUnit = image.getVoxelUnit();

//		BigDataProcessor2.saveImage( image, savingSettings, new LoggingProgressListener( "Save" ) );
	}
}
