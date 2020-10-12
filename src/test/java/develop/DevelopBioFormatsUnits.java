package develop;

import ch.epfl.biop.bdv.bioformats.BioFormatsMetaDataHelper;
import ome.units.unit.Unit;

public class DevelopBioFormatsUnits
{
	public static void main( String[] args )
	{
		Unit micrometer = BioFormatsMetaDataHelper.getUnitFromString( "micrometer" );
		System.out.println(micrometer.getSymbol());
	}
}
