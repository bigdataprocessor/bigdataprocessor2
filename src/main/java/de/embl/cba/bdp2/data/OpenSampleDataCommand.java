package de.embl.cba.bdp2.data;

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

import static de.embl.cba.bdp2.data.SampleData.TIFF_VOLUMES_X_50_Y_50_Z_50_C_2_T_6_1_6_MB;


/**
 * @param <R>
 */
@Plugin(type = Command.class, menuPath = "Plugins>BigDataProcessor2>Sample Data>" + OpenSampleDataCommand.COMMAND_FULL_NAME )
public class OpenSampleDataCommand< R extends RealType< R > & NativeType< R > > implements Command
{
    public static final String COMMAND_NAME = "Open Sample Data...";
    public static final String COMMAND_FULL_NAME = "" + COMMAND_NAME;
    public static BdvImageViewer parentBdvImageViewer;

    @Parameter
    CommandService commandService;

    @Parameter (label="Sample data", choices={ TIFF_VOLUMES_X_50_Y_50_Z_50_C_2_T_6_1_6_MB })
    String sampleDataName = TIFF_VOLUMES_X_50_Y_50_Z_50_C_2_T_6_1_6_MB;

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
