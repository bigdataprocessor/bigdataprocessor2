package de.embl.cba.bdp2;

import de.embl.cba.bdp2.process.crop.CropDialog;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.scijava.Services;
import ij.plugin.frame.Recorder;
import net.imagej.ImageJ;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;

import javax.swing.*;

@Plugin(type = Command.class, menuPath = "Plugins>BigDataProcessor>" + BigDataProcessor2Command.COMMAND_FULL_NAME )
public class BigDataProcessor2Command< R extends RealType< R > & NativeType< R > > implements Command
{
    @Parameter
    Context context;

    @Parameter
    CommandService commandService;

    @Parameter
    UIService uiService;

    public static final String COMMAND_NAME = "BigDataProcessor2";
    public static final String COMMAND_FULL_NAME = "" + COMMAND_NAME;

    public void run()
    {
        Services.setCommandService( commandService );
        Services.setUiService( uiService );
        Services.setContext( context );

        SwingUtilities.invokeLater( () -> {
            CropDialog.askForUnitsChoice = true;
            BigDataProcessor2UI.showUI();
        } );
    }

    public static void main ( String... args )
    {
        final ImageJ ij = new ImageJ();
        ij.ui().showUI();
        //Logger.setLevel( Logger.Level.Benchmark );
        //new Recorder();
        ij.command().run( BigDataProcessor2Command.class, true );
    }
}
