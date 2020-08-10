package de.embl.cba.bdp2.align;

import de.embl.cba.bdp2.process.AbstractProcessingCommand;
import de.embl.cba.bdp2.service.ImageService;
import de.embl.cba.bdp2.utils.Utils;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.List;

@Plugin(type = Command.class, menuPath = de.embl.cba.bdp2.dialog.Utils.BIGDATAPROCESSOR2_COMMANDS_MENU_ROOT + AbstractProcessingCommand.COMMAND_PROCESS_PATH + AlignChannelsCommand.COMMAND_FULL_NAME )
public class AlignChannelsCommand< R extends RealType< R > & NativeType< R > > extends AbstractProcessingCommand< R >
{

    public static final String COMMAND_NAME = "Align Channels...";
    public static final String COMMAND_FULL_NAME = Utils.COMMAND_BDP2_PREFIX + COMMAND_NAME;

    @Parameter(label = "Shifts X_C0,Y_C0,Z_C0;X_C1,Y_C1,Z_C1;... [pixels]")
    String shifts = "0,0,0;0,0,0";

    @Override
    public void run()
    {
        process();
        handleOutputImage( false, true );
        ImageService.imageNameToImage.put( outputImage.getName(), outputImage );
    }

    private void process()
    {
        final List< long[] > longs = Utils.delimitedStringToLongs( shifts, ";" );

        final ChannelShifter< R > shifter = new ChannelShifter< R >( inputImage.getRai() );
        outputImage = inputImage.newImage( shifter.getShiftedRai( longs ) );
        outputImage.setName( outputImageName );
    }

}
