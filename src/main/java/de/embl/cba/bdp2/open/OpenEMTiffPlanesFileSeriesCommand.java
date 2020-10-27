package de.embl.cba.bdp2.open;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.dialog.Utils;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Plugin;

import javax.swing.*;

import static de.embl.cba.bdp2.utils.Utils.COMMAND_BDP2_PREFIX;

@Plugin(type = Command.class, menuPath = Utils.BIGDATAPROCESSOR2_COMMANDS_MENU_ROOT + AbstractOpenCommand.COMMAND_OPEN_PATH + OpenEMTiffPlanesFileSeriesCommand.COMMAND_FULL_NAME )
public class OpenEMTiffPlanesFileSeriesCommand< R extends RealType< R > & NativeType< R > > extends AbstractOpenFileSeriesCommand< R >
{
    public static final String COMMAND_NAME = "Open EM Tiff Planes...";
    public static final String COMMAND_FULL_NAME = COMMAND_BDP2_PREFIX + COMMAND_NAME;

    public void run()
    {
        SwingUtilities.invokeLater( () ->  {
            outputImage = BigDataProcessor2.openTiffSeries(
                            directory.toString(),
                            NamingSchemes.TIFF_SLICES,
                            ".*" );
            recordJythonCall();
            handleOutputImage( true, false );
        });
    }

    @Override
    public void recordJythonCall()
    {
        // TODO!
    }
}
