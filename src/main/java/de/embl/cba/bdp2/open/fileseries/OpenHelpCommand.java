package de.embl.cba.bdp2.open.fileseries;

import de.embl.cba.bdp2.dialog.HelpWindow;
import de.embl.cba.bdp2.dialog.DialogUtils;
import de.embl.cba.bdp2.open.AbstractOpenFileSeriesCommand;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Plugin;

import javax.swing.*;

import static de.embl.cba.bdp2.BigDataProcessor2Menu.COMMAND_BDP2_PREFIX;

@Plugin(type = Command.class, menuPath = DialogUtils.BIGDATAPROCESSOR2_COMMANDS_MENU_ROOT + AbstractOpenFileSeriesCommand.COMMAND_OPEN_PATH + OpenHelpCommand.COMMAND_FULL_NAME )
public class OpenHelpCommand< R extends RealType< R > & NativeType< R > > implements Command
{
    public static final String COMMAND_NAME = "Help";
    public static final String COMMAND_FULL_NAME = COMMAND_BDP2_PREFIX + COMMAND_NAME;

    public void run()
    {
        SwingUtilities.invokeLater( () -> {
            final HelpWindow helpWindow = new HelpWindow( AbstractOpenFileSeriesCommand.class.getResource( "/OpenHelp.html" ) );
            helpWindow.setVisible( true );
        } );
    }
}
