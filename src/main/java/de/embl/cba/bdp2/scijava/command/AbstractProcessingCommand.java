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
    @Parameter(label = "Input image name", persist = true)
    protected Image inputImage = ImageService.nameToImage.values().iterator().next();

    @Parameter(label = "Output image name")
    protected String outputImageName = ImageService.nameToImage.keySet().iterator().next() + "-binned";

    @Parameter(label = "Show output image in new viewer")
    protected boolean newViewer = false;

    protected Image< R > outputImage;

    protected void showOutputImage( boolean autoContrast, boolean keepViewerTransform )
    {
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
