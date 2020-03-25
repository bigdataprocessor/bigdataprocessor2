package de.embl.cba.bdp2.open.command;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.open.files.FileInfos;
import de.embl.cba.bdp2.utils.Utils;
import loci.common.DebugTools;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Plugin;

import javax.swing.*;

@Plugin(type = Command.class, menuPath = "Plugins>BigDataProcessor2>Open>" + OpenLuxendoChannelsFromFoldersCommand.COMMAND_NAME )
public class OpenLuxendoChannelsFromFoldersCommand< R extends RealType< R > & NativeType< R > > extends AbstractOpenCommand< R >
{
    public static final String COMMAND_NAME = Utils.COMMAND_PREFIX + "Open Luxendo Channel Folders...";

    public void run()
    {
        SwingUtilities.invokeLater( () ->  {
            DebugTools.setRootLevel( "OFF" ); // Bio-Formats

            outputImage =
                    BigDataProcessor2.openImage(
                            directory.toString(),
                            FileInfos.LOAD_CHANNELS_FROM_FOLDERS,
                            FileInfos.PATTERN_LUXENDO,
                            "Data");

            handleOutputImage( true, false );
        });
    }
}
