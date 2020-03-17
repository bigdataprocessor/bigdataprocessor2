package de.embl.cba.bdp2.scijava.command;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.service.BdvService;
import de.embl.cba.bdp2.service.ImageService;
import de.embl.cba.bdp2.ui.BigDataProcessor2;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Command.class, menuPath = "Plugins>BigDataProcessor2>Process>BDP2_Bin...")
public class BinCommand< R extends RealType< R > & NativeType< R > > extends AbstractProcessingCommand implements Command
{
    @Parameter(label = "Bin width X&Y [pixels]", min = "1")
    int binWidthXYPixels = 1;

    @Parameter(label = "Bin width Z [pixels]", min = "1")
    int binWidthZPixels = 1;

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

}
