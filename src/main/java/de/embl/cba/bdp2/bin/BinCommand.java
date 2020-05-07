package de.embl.cba.bdp2.bin;

import de.embl.cba.bdp2.process.AbstractProcessingCommand;
import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.utils.Utils;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Command.class, menuPath = "Plugins>BigDataProcessor2>" + AbstractProcessingCommand.COMMAND_PROCESS_PATH + BinCommand.COMMAND_FULL_NAME )
public class BinCommand< R extends RealType< R > & NativeType< R > > extends AbstractProcessingCommand implements Command
{
    public static final String COMMAND_NAME = "Bin...";
    public static final String COMMAND_FULL_NAME = Utils.COMMAND_BDP_PREFIX + COMMAND_NAME;

    @Parameter(label = "Bin width X [pixels]", min = "1")
    int binWidthXPixels = 1;

    @Parameter(label = "Bin width Y [pixels]", min = "1")
    int binWidthYPixels = 1;

    @Parameter(label = "Bin width Z [pixels]", min = "1")
    int binWidthZPixels = 1;

    @Override
    public void run()
    {
        process();
        handleOutputImage( true, true );
    }

    private void process()
    {
        outputImage = BigDataProcessor2.bin( inputImage, new long[]{ binWidthXPixels, binWidthYPixels, binWidthZPixels, 1, 1 } );
    }

}
