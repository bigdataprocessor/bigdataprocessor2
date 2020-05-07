package presentations.tim2020;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.read.NamingScheme;

public class OpenLuxendoSplitChipMovie
{
	public static void main( String[] args )
	{
		final Image image = BigDataProcessor2.openHdf5Image( "/g/cba/exchange/bigdataprocessor/data/tim2020/luxendo-split-chip-movie",
				NamingScheme.SINGLE_CHANNEL_TIMELAPSE,
				NamingScheme.PATTERN_LUXENDO,
				"Data"
		);

		BigDataProcessor2.showImage( image);
	}
}
