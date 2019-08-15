package explore;

import bdv.util.BdvFunctions;
import bdv.util.BdvStackSource;
import de.embl.cba.bdv.utils.BdvUtils;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.XmlIoSpimData;
import mpicbg.spim.data.registration.ViewRegistration;
import mpicbg.spim.data.sequence.ViewId;
import mpicbg.spim.data.sequence.ViewSetup;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.ui.TransformListener;

import java.util.List;
import java.util.Map;

public class ExploreSpimDataReading
{
	public static void main( String[] args )
	{
		SpimData spimData = null;
		try
		{
			spimData = new XmlIoSpimData().load( "/Users/tischer/Desktop/views/153_hm.rec.2views.xml" );
		} catch ( SpimDataException e )
		{
			e.printStackTrace();
		}

		final Map< ViewId, ViewRegistration > viewRegistrations = spimData.getViewRegistrations().getViewRegistrations();

		final List< ViewSetup > viewSetupsOrdered = spimData.getSequenceDescription().getViewSetupsOrdered();

		final List< BdvStackSource< ? > > show = BdvFunctions.show( spimData );

		int a = 1;


	}
}
