package presentations.tim2020;

import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.progress.LoggingProgressListener;
import de.embl.cba.bdp2.saving.SavingSettings;
import de.embl.cba.bdp2.ui.BigDataProcessor2;

public class OpenLuxendoTwoChannelMovie
{
	public static void main( String[] args )
	{
		final Image image = BigDataProcessor2.openHdf5Image( "/Volumes/cba/exchange/bigdataprocessor/data/tim2020/luxendo-two-channel-movie",
				FileInfos.LOAD_CHANNELS_FROM_FOLDERS,
				FileInfos.PATTERN_LUXENDO_2,
				"Data"
		);

		BigDataProcessor2.showImage( image );
	}
}
