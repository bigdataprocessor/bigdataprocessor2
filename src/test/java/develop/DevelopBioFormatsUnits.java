package develop;

import ch.epfl.biop.bdv.bioformats.BioFormatsMetaDataHelper;
import de.embl.cba.bdp2.utils.Utils;
import ome.units.unit.Unit;

public class DevelopBioFormatsUnits
{
	public static void main( String[] args )
	{
		Unit micrometer = BioFormatsMetaDataHelper.getUnitFromString( "micrometer" );
		System.out.println( micrometer.getSymbol() );

		Unit unitFromString = Utils.getUnitFromString( micrometer.getSymbol() );
		System.out.println( unitFromString.getSymbol() );
	}
}
