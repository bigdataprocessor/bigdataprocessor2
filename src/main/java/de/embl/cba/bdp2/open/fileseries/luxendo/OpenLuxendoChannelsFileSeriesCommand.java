package de.embl.cba.bdp2.open.fileseries.luxendo;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.dialog.DialogUtils;
import de.embl.cba.bdp2.open.NamingSchemes;
import de.embl.cba.bdp2.open.AbstractOpenFileSeriesCommand;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import javax.swing.*;

import java.util.Arrays;

import static de.embl.cba.bdp2.BigDataProcessor2Menu.COMMAND_BDP2_PREFIX;

@Plugin(type = Command.class, menuPath = DialogUtils.BIGDATAPROCESSOR2_COMMANDS_MENU_ROOT + AbstractOpenFileSeriesCommand.COMMAND_OPEN_PATH + OpenLuxendoChannelsFileSeriesCommand.COMMAND_FULL_NAME )
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

            String regExp = NamingSchemes.LUXENDO.replace( "STACK", "" + stackIndex );

            outputImage = BigDataProcessor2.openHDF5Series(
                                directory.toString(),
                                regExp,
                               "Data",
                                Arrays.stream( channels.split( "," ) ).map( String::trim ).toArray( String[]::new ));

            handleOutputImage( true, false );
        });
    }

    @Override
    public void recordJythonCall()
    {
        // not needed as this is recorded from OpenLuxendoFileSeriesCommand
    }
}
