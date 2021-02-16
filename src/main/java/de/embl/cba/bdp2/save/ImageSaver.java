package de.embl.cba.bdp2.save;

import de.embl.cba.bdp2.log.progress.ProgressListener;
import de.embl.cba.bdp2.utils.Utils;

public interface ImageSaver
{
	void addProgressListener( ProgressListener l );
	void startSave();
	void stopSave();

	default void createOutputDirectories( SavingSettings savingSettings )
	{
		if ( savingSettings.saveVolumes )
			Utils.createFilePathParentDirectories( savingSettings.volumesFilePathStump );

		if ( savingSettings.saveProjections )
			Utils.createFilePathParentDirectories( savingSettings.projectionsFilePathStump );
	}
}
