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
package de.embl.cba.bdp2.track;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.BigDataProcessor2Menu;
import de.embl.cba.bdp2.dialog.DialogUtils;
import de.embl.cba.bdp2.process.AbstractImageProcessingCommand;
import de.embl.cba.bdp2.viewer.ImageViewer;
import de.embl.cba.bdv.utils.BdvUtils;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.io.File;

@Plugin(type = Command.class, name = ApplyTrackCommand.COMMAND_NAME, menuPath = DialogUtils.BIGDATAPROCESSOR2_COMMANDS_MENU_ROOT + AbstractImageProcessingCommand.COMMAND_PATH + ApplyTrackCommand.COMMAND_FULL_NAME )
public class ApplyTrackCommand< R extends RealType< R > & NativeType< R > > extends AbstractImageProcessingCommand< R >
{
    public static final String COMMAND_NAME = "Apply Track...";
    public static final String COMMAND_FULL_NAME =  BigDataProcessor2Menu.COMMAND_BDP2_PREFIX + COMMAND_NAME;

//    @Parameter(label = "Track")
//    Track track;

    @Parameter(label = "Track")
    File file;
    public static final String TRACK_FILE_PARAMETER = "file";

    @Parameter(label = "Center image on track positions")
    Boolean centerImage = false;
    public static final String CENTER_IMAGE_PARAMETER = "centerImage";

    public void run()
    {
        process();
        showOutputImage();
    }

    private void process()
    {
        outputImage = BigDataProcessor2.applyTrack( file, inputImage, centerImage );
    }

    public void showOutputImage()
    {
        final ImageViewer viewer = handleOutputImage( false, false );
        if ( centerImage )
            BdvUtils.moveToPosition( viewer.getBdvHandle(), new double[]{ 0, 0, 0 }, 0 , 0);
    }

    @Override
    public void showDialog( ImageViewer< R > imageViewer )
    {
        new ApplyTrackDialog<>( imageViewer ).showDialog();
    }
}
