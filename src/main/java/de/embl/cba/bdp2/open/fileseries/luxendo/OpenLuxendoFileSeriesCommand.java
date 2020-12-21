package de.embl.cba.bdp2.open.fileseries.luxendo;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.dialog.DialogUtils;
import de.embl.cba.bdp2.macro.MacroRecorder;
import de.embl.cba.bdp2.open.AbstractOpenFileSeriesCommand;
import de.embl.cba.bdp2.open.ChannelChooserDialog;
import de.embl.cba.bdp2.open.fileseries.FileInfos;
import de.embl.cba.bdp2.open.fileseries.FileInfosHelper;
import ij.IJ;
import ij.plugin.frame.Recorder;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Plugin;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;

import static de.embl.cba.bdp2.open.NamingSchemes.LUXENDO;
import static de.embl.cba.bdp2.BigDataProcessor2Menu.COMMAND_BDP2_PREFIX;
import static de.embl.cba.bdp2.open.NamingSchemes.LUXENDO_STACKINDEX;

@Plugin(type = Command.class, menuPath = DialogUtils.BIGDATAPROCESSOR2_COMMANDS_MENU_ROOT + AbstractOpenFileSeriesCommand.COMMAND_OPEN_PATH + OpenLuxendoFileSeriesCommand.COMMAND_FULL_NAME )
public class OpenLuxendoFileSeriesCommand< R extends RealType< R > & NativeType< R > > extends AbstractOpenFileSeriesCommand< R >
{
    public static final String COMMAND_NAME = "Open Luxendo HDF5 File Series...";
    public static final String COMMAND_FULL_NAME = COMMAND_BDP2_PREFIX + COMMAND_NAME;

    private String regExp;
    private String[] selectedChannels;

    public void run()
    {
        SwingUtilities.invokeLater( () ->  {

            if ( directory.getName().contains( "stack_" ) )
            {
                // go one level up such that all stacks are contained
                directory = new File( directory.getParent() );
            }

            // TODO: put the whole thing into a method
            ArrayList< String > captures = FileInfosHelper.captureMatchesInSubFolders( directory, LUXENDO_STACKINDEX );

            if ( captures.isEmpty() )
            {
                IJ.showMessage("...");
            }
            else
            {
                // show a dialog with a choice
            }

            regExp = LUXENDO.replace( "STACK", "" + stackIndex );

            // Fetch available channels and let user choose which ones to open
            //
            FileInfos fileInfos = new FileInfos( directory.toString(), regExp,  "Data" );
            final ChannelChooserDialog dialog = new ChannelChooserDialog( fileInfos.channelNames  );
            selectedChannels = dialog.getChannelsViaDialog();

            // Open the image
            //
            outputImage = BigDataProcessor2.openHDF5Series(
                    directory.toString(),
                    fileInfos.getFilesInFolders(), // pass this on for performance
                    regExp,
                    "Data",
                    selectedChannels );

            recordMacro();
            handleOutputImage( true, false );
        });
    }

    public void recordMacro()
    {
        // Record a macro call, however not of this command, which lets the user
        // choose which files to open via wizard style dialogs, but to another command
        // which takes those choices as input, such that the recorded code can run headless.

        if ( MacroRecorder.isScriptMode() )
        {
            recordJythonCall();
        }
        else
        {
            // Since one currently cannot tell SciJava Commands that one does not want to
            // record them we need to remove the already recorded one.
            removeOpenLuxendoCommandCallFromRecorder();

            MacroRecorder recorder = new MacroRecorder( OpenLuxendoChannelsFileSeriesCommand.COMMAND_FULL_NAME, viewingModality, outputImage );
            recorder.addCommandParameter( AbstractOpenFileSeriesCommand.DIRECTORY_PARAMETER, directory.getAbsolutePath() );
            recorder.addCommandParameter( AbstractOpenFileSeriesCommand.ARBITRARY_PLANE_SLICING_PARAMETER, enableArbitraryPlaneSlicing );
            recorder.addCommandParameter( OpenLuxendoFileSeriesCommand.STACK_INDEX_PARAMETER, stackIndex );
            recorder.addCommandParameter( OpenLuxendoChannelsFileSeriesCommand.CHANNELS_PARAMETER, String.join( ",", selectedChannels ) );
            recorder.record();
        }
    }

    private void removeOpenLuxendoCommandCallFromRecorder()
    {
        try
        {
            Recorder recorder = Recorder.getInstance();
            if ( recorder == null ) return;
            Field f = recorder.getClass().getDeclaredField("textArea"); //NoSuchFieldException
            f.setAccessible(true);
            TextArea textArea = (TextArea) f.get(recorder); //IllegalAccessException
            String text = textArea.getText();
            int removeNumChars = Recorder.scriptMode() ? 8 : 5;
            int start = text.indexOf( OpenLuxendoFileSeriesCommand.COMMAND_FULL_NAME ) - removeNumChars;
            int end = text.length() - 1;
            textArea.replaceRange("", start, end );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }

    @Override
    public void recordJythonCall()
    {
        MacroRecorder recorder = new MacroRecorder( outputImage );
        recorder.recordImportStatements( true );
        recorder.setAPIFunctionName( "openHDF5Series" );
        recorder.addAPIFunctionPrequel( "# " + OpenLuxendoFileSeriesCommand.COMMAND_NAME );
        recorder.addAPIFunctionParameter( recorder.quote( directory.toString() ) );
        recorder.addAPIFunctionParameter( recorder.quote( regExp ) );
        recorder.addAPIFunctionParameter( recorder.quote( "Data" ) );
        recorder.addAPIFunctionParameter( selectedChannels );
        recorder.record();
    }
}
