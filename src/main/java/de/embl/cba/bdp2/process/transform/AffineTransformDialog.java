/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2023 EMBL
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
package de.embl.cba.bdp2.process.transform;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.record.ScriptRecorder;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.bdp2.viewer.ImageViewer;
import ij.gui.GenericDialog;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import static de.embl.cba.bdp2.process.transform.TransformConstants.LINEAR;
import static de.embl.cba.bdp2.process.transform.TransformConstants.NEAREST;

public class AffineTransformDialog< T extends RealType< T > & NativeType< T > >
{
	protected static String affineTransform = "1,0,0,0,0,1,0,0,0,0,1,0";
	protected static String interpolationMode = NEAREST;
	protected final ImageViewer< T > viewer;
	protected final Image< T > inputImage;
	protected Image< T > outputImage;

	public AffineTransformDialog( final ImageViewer< T > viewer )
	{
		this.viewer = viewer;
		this.inputImage = viewer.getImage();
	}

	public void show()
	{
		final GenericDialog genericDialog = new GenericDialog( AffineTransformCommand.COMMAND_NAME );
		genericDialog.addStringField( AffineTransformCommand.AFFINE_LABEL, affineTransform, 30 );
		genericDialog.addChoice( "Interpolation", new String[]{ NEAREST, LINEAR }, interpolationMode );

		genericDialog.showDialog();
		if ( genericDialog.wasCanceled() ) return;

		affineTransform = genericDialog.getNextString();
		interpolationMode = genericDialog.getNextChoice();
		transform( AffineTransformCommand.getAffineTransform3D( affineTransform ) );
	}

	protected void transform( AffineTransform3D affineTransform3D )
	{
		outputImage = BigDataProcessor2.transform( inputImage, affineTransform3D, Utils.getInterpolator( interpolationMode ) );
		outputImage.setName( inputImage.getName() + "-transformed" );
		BigDataProcessor2.showImage( outputImage, inputImage );
		recordMacro();
	}

	protected void recordMacro()
	{
		final ScriptRecorder recorder = new ScriptRecorder( AffineTransformCommand.COMMAND_FULL_NAME, inputImage, outputImage );
		recorder.addCommandParameter( AffineTransformCommand.AFFINE_STRING_PARAMETER, affineTransform );
		recorder.addCommandParameter( AffineTransformCommand.INTERPOLATION_PARAMETER, interpolationMode );

		recorder.setBDP2FunctionName( "transform" );
		recorder.addAPIFunctionPrequelComment( AffineTransformCommand.COMMAND_NAME );
		recorder.addAPIFunctionParameter( AffineTransformCommand.getAffineTransform3D( affineTransform ).getRowPackedCopy() );
		recorder.addAPIFunctionParameter( interpolationMode );

		recorder.record();
	}
}
