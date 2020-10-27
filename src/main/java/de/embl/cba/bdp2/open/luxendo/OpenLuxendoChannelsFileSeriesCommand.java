package de.embl.cba.bdp2.open.luxendo;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.dialog.Utils;
import de.embl.cba.bdp2.open.NamingSchemes;
import de.embl.cba.bdp2.open.AbstractOpenFileSeriesCommand;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import javax.swing.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static de.embl.cba.bdp2.utils.Utils.COMMAND_BDP2_PREFIX;

@Plugin(type = Command.class, menuPath = Utils.BIGDATAPROCESSOR2_COMMANDS_MENU_ROOT + AbstractOpenFileSeriesCommand.COMMAND_OPEN_PATH + OpenLuxendoChannelsFileSeriesCommand.COMMAND_FULL_NAME )
public class OpenLuxendoChannelsFileSeriesCommand< R extends RealType< R > & NativeType< R > > extends AbstractOpenFileSeriesCommand< R >
{
    public static final String COMMAND_NAME = "Open Luxendo Channels...";
    public static final String COMMAND_FULL_NAME = COMMAND_BDP2_PREFIX + COMMAND_NAME;

    @Parameter(label = "Stack index"  )
    protected int stackIndex = 0;

    @Parameter(label = "Channels")
    protected String channels = "channel_0_Cam_Short,channel_0_Cam_Right";
    public static String CHANNELS_PARAMETER = "channels";

    public void run()
    {
        SwingUtilities.invokeLater( () ->  {

            String regExp = NamingSchemes.LUXENDO_REGEXP.replace( "STACK", "" + stackIndex );

            final List< String > channelList = Arrays.stream( channels.split( "," ) ).map( String::trim ).collect( Collectors.toList() );

            outputImage = BigDataProcessor2.openHdf5Series(
                                directory.toString(),
                                regExp,
                               "Data",
                                channelList );

            handleOutputImage( true, false );
        });
    }

    @Override
    public void recordJythonCall()
    {

    }
}
