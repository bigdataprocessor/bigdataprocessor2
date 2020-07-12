package benchmark;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.BigDataProcessor2UI;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.open.core.NamingSchemes;

import static de.embl.cba.bdp2.open.core.NamingSchemes.TIF;

public class Read2kBy2kImage
{
	public static void main( String[] args )
	{
		final BigDataProcessor2 bdp = new BigDataProcessor2();

		final String directory = "/Users/tischer/Desktop/test/";

		final Image image = bdp.openImage(
				directory,
				NamingSchemes.SINGLE_CHANNEL_VOLUMES + TIF,
				".*" );

		BigDataProcessor2.showUI();
		BigDataProcessor2.showImage( image );
	}
}
