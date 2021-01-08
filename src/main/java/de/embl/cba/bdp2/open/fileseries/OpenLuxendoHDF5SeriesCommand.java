package de.embl.cba.bdp2.open.fileseries;

import de.embl.cba.bdp2.dialog.DialogUtils;
import de.embl.cba.bdp2.open.AbstractOpenFileSeriesCommand;
import de.embl.cba.bdp2.record.ScriptRecorder;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Plugin;

import javax.swing.*;

import static de.embl.cba.bdp2.open.NamingSchemes.LUXENDO;
import static de.embl.cba.bdp2.BigDataProcessor2Menu.COMMAND_BDP2_PREFIX;
import static de.embl.cba.bdp2.open.NamingSchemes.LUXENDO_STACKINDEX;

@Plugin(type = Command.class, menuPath = DialogUtils.BIGDATAPROCESSOR2_COMMANDS_MENU_ROOT + AbstractOpenFileSeriesCommand.COMMAND_OPEN_PATH + OpenLuxendoHDF5SeriesCommand.COMMAND_FULL_NAME )
public class OpenLuxendoHDF5SeriesCommand< R extends RealType< R > & NativeType< R > > extends AbstractOpenFileSeriesCommand< R >
{
    public static final String COMMAND_NAME = "Open Luxendo HDF5 Series...";
    public static final String COMMAND_FULL_NAME = COMMAND_BDP2_PREFIX + COMMAND_NAME;

    private static final String channelTimeRegExp = LUXENDO;
    private static final String positionRegExp = LUXENDO_STACKINDEX;

    public void run()
    {
        SwingUtilities.invokeLater( () ->  {
            PositionAndChannelsOpenerWizard openerWizard = new PositionAndChannelsOpenerWizard( directory, positionRegExp, channelTimeRegExp );
            openerWizard.run();
            ScriptRecorder.removeMacroCallFromRecorder(); // This command should not be recorded
        });
    }

    @Override
    public void recordAPICall()
    {
        // This command does not run headless, the recording takes places within the Wizard
    }
}
