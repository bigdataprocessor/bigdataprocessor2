package de.embl.cba.bdp2.quit;

import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.scijava.Services;
import de.embl.cba.bdp2.utils.Utils;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Plugin;

@Plugin(type = Command.class, menuPath = de.embl.cba.bdp2.dialog.Utils.BIGDATAPROCESSOR2_COMMANDS_MENU_ROOT + QuitCommand.COMMAND_PATH + QuitCommand.COMMAND_FULL_NAME )
public class QuitCommand< R extends RealType< R > & NativeType< R > > implements Command
{
    public static final String COMMAND_PATH = "Commands>";
    public static final String COMMAND_NAME = "Quit Fiji";
    public static final String COMMAND_FULL_NAME = Utils.COMMAND_BDP2_PREFIX + COMMAND_NAME;

    public void run()
    {
        Logger.info( "Shutting down...." );
        try {
            Services.context.dispose();
        }
        finally {
            System.exit(1);
        }
    }
}
