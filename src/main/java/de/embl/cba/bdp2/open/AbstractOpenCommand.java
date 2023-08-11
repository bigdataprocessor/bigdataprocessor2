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
package de.embl.cba.bdp2.open;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.process.calibrate.CalibrationChecker;
import de.embl.cba.bdp2.scijava.Services;
import de.embl.cba.bdp2.service.ImageService;
import de.embl.cba.bdp2.viewer.ImageViewer;
import de.embl.cba.bdp2.viewer.ViewingModalities;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.ui.UIService;
import spim.fiji.plugin.Specify_Calibration;

public abstract class AbstractOpenCommand< R extends RealType< R > & NativeType< R > > implements Command, OpenCommand
{
    public static final String COMMAND_OPEN_PATH = "Commands>Open>";

	// TODO: this is not concurrency save
    public static ImageViewer parentViewer = null;

    @Parameter
    protected UIService uiService;

    @Parameter
    protected Context context;

    @Parameter(label = "Output image handling", choices = {
            ViewingModalities.SHOW_IN_CURRENT_VIEWER,
            ViewingModalities.SHOW_IN_NEW_VIEWER,
            ViewingModalities.DO_NOT_SHOW })
    protected String viewingModality = ViewingModalities.SHOW_IN_NEW_VIEWER; // Note: this must be the same variable name as in AbstractProcessingCommand

    @Parameter(label = "Enable arbitrary plane slicing", required = false)
    protected boolean enableArbitraryPlaneSlicing = false;
    public static String ARBITRARY_PLANE_SLICING_PARAMETER = "enableArbitraryPlaneSlicing";

    protected boolean autoContrast = true;

    protected Image< R > outputImage;

    protected void handleOutputImage( boolean autoContrast, boolean keepViewerTransform )
    {
        Services.setUiService( uiService );
        Services.setContext( context );

        ImageService.imageNameToImage.put( outputImage.getName(), outputImage );

        if ( viewingModality.equals( ViewingModalities.DO_NOT_SHOW ) )
        {
            return;
        }
        else
        {
            if ( ! CalibrationChecker.checkVoxelDimension( outputImage.getVoxelDimensions() ) )
                outputImage.setVoxelDimensions( new double[]{1,1,1} );

            if ( ! CalibrationChecker.checkVoxelUnit( outputImage.getVoxelUnit() ) )
                outputImage.setVoxelUnit( "unknown" );

            // TODO: the UI pop up below creates an issue when run in a macro!
            //CalibrationChecker.correctCalibrationViaDialogIfNecessary( outputImage );
            showInViewer( autoContrast, keepViewerTransform );
        }
    }

    protected ImageViewer showInViewer( boolean autoContrast, boolean keepViewerTransform )
    {
        if ( viewingModality.equals( ViewingModalities.SHOW_IN_NEW_VIEWER ) || parentViewer == null )
        {
            return BigDataProcessor2.showImage( outputImage, autoContrast, enableArbitraryPlaneSlicing );
        }
        else if ( viewingModality.equals( ViewingModalities.SHOW_IN_CURRENT_VIEWER ) )
        {
            parentViewer.replaceImage( outputImage, autoContrast, keepViewerTransform );
            return parentViewer;
        }
        else
        {
            return null;
        }
    }
}
