/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2025 EMBL
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package de.embl.cba.bdp2.process.calibrate;

import ch.epfl.biop.bdv.img.legacy.bioformats.BioFormatsTools;
import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.record.ScriptRecorder;
import de.embl.cba.bdp2.viewer.ImageViewer;
import de.embl.cba.bdp2.viewer.ViewingModalities;
import ij.IJ;
import ij.gui.GenericDialog;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import ome.units.UNITS;
import ome.units.quantity.Length;
import ome.units.unit.Unit;

import java.util.Arrays;

// TODO: Can one make it a child of AbstractProcessingDialog?
//   Should this have an output image or operate in place?
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

		final double[] voxelDimension = inputImage.getVoxelDimensions();
		Unit< Length > voxelUnit = inputImage.getVoxelUnit();

		if ( voxelUnit == null )
			voxelUnit = UNITS.MICROMETER;

		if ( ! Arrays.asList( voxelUnitSymbols ).contains( voxelUnit.getSymbol() ) )
			voxelUnit = UNITS.MICROMETER;

		final GenericDialog genericDialog = new GenericDialog( "Calibration" );
		genericDialog.addChoice( "Unit", voxelUnitSymbols, voxelUnit.getSymbol() );
		genericDialog.addNumericField( "Voxel size X", voxelDimension[ 0 ], 3, 12, "" );
		genericDialog.addNumericField( "Voxel size Y", voxelDimension[ 1 ], 3, 12, "" );
		genericDialog.addNumericField( "Voxel size Z", voxelDimension[ 2 ], 3, 12, "" );

		genericDialog.showDialog();
		if ( genericDialog.wasCanceled() ) return null;

		String unitChoice = genericDialog.getNextChoice();
		Unit< Length > unit = BioFormatsTools.getUnitFromString( unitChoice );
		voxelDimension[ 0 ] = genericDialog.getNextNumber();
		voxelDimension[ 1 ] = genericDialog.getNextNumber();
		voxelDimension[ 2 ] = genericDialog.getNextNumber();

		if ( ! CalibrationChecker.checkVoxelDimension( voxelDimension ) || ! CalibrationChecker.checkVoxelUnit( unit ) )
		{
			IJ.showMessage( "Incorrect voxel size or unit (see Log window).\nPlease set again." );
			return null;
		}

		outputImage = BigDataProcessor2.setVoxelSize( inputImage, voxelDimension, unit );

		Logger.info( "\n# " + SetVoxelSizeCommand.COMMAND_NAME );
		Logger.info( "Image voxel unit: " + outputImage.getVoxelUnit().getSymbol() );
		Logger.info( "Image voxel size: " + Arrays.toString( outputImage.getVoxelDimensions() ) );

		if ( viewer != null )
			viewer.replaceImage( outputImage, false, false );

		recordMacro();

		return outputImage;
	}

	protected void recordMacro()
	{
		final ScriptRecorder recorder = new ScriptRecorder( SetVoxelSizeCommand.COMMAND_FULL_NAME, inputImage, outputImage, ViewingModalities.SHOW_IN_CURRENT_VIEWER );

		final double[] voxelSize = outputImage.getVoxelDimensions();
		recorder.addCommandParameter( "unit", outputImage.getVoxelUnit().getSymbol() );
		recorder.addCommandParameter( SetVoxelSizeCommand.VOXEL_SIZE_X_PARAMETER, voxelSize[ 0 ] );
		recorder.addCommandParameter( SetVoxelSizeCommand.VOXEL_SIZE_Y_PARAMETER, voxelSize[ 1 ] );
		recorder.addCommandParameter( SetVoxelSizeCommand.VOXEL_SIZE_Z_PARAMETER, voxelSize[ 2 ] );

		// public static void calibrate( Image image, double[] doubles, String voxelUnit )
		recorder.setBDP2FunctionName( "setVoxelSize" );
		recorder.addAPIFunctionPrequelComment( SetVoxelSizeCommand.COMMAND_NAME );
		recorder.addAPIFunctionParameter( voxelSize );
		recorder.addAPIFunctionParameter( recorder.quote( outputImage.getVoxelUnit().getSymbol() ) );

		recorder.record();
	}
}
