package de.embl.cba.bdp2.process.calibrate;

import ch.epfl.biop.bdv.bioformats.BioFormatsMetaDataHelper;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.macro.MacroRecorder;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.bdp2.viewer.ImageViewer;
import ij.IJ;
import ij.gui.GenericDialog;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import ome.units.UNITS;
import ome.units.quantity.Length;
import ome.units.unit.Unit;

import java.util.ArrayList;
import java.util.Arrays;

import static de.embl.cba.bdp2.process.calibrate.CalibrationUtils.fixVoxelSizeAndUnit;

// TODO: Can one make it a child of AbstractProcessingDialog?
public class CalibrationDialog< R extends RealType< R > & NativeType< R > >
{
	private final Image< R > inputImage;
	private ImageViewer< R > viewer;
	private Image< R > outputImage;

	public CalibrationDialog( Image< R > inputImage )
	{
		this.inputImage = inputImage;
	}

	public CalibrationDialog( final ImageViewer< R > viewer )
	{
		this.inputImage = viewer.getImage();
		this.viewer = viewer;
	}

	public Image< R > showDialog()
	{
		String[] voxelUnitSymbols = { UNITS.MICROMETRE.getSymbol(), UNITS.NANOMETRE.getSymbol() };

		final double[] voxelSize = inputImage.getVoxelSize();
		Unit< Length > voxelUnit = inputImage.getVoxelUnit();

		if ( voxelUnit == null )
			voxelUnit = UNITS.MICROMETER;

		if ( ! Arrays.asList( voxelUnitSymbols ).contains( voxelUnit.getSymbol() ) )
			voxelUnit = UNITS.MICROMETER;

		final GenericDialog genericDialog = new GenericDialog( "Calibration" );
		genericDialog.addChoice( "Unit", voxelUnitSymbols, voxelUnit.getSymbol() );
		genericDialog.addNumericField( "Voxel size X", voxelSize[ 0 ], 3, 12, "" );
		genericDialog.addNumericField( "Voxel size Y", voxelSize[ 1 ], 3, 12, "" );
		genericDialog.addNumericField( "Voxel size Z", voxelSize[ 2 ], 3, 12, "" );

		genericDialog.showDialog();
		if ( genericDialog.wasCanceled() ) return null;

		outputImage = inputImage;

		String unitChoice = genericDialog.getNextChoice();
		Unit unitFromString = BioFormatsMetaDataHelper.getUnitFromString( unitChoice );
		outputImage.setVoxelUnit( unitFromString );
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
