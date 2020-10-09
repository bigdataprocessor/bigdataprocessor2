package de.embl.cba.bdp2.open;

import ch.systemsx.cisd.hdf5.IHDF5Reader;
import de.embl.cba.bdp2.log.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.embl.cba.bdp2.open.NamingSchemes.LUXENDO_STACKINDEX_REGEXP;

public class Luxendos
{
	public static String extractStackIndex( String subFolderName )
	{
		Pattern pattern = Pattern.compile( LUXENDO_STACKINDEX_REGEXP );
		Matcher matcher = pattern.matcher( subFolderName );
		String stackIndex;
		if ( matcher.matches() )
		{
			stackIndex = matcher.group( "StackIndex" );
		}
		else
		{
			throw new RuntimeException( subFolderName + " does not match pattern " + LUXENDO_STACKINDEX_REGEXP );
		}
		return stackIndex;
	}

	public static double[] getVoxelSizeMicrometer( IHDF5Reader reader, String h5DataSetName )
	{
		if ( reader.hasAttribute( "/" + h5DataSetName, "element_size_um" ) )
		{
			final double[] voxelSizeZYX = reader.float64().getArrayAttr( "/" + h5DataSetName, "element_size_um");
			double[] voxelSizeXYZ = new double[ 3 ];

			// reorder the dimensions
			for ( int d = 0; d < 3; d++ )
				voxelSizeXYZ[ d ] = voxelSizeZYX[ 2 - d];

			return voxelSizeXYZ;
		}
		else
		{
			Logger.warning( "Could not read voxel size!");
			return null;
		}
	}
}
