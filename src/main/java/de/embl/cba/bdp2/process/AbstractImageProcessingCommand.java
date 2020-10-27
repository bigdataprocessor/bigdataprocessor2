package de.embl.cba.bdp2.process;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.open.AbstractOpenFileSeriesCommand;
import de.embl.cba.bdp2.service.ImageViewerService;
import de.embl.cba.bdp2.service.ImageService;
import de.embl.cba.bdp2.viewer.ImageViewer;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;


/**
 * The AbstractImageProcessingCommand serves to process an input image
 * and generate an output image.
 *
 * One key benefit of implementing this functionality as a SciJava command is
 * to enable calling the processing step (headless) from an ImageJ macro.
 *
 * Example of an implementation of this class:
 * - MultiChannelUnsignedByteTypeConverterCommand
 *
 * @param <R>
 */
public abstract class AbstractImageProcessingCommand< R extends RealType< R > & NativeType< R > > implements Command
{
    public static final String COMMAND_PROCESS_PATH = "Commands>Process>";

    @Parameter(label = "Input image")
    protected Image inputImage; // = ImageService.imageNameToImage.values().iterator().next();
    public static final String INPUT_IMAGE_PARAMETER = "inputImage";

    @Parameter(label = "Output image name")
    protected String outputImageName; // = ImageService.imageNameToImage.keySet().iterator().next();
    public static final String OUTPUT_IMAGE_NAME_PARAMETER = "outputImageName";

    @Parameter(label = "Output image handling", choices = {
            AbstractOpenFileSeriesCommand.SHOW_IN_CURRENT_VIEWER,
            AbstractOpenFileSeriesCommand.SHOW_IN_NEW_VIEWER,
            AbstractOpenFileSeriesCommand.DO_NOT_SHOW })

    protected String viewingModality;
    public static final String VIEWING_MODALITY_PARAMETER = "viewingModality";

    protected Image< R > outputImage;

    protected ImageViewer handleOutputImage( boolean autoContrast, boolean keepViewerTransform )
    {
        outputImage.setName( outputImageName );
        ImageService.imageNameToImage.put( outputImageName, outputImage );

        if ( viewingModality.equals( AbstractOpenFileSeriesCommand.SHOW_IN_NEW_VIEWER ) )
        {
            if ( autoContrast )
                return BigDataProcessor2.showImage( outputImage, true );
            else
                return BigDataProcessor2.showImageInheritingDisplaySettings( outputImage, inputImage );

        }
        else if ( viewingModality.equals( AbstractOpenFileSeriesCommand.SHOW_IN_CURRENT_VIEWER ))
        {
            final ImageViewer viewer = ImageViewerService.imageNameToBdvImageViewer.get( inputImage.getName() );
            viewer.replaceImage( outputImage, autoContrast, keepViewerTransform );
            return viewer;
        }
        else if ( viewingModality.equals( AbstractOpenFileSeriesCommand.DO_NOT_SHOW ))
        {
            // do nothing
            return null;
        }
        else
        {
            throw new RuntimeException( "Unsupported viewing modality: " + viewingModality );
        }
    }

    /**
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
