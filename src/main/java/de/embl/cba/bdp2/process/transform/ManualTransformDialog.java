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
package de.embl.cba.bdp2.process.transform;

import de.embl.cba.bdp2.viewer.ImageViewer;
import ij.IJ;
import ij.gui.GenericDialog;
import ij.gui.NonBlockingGenericDialog;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import static de.embl.cba.bdp2.process.transform.TransformConstants.LINEAR;
import static de.embl.cba.bdp2.process.transform.TransformConstants.NEAREST;

public class ManualTransformDialog< T extends RealType< T > & NativeType< T > > extends AffineTransformDialog< T >
{
	public ManualTransformDialog( final ImageViewer< T > viewer )
	{
		super( viewer );
	}

	@Override
	public void show()
	{
		final GenericDialog genericDialog = new NonBlockingGenericDialog( ManualTransformCommand.COMMAND_NAME );
		genericDialog.addMessage( "1. Select the BDV window and press T to start the transform\n" +
				"2. Transform the image\n" +
				"  - Press X,Y, or Z to change the axis of rotation\n" +
				"  - Use the LEFT and RIGHT arrow keys to rotate your image\n" +
				"  - DON'T use the UP and DOWN arrow keys as this will\n" +
				"    re-scale your image (not properly supported)\n" +
				"3. Press T again to fix the transformation\n" +
				"4. Click [ OK ] to apply the transformation\n" +
				"   \n" +
				"Note: For multi-channel images it will only show\n" +
				"the manual interactive transform for the first channel.\n" +
				"However, upon clicking [OK] it will apply it to all channels!\n" +
				"To see all channels interactively transformed,\n" +
				"please select the BDV window,\n" +
				"press P and configure the Group view mode." );
		genericDialog.addChoice( "Interpolation", new String[]{ NEAREST, LINEAR }, interpolationMode );

		genericDialog.showDialog();
		if ( genericDialog.wasCanceled() ) return;

		interpolationMode = genericDialog.getNextChoice();
		final AffineTransform3D sourceTransform = viewer.getSourceTransform();
		if ( ! sourceTransform.isIdentity() )
		{
			transform( sourceTransform );
		}
		else
		{
			IJ.showMessage("No manual transform could be found!\n" +
					"Maybe you forget to press T again to fix the transform?\n" +
					"If so, please note that BDV now still is in manual transform mode...\n" +
					"Thus, to apply the current transform please select again [ Process > Transform > Manual Transform... ]\n" +
					"and just press T once and click [ OK ].");
		}
	}
}
