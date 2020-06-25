package de.embl.cba.bdp2.open.ui;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.open.FixedListChannelSubsetter;
import de.embl.cba.bdp2.open.core.NamingSchemes;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import javax.swing.*;

import java.rmi.Naming;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static de.embl.cba.bdp2.open.core.NamingSchemes.LUXENDO_REGEXP_OLD;
import static de.embl.cba.bdp2.utils.Utils.COMMAND_BDP_PREFIX;

@Plugin(type = Command.class, menuPath = BigDataProcessor2Command.BIGDATAPROCESSOR2_PLUGINS_MENU_ROOT + AbstractOpenCommand.COMMAND_OPEN_PATH + OpenLuxendoChannelsCommand.COMMAND_FULL_NAME )
public class OpenLuxendoChannelsCommand< R extends RealType< R > & NativeType< R > > extends AbstractOpenCommand< R >
{
    public static final String COMMAND_NAME = "Open Luxendo Channels...";
    public static final String COMMAND_FULL_NAME = COMMAND_BDP_PREFIX + COMMAND_NAME;

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

            final FixedListChannelSubsetter channelSubsetter = new FixedListChannelSubsetter( channelList );

            outputImage = BigDataProcessor2.openImageFromHdf5(
                                directory.toString(),
                                regExp,
                                regExp,
                                "Data",
                                channelSubsetter );

            handleOutputImage( true, false );

        });
    }
}
