package de.embl.cba.bdp2.scijava.command.process;

import de.embl.cba.bdp2.service.ImageService;
import de.embl.cba.bdp2.ui.BigDataProcessor2;
import net.imglib2.FinalInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Command.class, menuPath = "Plugins>BigDataProcessor2>Process>BDP2_Crop...")
public class CropCommand< R extends RealType< R > & NativeType< R > > extends AbstractProcessingCommand implements Command
{
    @Parameter(label = "Min X [pixel]", min = "0")
    long minX = 0;

    @Parameter(label = "Min Y [pixel]", min = "0")
    long minY = 0;

    @Parameter(label = "Min Z [pixel]", min = "0")
    long minZ = 0;

    @Parameter(label = "Min C [channel]", min = "0")
    long minC = 0;

    @Parameter(label = "Min T [frame]", min = "0")
    long minT = 0;
    
    @Parameter(label = "Max X [pixel]", min = "0")
    long maxX = 100;

    @Parameter(label = "Max Y [pixel]", min = "0")
    long maxY = 100;

    @Parameter(label = "Max Z [pixel]", min = "0")
    long maxZ = 100;

    @Parameter(label = "Max C [channel]", min = "0")
    long maxC = 0;

    @Parameter(label = "Max T [frame]", min = "0")
    long maxT = 0;

    @Override
    public void run()
    {
        process();
        show();
        ImageService.nameToImage.put( outputImage.getName(), outputImage );
    }

    private void process()
    {
        final FinalInterval minMax = Intervals.createMinMax(
                minX, minY, minZ, minC, minT,
                maxX, maxY, maxZ, maxC, maxT );

        outputImage = BigDataProcessor2.crop( inputImage, minMax);
        outputImage.setName( outputImageName );
    }

}