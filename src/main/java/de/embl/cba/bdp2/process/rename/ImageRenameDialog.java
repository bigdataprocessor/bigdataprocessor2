/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2022 EMBL
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
package de.embl.cba.bdp2.process.rename;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.record.ScriptRecorder;
import de.embl.cba.bdp2.viewer.ImageViewer;
import ij.gui.GenericDialog;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import static de.embl.cba.bdp2.process.rename.ImageRenameCommand.CHANNEL_NAMES_PARAMETER;

public class ImageRenameDialog < R extends RealType< R > & NativeType< R > >
{
	private final Image< R > inputImage;
	private final ImageViewer< R > viewer;
	private String[] channelNames;
	private Image< R > outputImage;

	public ImageRenameDialog( final ImageViewer< R > viewer  )
	{
		this.viewer = viewer;
		this.inputImage = viewer.getImage();
	}

	protected void recordMacro()
	{
		final ScriptRecorder recorder = new ScriptRecorder( ImageRenameCommand.COMMAND_FULL_NAME, inputImage, outputImage );
		recorder.addCommandParameter( CHANNEL_NAMES_PARAMETER, String.join( ",", channelNames ) );

		// Image< R > rename( Image< R > image, String name )
		recorder.setBDP2FunctionName( "rename" );
		recorder.addAPIFunctionPrequelComment(  ImageRenameCommand.COMMAND_NAME );
		recorder.addAPIFunctionParameter( ScriptRecorder.quote( inputImage.getName() ) );
		recorder.addAPIFunctionParameter( inputImage.getChannelNames() );
		recorder.record();
	}

	protected void showDialog()
	{
		final GenericDialog gd = new GenericDialog( "Rename Image" );
		gd.setResizable( true );

		final int length = inputImage.getName().length();
		gd.addStringField( "Image", inputImage.getName(), 2 * length );

		channelNames = inputImage.getChannelNames();
		for ( int c = 0; c < channelNames.length; c++ )
		{
			gd.addStringField( "Channel " + c, channelNames[ c ], 2 * length  );
		}

		gd.showDialog();

		if( gd.wasCanceled() ) return;

		String name = gd.getNextString();

		for ( int c = 0; c < channelNames.length; c++ )
		{
			channelNames[ c ] = gd.getNextString();
		}

		outputImage = BigDataProcessor2.rename( inputImage, name, channelNames );

		viewer.replaceImage( outputImage, false, true );

		recordMacro();
	}
}
