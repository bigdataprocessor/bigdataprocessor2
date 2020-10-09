package presentations.tim2020;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.open.NamingSchemes;

public class OpenLuxendoTwoChannelMovie
{
	public static void main( String[] args )
	{
		final Image image = BigDataProcessor2.openImageFromHdf5( "/Volumes/USB Drive/tim2020/luxendo-two-channel-movie",
				NamingSchemes.LOAD_CHANNELS_FROM_FOLDERS,
				NamingSchemes.PATTERN_LUXENDO,
				"Data");

		BigDataProcessor2.showImage( image);

	}
}
