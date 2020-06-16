package presentations.tim2020;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.open.core.NamingScheme;

public class OpenShearingSingleChannelMovie
{
	public static void main( String[] args )
	{
		final Image image = BigDataProcessor2.openImage( "/Volumes/cba/exchange/bigdataprocessor/data/nils/",
				NamingScheme.SINGLE_CHANNEL_TIMELAPSE,
				NamingScheme.PATTERN_ALL
		);

		BigDataProcessor2.showImage( image);
	}
}
