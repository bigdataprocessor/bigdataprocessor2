package de.embl.cba.bdp2.calibrate;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.macro.MacroRecorder;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import ij.IJ;
import ij.gui.GenericDialog;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import java.util.Arrays;

import static de.embl.cba.bdp2.calibrate.CalibrationUtils.fixVoxelSizeAndUnit;

public class CalibrationDialog< R extends RealType< R > & NativeType< R > >
{
	private final Image< R > inputImage;
	private BdvImageViewer< R > viewer;
	private Image< R > outputImage;

	public CalibrationDialog( Image< R > inputImage )
	{
		this.inputImage = inputImage;
	}

	public CalibrationDialog( final BdvImageViewer< R > viewer )
	{
		this.inputImage = viewer.getImage();
		this.viewer = viewer;
	}

	public Image< R > showDialog()
	{
		final double[] voxelSize = inputImage.getVoxelSize();
		String voxelUnit = inputImage.getVoxelUnit();
		voxelUnit = fixVoxelSizeAndUnit( voxelSize, voxelUnit );

		final GenericDialog genericDialog = new GenericDialog( "Calibration" );
		genericDialog.addStringField( "Unit", voxelUnit, 12 );
		genericDialog.addNumericField( "Voxel size X", voxelSize[ 0 ], 3, 12, "" );
		genericDialog.addNumericField( "Voxel size Y", voxelSize[ 1 ], 3, 12, "" );
		genericDialog.addNumericField( "Voxel size Z", voxelSize[ 2 ], 3, 12, "" );

		genericDialog.showDialog();
		if ( genericDialog.wasCanceled() ) return null;

		outputImage = inputImage;

		outputImage.setVoxelUnit( genericDialog.getNextString() );
		voxelSize[ 0 ] = genericDialog.getNextNumber();
		voxelSize[ 1 ] = genericDialog.getNextNumber();
		voxelSize[ 2 ] = genericDialog.getNextNumber();

		if ( ! Utils.checkVoxelSize( voxelSize ) )
		{
			IJ.showMessage( "Incorrect voxel size (see Log window).\nPlease set again." );
			return null;
		}

		outputImage.setVoxelSize( voxelSize );
		Logger.info( "Image voxel unit: " + outputImage.getVoxelUnit() );
		Logger.info( "Image voxel size: " + Arrays.toString( outputImage.getVoxelSize() ) );

		if ( viewer != null )
			viewer.replaceImage( outputImage, false, false );

		recordMacro();
		return outputImage;
	}

	protected void recordMacro()
	{
		final MacroRecorder recorder = new MacroRecorder( CalibrateCommand.COMMAND_FULL_NAME, inputImage, outputImage );

		final double[] voxelSize = outputImage.getVoxelSize();
		recorder.addOption( "unit", outputImage.getVoxelUnit() );
		recorder.addOption( CalibrateCommand.VOXEL_SIZE_X_PARAMETER, voxelSize[ 0 ] );
		recorder.addOption( CalibrateCommand.VOXEL_SIZE_Y_PARAMETER, voxelSize[ 1 ] );
		recorder.addOption( CalibrateCommand.VOXEL_SIZE_Z_PARAMETER, voxelSize[ 2 ] );

		recorder.record();
	}
}
