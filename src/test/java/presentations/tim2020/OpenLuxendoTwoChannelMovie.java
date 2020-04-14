package presentations.tim2020;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.open.files.FileInfos;
import de.embl.cba.bdp2.BigDataProcessor2;

public class OpenLuxendoTwoChannelMovie
{
	public static void main( String[] args )
	{
		final Image image = BigDataProcessor2.openHdf5Image( "/Volumes/USB Drive/tim2020/luxendo-two-channel-movie",
				FileInfos.LOAD_CHANNELS_FROM_FOLDERS,
				FileInfos.PATTERN_LUXENDO,
				"Data"
		);

		BigDataProcessor2.showImage( image);

	}
}
