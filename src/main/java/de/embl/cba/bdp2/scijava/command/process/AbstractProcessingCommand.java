package de.embl.cba.bdp2.scijava.command.process;

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

    protected void show()
    {
        if ( newViewer )
        {
            new BdvImageViewer<>( outputImage, true );
        }
        else
        {
            final BdvImageViewer viewer = BdvService.imageNameToBdv.get( inputImage.getName() );
            viewer.replaceImage( outputImage, true );
        }
    }

}
