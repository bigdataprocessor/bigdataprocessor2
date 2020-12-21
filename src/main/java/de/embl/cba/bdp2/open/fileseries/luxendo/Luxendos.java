package de.embl.cba.bdp2.open.fileseries.luxendo;

import ch.systemsx.cisd.hdf5.IHDF5Reader;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.open.fileseries.FileInfosHelper;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Luxendos
{

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
			Logger.warn( "Could not read voxel size!");
			return null;
		}
	}
}
