package explore;

import bdv.img.imaris.Imaris;
import bdv.img.remote.RemoteImageLoader;
import bdv.spimdata.SpimDataMinimal;
import bdv.spimdata.XmlIoSpimDataMinimal;
import bdv.util.BdvFunctions;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.XmlIoSpimData;

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
