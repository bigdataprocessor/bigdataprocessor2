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
package de.embl.cba.bdp2.process;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.service.ImageViewerService;
import de.embl.cba.bdp2.service.ImageService;
import de.embl.cba.bdp2.viewer.ImageViewer;
import de.embl.cba.bdp2.viewer.ViewingModalities;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;

public abstract class AbstractImageProcessingCommand< R extends RealType< R > & NativeType< R > > implements Command
{
    public static final String COMMAND_PATH = "Commands>Process>";

    /*
     * This parameter is preprocessed by the
     * SwingImageWidget class
     */
    @Parameter(label = "Input image")
    protected Image inputImage;
    public static final String INPUT_IMAGE_PARAMETER = "inputImage";

    @Parameter(label = "Output image name")
    protected String outputImageName;
    public static final String OUTPUT_IMAGE_NAME_PARAMETER = "outputImageName";

    @Parameter(label = "Output image handling", choices = {
            ViewingModalities.SHOW_IN_CURRENT_VIEWER,
            ViewingModalities.SHOW_IN_NEW_VIEWER,
            ViewingModalities.DO_NOT_SHOW })
    protected String viewingModality = ViewingModalities.SHOW_IN_NEW_VIEWER;
    public static final String VIEWING_MODALITY_PARAMETER = "viewingModality";

    protected Image< R > outputImage;

    protected ImageViewer handleOutputImage( boolean autoContrast, boolean keepViewerTransform )
    {
        outputImage.setName( outputImageName );
        ImageService.imageNameToImage.put( outputImage.getName(), outputImage );

        if ( viewingModality.equals( ViewingModalities.SHOW_IN_NEW_VIEWER ) )
        {
            if ( autoContrast )
                return BigDataProcessor2.showImage( outputImage, true );
            else
                return BigDataProcessor2.showImage( outputImage, inputImage );
        }
        else if ( viewingModality.equals( ViewingModalities.SHOW_IN_CURRENT_VIEWER ))
        {
            final ImageViewer viewer = ImageViewerService.imageNameToBdvImageViewer.get( inputImage.getName() );
            viewer.replaceImage( outputImage, autoContrast, keepViewerTransform );
            return viewer;
        }
        else if ( viewingModality.equals( ViewingModalities.DO_NOT_SHOW ))
        {
            // do nothing
            return null;
        }
        else
        {
            throw new RuntimeException( "Unsupported viewing modality: " + viewingModality );
        }
    }

    /*
     * This is the method that is called from the BigDataProcessor2 menu.
     * It should provide interactive functionality to process the
     * image that is currently shown in the imageViewer.
     *
     * In principle, the SciJava user interface that can be auto-generated from the
     * implementation of the AbstractImageProcessingCommand (using commandService.run()),
     * could be shown here. However, in practice this often is not flexible enough
     * and thus creating an own user interface instead may be necessary.
     *
     * When implementing below dialog, please ensure that a macro command will be recorded
     * that runs the AbstractImageProcessingCommand implementation.
     *
     * It is recommended to use AbstractProcessingDialog for creating the dialog.
     * Example:
     * - MultiChannelUnsignedByteTypeConverterDialog
     *
     * @param imageViewer
     */
    public abstract void showDialog( ImageViewer< R > imageViewer );

}
