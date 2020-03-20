package de.embl.cba.bdp2.scijava.command;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.service.BdvService;
import de.embl.cba.bdp2.service.ImageService;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.plugin.Parameter;

public class AbstractProcessingCommand< R extends RealType< R > & NativeType< R > >
{
    public static final String SHOW_IMAGE_IN_NEW_VIEWER = "Show image in new viewer";


    @Parameter(label = "Input image name", persist = true)
    protected Image inputImage = ImageService.nameToImage.values().iterator().next();

    @Parameter(label = "Output image name")
    protected String outputImageName = ImageService.nameToImage.keySet().iterator().next() + "-binned";

    @Parameter(label = "Output image handling", choices = {
            SHOW_IMAGE_IN_NEW_VIEWER,

            "Replace image in viewer",
            "Do not show"})
    protected String newViewer;

    {
        final String s = "Replace image in viewer";
        newViewer = false;
    }

    protected Image< R > outputImage;

    protected void handleOutputImage( boolean autoContrast, boolean keepViewerTransform )
    {
        outputImage.setName( outputImageName );
        ImageService.nameToImage.put( outputImageName, outputImage );
        if ( newViewer )
        {
            new BdvImageViewer<>( outputImage, autoContrast );
        }
        else
        {
            final BdvImageViewer viewer = BdvService.imageNameToBdv.get( inputImage.getName() );
            viewer.replaceImage( outputImage, autoContrast, keepViewerTransform );
        }
    }

}
