package de.embl.cba.bdp2;

import de.embl.cba.bdp2.crop.CropDialog;
import de.embl.cba.bdp2.scijava.Services;
import de.embl.cba.bdp2.ui.BigDataProcessor2UI;
import ij.plugin.frame.Recorder;
import net.imagej.ImageJ;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import javax.swing.*;

@Plugin(type = Command.class, menuPath = "Plugins>BigDataProcessor>" + BigDataProcessor2Command.COMMAND_FULL_NAME )
public class BigDataProcessor2Command< R extends RealType< R > & NativeType< R > > implements Command
{
    @Parameter
    CommandService cs;

    public static final String COMMAND_NAME = "BigDataProcessor2";
    public static final String COMMAND_FULL_NAME = "" + COMMAND_NAME;

    public void run()
    {
        Services.commandService = cs;
        CropDialog.askForUnitsChoice = true;

        SwingUtilities.invokeLater( () -> {
            BigDataProcessor2UI.showUI();
        } );
    }

    public static void main ( String... args )
    {
        final ImageJ ij = new ImageJ();
        ij.ui().showUI();

        new Recorder();

        Services.commandService = ij.command();

        ij.command().run( BigDataProcessor2Command.class, true );
    }
}
