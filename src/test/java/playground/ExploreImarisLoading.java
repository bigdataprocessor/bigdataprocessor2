package playground;

import bdv.img.imaris.Imaris;
import bdv.spimdata.SpimDataMinimal;
import bdv.util.BdvFunctions;
import mpicbg.spim.data.SpimDataException;

import java.io.IOException;

public class ExploreImarisLoading
{
	public static void main( String[] args ) throws SpimDataException, IOException
	{
		final SpimDataMinimal spimData = Imaris.openIms( "/Users/tischer/Documents/fiji-plugin-bigDataProcessor2/src/test/resources/test-output/nc1-nt3-calibrated-imaris-volumes/volume.ims" );

		// new RemoteImageLoader

		BdvFunctions.show( spimData );
	}
}
