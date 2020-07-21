package de.embl.cba.bdp2.process;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.open.ui.AbstractOpenCommand;
import de.embl.cba.bdp2.service.BdvService;
import de.embl.cba.bdp2.service.ImageService;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;

public abstract class AbstractProcessingCommand< R extends RealType< R > & NativeType< R > > implements Command
{
    public static final String COMMAND_PROCESS_PATH = "Commands>Process>";

    @Parameter(label = "Input image")
    protected Image inputImage = ImageService.imageNameToImage.values().iterator().next();
    public static final String INPUT_IMAGE_PARAMETER = "inputImage";

    @Parameter(label = "Output image name")
    protected String outputImageName = ImageService.imageNameToImage.keySet().iterator().next();
    public static final String OUTPUT_IMAGE_NAME_PARAMETER = "outputImageName";

    @Parameter(label = "Output image handling", choices = {
            AbstractOpenCommand.SHOW_IN_CURRENT_VIEWER,
            AbstractOpenCommand.SHOW_IN_NEW_VIEWER,
            AbstractOpenCommand.DO_NOT_SHOW })

    protected String viewingModality;
    public static final String VIEWING_MODALITY_PARAMETER = "viewingModality";

    protected Image< R > outputImage;

    protected BdvImageViewer handleOutputImage( boolean autoContrast, boolean keepViewerTransform )
    {
        outputImage.setName( outputImageName );
        ImageService.imageNameToImage.put( outputImageName, outputImage );

        if ( viewingModality.equals( AbstractOpenCommand.SHOW_IN_NEW_VIEWER ) )
        {
            if ( autoContrast )
                return BigDataProcessor2.showImage( outputImage, true );
            else
                return BigDataProcessor2.showImageInheritingDisplaySettings( outputImage, inputImage );

        }
        else if ( viewingModality.equals( AbstractOpenCommand.SHOW_IN_CURRENT_VIEWER ))
        {
            final BdvImageViewer viewer = BdvService.imageNameToBdvImageViewer.get( inputImage.getName() );
            viewer.replaceImage( outputImage, autoContrast, keepViewerTransform );
            return viewer;
        }
        else if ( viewingModality.equals( AbstractOpenCommand.DO_NOT_SHOW ))
        {
            // do nothing
            return null;
        }
        else
        {
            throw new RuntimeException( "Unsupported viewing modality: " + viewingModality );
        }
    }

}
