package de.embl.cba.bdp2.process.calibrate;

import ch.epfl.biop.bdv.bioformats.BioFormatsMetaDataHelper;
import de.embl.cba.bdp2.BigDataProcessor2;
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

import java.util.Arrays;

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


		String unitChoice = genericDialog.getNextChoice();
		Unit< Length > unit = BioFormatsMetaDataHelper.getUnitFromString( unitChoice );
		voxelSize[ 0 ] = genericDialog.getNextNumber();
		voxelSize[ 1 ] = genericDialog.getNextNumber();
		voxelSize[ 2 ] = genericDialog.getNextNumber();

		if ( ! Utils.checkVoxelSize( voxelSize ) )
		{
			IJ.showMessage( "Incorrect voxel size (see Log window).\nPlease set again." );
			return null;
		}

		outputImage = BigDataProcessor2.calibrate( inputImage, voxelSize, unit );

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
		recorder.addCommandParameter( "unit", outputImage.getVoxelUnit() );
		recorder.addCommandParameter( CalibrateCommand.VOXEL_SIZE_X_PARAMETER, voxelSize[ 0 ] );
		recorder.addCommandParameter( CalibrateCommand.VOXEL_SIZE_Y_PARAMETER, voxelSize[ 1 ] );
		recorder.addCommandParameter( CalibrateCommand.VOXEL_SIZE_Z_PARAMETER, voxelSize[ 2 ] );

		// public static void calibrate( Image image, double[] doubles, String voxelUnit )
		recorder.setAPIFunction( "calibrate" );
		recorder.addAPIFunctionParameter( voxelSize );
		recorder.addAPIFunctionParameter( recorder.quote( outputImage.getVoxelUnit().getSymbol() ) );

		recorder.record();
	}
}
