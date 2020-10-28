package de.embl.cba.bdp2.open.luxendo;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.dialog.Utils;
import de.embl.cba.bdp2.macro.MacroRecorder;
import de.embl.cba.bdp2.open.AbstractOpenFileSeriesCommand;
import de.embl.cba.bdp2.open.ChannelChooserDialog;
import de.embl.cba.bdp2.open.fileseries.FileInfos;
import ij.plugin.frame.Recorder;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import javax.swing.*;

import java.awt.*;
import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static de.embl.cba.bdp2.open.NamingSchemes.LUXENDO_REGEXP;
import static de.embl.cba.bdp2.utils.Utils.COMMAND_BDP2_PREFIX;

@Plugin(type = Command.class, menuPath = Utils.BIGDATAPROCESSOR2_COMMANDS_MENU_ROOT + AbstractOpenFileSeriesCommand.COMMAND_OPEN_PATH + OpenLuxendoFileSeriesCommand.COMMAND_FULL_NAME )
public class OpenLuxendoFileSeriesCommand< R extends RealType< R > & NativeType< R > > extends AbstractOpenFileSeriesCommand< R >
{
    public static final String COMMAND_NAME = "Open Luxendo Hdf5...";
    public static final String COMMAND_FULL_NAME = COMMAND_BDP2_PREFIX + COMMAND_NAME;

    @Parameter( label = "Stack index"  )
    protected int stackIndex = 0;
    public static String STACK_INDEX_PARAMETER = "stackIndex";
    private MacroRecorder recorder;
    private String regExp;
    private String[] selectedChannels;

    public void run()
    {
        SwingUtilities.invokeLater( () ->  {
            regExp = LUXENDO_REGEXP.replace( "STACK", "" + stackIndex );

            if ( directory.getName().contains( "stack_" ) )
            {
                // In case the user mistakenly clicked one level too deep
                directory = new File( directory.getParent() );
            }

            FileInfos fileInfos = new FileInfos( directory.toString(), regExp, regExp, "Data" );
            final ChannelChooserDialog dialog = new ChannelChooserDialog( fileInfos.channelNames  );
            selectedChannels = dialog.getChannelsViaDialog();

            outputImage = BigDataProcessor2.openHdf5Series(
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
        if ( MacroRecorder.isScriptMode() )
        {
            recordJythonCall();
        }
        else
        {
            removeOpenLuxendoCommandCallFromRecorder();
            recorder = new MacroRecorder( OpenLuxendoChannelsFileSeriesCommand.COMMAND_FULL_NAME, viewingModality, outputImage );
            recorder.addCommandParameter( AbstractOpenFileSeriesCommand.DIRECTORY_PARAMETER, directory.getAbsolutePath() );
            recorder.addCommandParameter( AbstractOpenFileSeriesCommand.ARBITRARY_PLANE_SLICING_PARAMETER, enableArbitraryPlaneSlicing );
            recorder.addCommandParameter( OpenLuxendoFileSeriesCommand.STACK_INDEX_PARAMETER, stackIndex );
            recorder.addCommandParameter( OpenLuxendoChannelsFileSeriesCommand.CHANNELS_PARAMETER, String.join( ",", selectedChannels ) );
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
            //e.printStackTrace();
        }
    }

    @Override
    public void recordJythonCall()
    {
        recorder = new MacroRecorder();
        recorder.recordImportStatements( true );
        recorder.setAPIFunction( "openHdf5Series" );
        recorder.addAPIFunctionParameter( recorder.quote( directory.toString() ) );
        recorder.addAPIFunctionParameter( recorder.quote( regExp ) );
        recorder.addAPIFunctionParameter( recorder.quote( "Data" ) );
        recorder.addAPIFunctionParameter( selectedChannels );
        recorder.record();
    }
}
