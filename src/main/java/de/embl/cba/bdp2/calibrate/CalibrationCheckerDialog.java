package de.embl.cba.bdp2.calibrate;

import de.embl.cba.bdp2.image.Image;
import ij.IJ;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class CalibrationCheckerDialog < R extends RealType< R > & NativeType< R > >
{
	public CalibrationCheckerDialog()
	{
	}

	public Image< R > checkAndCorrectCalibration( Image< R > inputImage )
	{
		if ( ! de.embl.cba.bdp2.utils.Utils.checkVoxelSize( inputImage.getVoxelSize() ) )
		{
			Image< R > calibratedImage = null;
			while( calibratedImage == null )
			{
				calibratedImage = new CalibrationDialog<>( inputImage ).showDialog();
			}
			return calibratedImage;
		}
		else
		{
			return inputImage;
		}
	}
}
