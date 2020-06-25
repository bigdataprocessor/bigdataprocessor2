package de.embl.cba.bdp2.convert;

import de.embl.cba.bdp2.BigDataProcessor2Command;
import de.embl.cba.bdp2.process.AbstractProcessingCommand;
import de.embl.cba.bdp2.utils.Utils;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Command.class, menuPath = BigDataProcessor2Command.BIGDATAPROCESSOR2_PLUGINS_MENU_ROOT + AbstractProcessingCommand.COMMAND_PROCESS_PATH + ConvertToUnsignedByteTypeCommand.COMMAND_FULL_NAME )
public class ConvertToUnsignedByteTypeCommand< R extends RealType< R > & NativeType< R > > extends AbstractProcessingCommand implements Command
{
    public static final String COMMAND_NAME = "Convert to 8-Bit...";
    public static final String COMMAND_FULL_NAME = Utils.COMMAND_BDP_PREFIX + COMMAND_NAME;
    @Parameter(label = "Map to 0", min = "0")
    int mapTo0 = 0;

    @Parameter(label = "Map to 255", min = "0")
    int mapTo255 = 65535;

    @Override
    public void run()
    {
        process();
        handleOutputImage( true, true );
    }

    private void process()
    {
        final UnsignedByteTypeConverter< R > converter = new UnsignedByteTypeConverter<>( inputImage, mapTo0, mapTo255 );

        outputImage = converter.getConvertedImage();
    }
}
