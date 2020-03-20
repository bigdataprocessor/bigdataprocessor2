package de.embl.cba.bdp2.calibrate;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.record.MacroRecorder;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import ij.gui.GenericDialog;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import java.util.Arrays;

import static de.embl.cba.bdp2.calibrate.CalibrationUtils.fixVoxelSpacingAndUnit;

public class CalibrationDialog< R extends RealType< R > & NativeType< R > >
{
	private final Image< R > inputImage;
	private final BdvImageViewer< R > viewer;
	private Image< R > outputImage;

	public CalibrationDialog( final BdvImageViewer< R > viewer )
	{
		this.inputImage = viewer.getImage();
		this.viewer = viewer;
		showGenericDialog();
	}

	private void showGenericDialog()
	{
		final double[] voxelSpacing = inputImage.getVoxelSpacing();
		String voxelUnit = inputImage.getVoxelUnit();
		voxelUnit = fixVoxelSpacingAndUnit( voxelSpacing, voxelUnit );

		final GenericDialog genericDialog = new GenericDialog( "Calibration" );
		genericDialog.addStringField( "Unit", voxelUnit, 12 );
		genericDialog.addNumericField( "Voxel spacing X", voxelSpacing[ 0 ], 3, 12, "" );
		genericDialog.addNumericField( "Voxel spacing Y", voxelSpacing[ 1 ], 3, 12, "" );
		genericDialog.addNumericField( "Voxel spacing Z", voxelSpacing[ 2 ], 3, 12, "" );

		genericDialog.showDialog();
		if ( genericDialog.wasCanceled() ) return;

		outputImage = inputImage;

		outputImage.setVoxelUnit( genericDialog.getNextString() );
		voxelSpacing[ 0 ] = genericDialog.getNextNumber();
		voxelSpacing[ 1 ] = genericDialog.getNextNumber();
		voxelSpacing[ 2 ] = genericDialog.getNextNumber();

		Logger.info( "Image voxel unit: " + outputImage.getVoxelUnit() );
		Logger.info( "Image voxel size: " + Arrays.toString( outputImage.getVoxelSpacing() ) );

		viewer.replaceImage( outputImage, false, false );

		recordMacro();
	}

	protected void recordMacro()
	{
		final MacroRecorder recorder = new MacroRecorder( "BDP2_Calibrate...", inputImage, outputImage, false );

		final double[] voxelSpacing = outputImage.getVoxelSpacing();
		recorder.addOption( "unit", outputImage.getVoxelUnit() );
		recorder.addOption( "voxelSpacingX", voxelSpacing[ 0 ] );
		recorder.addOption( "voxelSpacingY", voxelSpacing[ 1 ] );
		recorder.addOption( "voxelSpacingZ", voxelSpacing[ 2 ] );

		recorder.record();
	}
}
