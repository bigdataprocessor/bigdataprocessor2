package de.embl.cba.bdp2.process.calibrate;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.log.Logger;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import ome.units.quantity.Length;
import ome.units.unit.Unit;

public abstract class CalibrationChecker< R extends RealType< R > & NativeType< R > >
{
	public static < R extends RealType< R > & NativeType< R > > void correctCalibrationViaDialogIfNecessary( Image< R > inputImage )
	{
		if ( ! checkVoxelDimension( inputImage.getVoxelDimensions() ) || ! checkVoxelUnit( inputImage.getVoxelUnit() ) )
		{
			Image< R > calibratedImage = null;
			while( calibratedImage == null )
			{
				calibratedImage = new CalibrationDialog<>( inputImage ).showDialog();
			}
			inputImage.setVoxelUnit( calibratedImage.getVoxelUnit() );
			inputImage.setVoxelDimensions( calibratedImage.getVoxelDimensions() );
		}
	}

	public static boolean checkVoxelDimension( double[] voxelSize )
	{
		if ( voxelSize == null )
		{
			return false;
		}

		for ( int d = 0; d < 3; d++ )
		{
			if ( Double.isNaN( voxelSize[ d ] ) || voxelSize[ d ] <= 0.0 )
			{
				return false;
			}
		}

		return true;
	}

	public static boolean checkImage( Image< ? > image )
	{
		if ( ! checkVoxelUnit( image.getVoxelUnit() ) )
			return false;

		if ( ! checkVoxelDimension( image.getVoxelDimensions() ) )
			return false;

		return true;
	}

	public static boolean checkVoxelUnit( Unit< Length > voxelUnit )
	{
		if ( voxelUnit == null )
		{
			return false;
		}

		return true;
	}
}
