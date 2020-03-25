package de.embl.cba.bdp2.calibrate;

import de.embl.cba.bdp2.log.Logger;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class CalibrationUtils
{
	public static < R extends RealType< R > & NativeType< R > > String fixVoxelSpacingAndUnit( double[] voxelSpacing, String voxelUnit )
	{
		if ( voxelUnit.equals( "cm" ) )
		{
			voxelUnit = "micrometer";
			for ( int i = 0; i < voxelSpacing.length; i++ )
				voxelSpacing[ i ] *= 10000;

			Logger.log("Converted voxel spacing from cm to micrometer.");
		}
		return voxelUnit;
	}
}
