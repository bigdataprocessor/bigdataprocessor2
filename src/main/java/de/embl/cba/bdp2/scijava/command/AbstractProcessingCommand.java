package de.embl.cba.bdp2.scijava.command;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.service.BdvService;
import de.embl.cba.bdp2.service.ImageService;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;

public abstract class AbstractProcessingCommand< R extends RealType< R > & NativeType< R > > implements Command
{
    public static final String SHOW_IN_NEW_VIEWER = "Show in new viewer";
    public static final String REPLACE_IN_VIEWER = "Replace input image";
    public static final String DO_NOT_SHOW = "Do not show";

    @Parameter(label = "Input image", persist = true)
    protected Image inputImage = ImageService.nameToImage.values().iterator().next();
    public static final String INPUT_IMAGE_PARAMETER = "inputImage";

    @Parameter(label = "Output image name")
    protected String outputImageName = ImageService.nameToImage.keySet().iterator().next() + "-binned";
    public static final String OUTPUT_IMAGE_NAME_PARAMETER = "outputImageName";

    @Parameter(label = "Output image handling", choices = {
            REPLACE_IN_VIEWER,
            SHOW_IN_NEW_VIEWER,
            DO_NOT_SHOW })
    protected String outputModality;
    public static final String OUTPUT_MODALITY_PARAMETER = "outputModality";

    protected Image< R > outputImage;

    protected void handleOutputImage( boolean autoContrast, boolean keepViewerTransform )
    {
        outputImage.setName( outputImageName );
        ImageService.nameToImage.put( outputImageName, outputImage );

        if ( outputModality.equals( SHOW_IN_NEW_VIEWER ) )
        {
            new BdvImageViewer<>( outputImage, autoContrast );
        }
        else if ( outputModality.equals( REPLACE_IN_VIEWER ))
        {
            final BdvImageViewer viewer = BdvService.imageNameToBdv.get( inputImage.getName() );
            viewer.replaceImage( outputImage, autoContrast, keepViewerTransform );
        }
        else if ( outputModality.equals( DO_NOT_SHOW ))
        {
            // do nothing
        }
    }
}
