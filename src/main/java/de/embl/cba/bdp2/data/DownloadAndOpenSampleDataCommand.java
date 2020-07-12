package de.embl.cba.bdp2.data;

import de.embl.cba.bdp2.dialog.Utils;
import de.embl.cba.bdp2.open.ui.AbstractOpenCommand;
import de.embl.cba.bdp2.scijava.Services;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import javax.swing.*;
import java.io.File;

import static de.embl.cba.bdp2.data.SampleData.DUAL_COLOR_MOUSE;
import static de.embl.cba.bdp2.data.SampleData.MINIMAL_SYNTHETIC;
import static de.embl.cba.bdp2.utils.Utils.COMMAND_BDP_PREFIX;


/**
 * @param <R>
 */
@Plugin(type = Command.class, menuPath = Utils.BIGDATAPROCESSOR2_COMMANDS_MENU_ROOT + AbstractOpenCommand.COMMAND_OPEN_PATH + DownloadAndOpenSampleDataCommand.COMMAND_FULL_NAME )
public class DownloadAndOpenSampleDataCommand< R extends RealType< R > & NativeType< R > > implements Command
{
    public static final String COMMAND_NAME = "Download and Open Sample Data...";
    public static final String COMMAND_FULL_NAME = COMMAND_BDP_PREFIX + COMMAND_NAME;
    public static BdvImageViewer parentBdvImageViewer;

    @Parameter
    CommandService commandService;

    @Parameter (label="Sample data", choices={ MINIMAL_SYNTHETIC, DUAL_COLOR_MOUSE  })
    String sampleDataName = MINIMAL_SYNTHETIC;

    @Parameter (label="Save to directory", style = "directory")
    File outputDirectory;

    public void run()
    {
        Services.commandService = commandService;

        SwingUtilities.invokeLater( () -> {
            new SampleData().downloadAndOpen( sampleDataName, outputDirectory, parentBdvImageViewer );
        } );
    }
}
