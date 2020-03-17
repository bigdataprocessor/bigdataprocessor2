package de.embl.cba.bdp2.scijava.command;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.service.BdvService;
import de.embl.cba.bdp2.service.ImageService;
import de.embl.cba.bdp2.ui.BigDataProcessor2;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.*;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Command.class, menuPath = "Plugins>BigDataProcessor2>Process>BDP2_Bin...")
public class BinCommand< R extends RealType< R > & NativeType< R > >
        implements Command
{
    @Parameter(label = "Input Image", persist = true)
    Image inputImage = ImageService.nameToImage.values().iterator().next();

    @Parameter(label = "Bin width X&Y [pixels]", min = "1")
    int binWidthXYPixels = 1;

    @Parameter(label = "Bin width Z [pixels]", min = "1")
    int binWidthZPixels = 1;

    @Parameter(label = "Output image name")
    String outputImageName = ImageService.nameToImage.keySet().iterator().next() + "-binned";

    @Parameter(label = "Open in new viewer")
    boolean openInNewViewer = false;

    private Image< R > outputImage;

    @Override
    public void run()
    {
        process();
        show();
        ImageService.nameToImage.put( outputImage.getName(), outputImage );
    }

    private void process()
    {
        outputImage = BigDataProcessor2.bin( inputImage, new long[]{ binWidthXYPixels, binWidthXYPixels, binWidthZPixels, 1, 1 } );
        outputImage.setName( outputImageName );
    }

    private void show()
    {
        if ( openInNewViewer )
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
