package de.embl.cba.bdp2.open.fileseries.luxendo;

import de.embl.cba.bdp2.dialog.DialogUtils;
import de.embl.cba.bdp2.macro.MacroRecorderHelper;
import de.embl.cba.bdp2.open.AbstractOpenFileSeriesCommand;
import de.embl.cba.bdp2.open.fileseries.PositionAndChannelsOpenerWizard;
import de.embl.cba.bdp2.scijava.Services;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Plugin;

import javax.swing.*;

import static de.embl.cba.bdp2.open.NamingSchemes.LUXENDO;
import static de.embl.cba.bdp2.BigDataProcessor2Menu.COMMAND_BDP2_PREFIX;
import static de.embl.cba.bdp2.open.NamingSchemes.LUXENDO_STACKINDEX;

@Plugin(type = Command.class, menuPath = DialogUtils.BIGDATAPROCESSOR2_COMMANDS_MENU_ROOT + AbstractOpenFileSeriesCommand.COMMAND_OPEN_PATH + OpenLuxendoFileSeriesCommand.COMMAND_FULL_NAME )
public class OpenLuxendoFileSeriesCommand< R extends RealType< R > & NativeType< R > > extends AbstractOpenFileSeriesCommand< R >
{
    public static final String COMMAND_NAME = "Open Luxendo HDF5 File Series...";
    public static final String COMMAND_FULL_NAME = COMMAND_BDP2_PREFIX + COMMAND_NAME;

    private static final String channelTimeRegExp = LUXENDO;
    private static final String positionRegExp = LUXENDO_STACKINDEX;

    public void run()
    {
        SwingUtilities.invokeLater( () ->  {
            PositionAndChannelsOpenerWizard openerWizard = new PositionAndChannelsOpenerWizard( directory, positionRegExp, channelTimeRegExp );
            openerWizard.run();
        });

        // This command does not run headless
        MacroRecorderHelper.removeCommandFromRecorder( this.COMMAND_FULL_NAME );
    }

    @Override
    public void recordJythonCall()
    {
        // This command does not run headless
    }
}
