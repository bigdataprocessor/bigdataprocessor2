package presentations.tim2020;

import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.ui.BigDataProcessor2;

public class OpenLuxendoSplitChipMovie
{
	public static void main( String[] args )
	{
		final Image image = BigDataProcessor2.openHdf5Image( "/g/cba/exchange/bigdataprocessor/data/tim2020/luxendo-split-chip-movie",
				FileInfos.SINGLE_CHANNEL_TIMELAPSE,
				FileInfos.PATTERN_LUXENDO_2,
				"Data"
		);

		BigDataProcessor2.showImage( image );
	}
}
