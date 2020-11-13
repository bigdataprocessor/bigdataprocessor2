package de.embl.cba.bdp2.process.align.channelshift;

import de.embl.cba.bdp2.BigDataProcessor2Menu;
import de.embl.cba.bdp2.dialog.DialogUtils;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.process.AbstractImageProcessingCommand;
import de.embl.cba.bdp2.service.ImageService;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.bdp2.viewer.ImageViewer;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.List;

@Plugin(type = AbstractImageProcessingCommand.class, name = AlignChannelsCommand.COMMAND_NAME, menuPath = DialogUtils.BIGDATAPROCESSOR2_COMMANDS_MENU_ROOT + AbstractImageProcessingCommand.COMMAND_PROCESS_PATH + AlignChannelsCommand.COMMAND_FULL_NAME )
public class AlignChannelsCommand< R extends RealType< R > & NativeType< R > > extends AbstractImageProcessingCommand< R >
{
    public static final String COMMAND_NAME = "Align Channels...";
    public static final String COMMAND_FULL_NAME = BigDataProcessor2Menu.COMMAND_BDP2_PREFIX + COMMAND_NAME;

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

        outputImage = new Image( inputImage );
        outputImage.setRai( shifter.getShiftedRai( longs ) );
        outputImage.setName( outputImageName );
    }

    @Override
    public void showDialog( ImageViewer< R > imageViewer )
    {
        new AlignChannelsDialog<>( imageViewer ).showDialog();
    }
}
