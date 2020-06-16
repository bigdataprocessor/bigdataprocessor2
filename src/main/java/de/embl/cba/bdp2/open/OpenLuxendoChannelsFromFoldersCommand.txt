package de.embl.cba.bdp2.open;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.open.core.NamingScheme;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Plugin;

import javax.swing.*;

import static de.embl.cba.bdp2.utils.Utils.COMMAND_BDP_PREFIX;

@Plugin(type = Command.class, menuPath = "Plugins>BigDataProcessor2>" + AbstractOpenCommand.COMMAND_OPEN_PATH + OpenLuxendoChannelsFromFoldersCommand.COMMAND_FULL_NAME )
public class OpenLuxendoChannelsFromFoldersCommand< R extends RealType< R > & NativeType< R > > extends AbstractOpenCommand< R >
{
    public static final String COMMAND_NAME = "Open Luxendo Channel Folders...";
    public static final String COMMAND_FULL_NAME = COMMAND_BDP_PREFIX + COMMAND_NAME;

    public void run()
    {
        SwingUtilities.invokeLater( () ->  {
            outputImage =
                    BigDataProcessor2.openImageFromHdf5(
                            directory.toString(),
                            NamingScheme.LOAD_CHANNELS_FROM_FOLDERS,
                            NamingScheme.PATTERN_LUXENDO,
                            "Data");

            handleOutputImage( true, false );
        });
    }
}
