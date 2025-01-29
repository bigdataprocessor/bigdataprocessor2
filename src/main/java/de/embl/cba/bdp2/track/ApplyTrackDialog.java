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
package de.embl.cba.bdp2.track;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.record.ScriptRecorder;
import de.embl.cba.bdp2.viewer.ImageViewer;
import de.embl.cba.bdv.utils.BdvUtils;
import fiji.util.gui.GenericDialogPlus;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import java.io.File;

public class ApplyTrackDialog< R extends RealType< R > & NativeType< R > >
{
	private final Image< R > inputImage;
	private Image< R > outputImage;
	private static File trackFile = new File( "" );
	private static boolean centerImage = false;

	public ApplyTrackDialog( final ImageViewer< R > viewer )
	{
		this.inputImage = viewer.getImage();
	}

	public void showDialog()
	{
		final GenericDialogPlus genericDialog = new GenericDialogPlus( "Apply Track" );
		genericDialog.addFileField( "Track", trackFile.getAbsolutePath(), 50 );
		genericDialog.addCheckbox("Center image on track positions", centerImage );

		genericDialog.showDialog();
		if ( genericDialog.wasCanceled() ) return;

		trackFile = new File ( genericDialog.getNextString() );
		centerImage = genericDialog.getNextBoolean();
		outputImage = BigDataProcessor2.applyTrack( trackFile, inputImage, centerImage );

		final ImageViewer viewer = BigDataProcessor2.showImage( outputImage );

		if ( centerImage )
			BdvUtils.moveToPosition( viewer.getBdvHandle(), new double[]{ 0, 0, 0 }, 0 , 0);

		recordMacro();
	}

	protected void recordMacro()
	{
		final ScriptRecorder recorder = new ScriptRecorder( ApplyTrackCommand.COMMAND_FULL_NAME, inputImage, outputImage );

		recorder.addCommandParameter( ApplyTrackCommand.TRACK_FILE_PARAMETER, trackFile );
		recorder.addCommandParameter( ApplyTrackCommand.CENTER_IMAGE_PARAMETER, centerImage);

		recorder.record();
	}
}
