package de.embl.cba.bdp2.open.luxendo;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.dialog.Utils;
import de.embl.cba.bdp2.macro.MacroRecorder;
import de.embl.cba.bdp2.open.AbstractOpenCommand;
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

@Plugin(type = Command.class, menuPath = Utils.BIGDATAPROCESSOR2_COMMANDS_MENU_ROOT + AbstractOpenCommand.COMMAND_OPEN_PATH + OpenLuxendoCommand.COMMAND_FULL_NAME )
public class OpenLuxendoCommand< R extends RealType< R > & NativeType< R > > extends AbstractOpenCommand< R >
{
    public static final String COMMAND_NAME = "Open Luxendo Hdf5...";
    public static final String COMMAND_FULL_NAME = COMMAND_BDP2_PREFIX + COMMAND_NAME;

    @Parameter( label = "Stack index"  )
    protected int stackIndex = 0;
    public static String STACK_INDEX_PARAMETER = "stackIndex";

    public void run()
    {
        SwingUtilities.invokeLater( () ->  {
            String regExp = LUXENDO_REGEXP.replace( "STACK", "" + stackIndex );

            if ( directory.getName().contains( "stack_" ) )
            {
                // In case the user mistakenly clicked one level too deep
                directory = new File( directory.getParent() );
            }


            FileInfos fileInfos = new FileInfos( directory.toString(), regExp, regExp, "Data" );
            final ChannelChooserDialog dialog = new ChannelChooserDialog( Arrays.asList( fileInfos.channelNames ) );
            List< String > selectedChannels = dialog.getChannelsViaDialog();

            // TODO: avoid that the fileInfos are fetched two times....

            outputImage = BigDataProcessor2.openHdf5Series(
                    directory.toString(),
                    regExp,
                    "Data",
                    selectedChannels );

            recordMacro( regExp, selectedChannels );

            handleOutputImage( true, false );
        });
    }

    public void recordMacro( String regExp, List< String > selectedChannels )
    {
        removeOpenLuxendoCommandCallFromRecorder();

        MacroRecorder recorder = new MacroRecorder( OpenLuxendoChannelsCommand.COMMAND_FULL_NAME, viewingModality, outputImage );
        recorder.addCommandParameter( AbstractOpenCommand.DIRECTORY_PARAMETER, directory.getAbsolutePath() );
        recorder.addCommandParameter( AbstractOpenCommand.ARBITRARY_PLANE_SLICING_PARAMETER, enableArbitraryPlaneSlicing );
        recorder.addCommandParameter( OpenLuxendoCommand.STACK_INDEX_PARAMETER, stackIndex );
        recorder.addCommandParameter( OpenLuxendoChannelsCommand.CHANNELS_PARAMETER, String.join( ",", selectedChannels ) );

        recorder.recordImportStatements( true );
        recorder.setAPIFunction( "openHdf5Series" );
        recorder.addAPIFunctionParameter( recorder.quote( directory.toString() ) );
        recorder.addAPIFunctionParameter( recorder.quote( regExp ) );
        recorder.addAPIFunctionParameter( recorder.quote( "Data" ) );
        recorder.record();
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
            int start = text.indexOf( OpenLuxendoCommand.COMMAND_FULL_NAME ) - removeNumChars;
            int end = text.length() - 1;
            textArea.replaceRange("", start, end );
        }
        catch ( Exception e )
        {
            //e.printStackTrace();
        }
    }

}
