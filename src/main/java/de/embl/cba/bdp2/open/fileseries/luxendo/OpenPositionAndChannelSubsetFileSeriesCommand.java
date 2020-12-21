package de.embl.cba.bdp2.open.fileseries.luxendo;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.dialog.DialogUtils;
import de.embl.cba.bdp2.macro.MacroRecorder;
import de.embl.cba.bdp2.open.NamingSchemes;
import de.embl.cba.bdp2.open.AbstractOpenFileSeriesCommand;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import javax.swing.*;

import java.io.File;
import java.util.Arrays;

import static de.embl.cba.bdp2.BigDataProcessor2Menu.COMMAND_BDP2_PREFIX;

@Plugin(type = Command.class, menuPath = DialogUtils.BIGDATAPROCESSOR2_COMMANDS_MENU_ROOT + AbstractOpenFileSeriesCommand.COMMAND_OPEN_PATH + OpenPositionAndChannelSubsetFileSeriesCommand.COMMAND_FULL_NAME )
public class OpenPositionAndChannelSubsetFileSeriesCommand< R extends RealType< R > & NativeType< R > > extends AbstractOpenFileSeriesCommand< R >
{
    public static final String COMMAND_NAME = "Open Position And Channel Subset...";
    public static final String COMMAND_FULL_NAME = COMMAND_BDP2_PREFIX + COMMAND_NAME;

    private String[][] filesInFolders;

    @Parameter(label = "Regular Expression (including POSITION)"  )
    protected String positionRegExp = "";
    protected static String POSITION_REGEXP_PARAMETER = "positionRegExp";

    @Parameter(label = "Position"  )
    protected String position = "";
    protected static String POSITION_PARAMETER = "position";

    @Parameter(label = "Channels")
    protected String channelSubset = "channel_0_Cam_Short,channel_0_Cam_Right";
    protected static String CHANNELS_PARAMETER = "channels";

    private String regExp;
    private String[] channels;

    public OpenPositionAndChannelSubsetFileSeriesCommand( File directory, String[][] filesInFolders, String channelSubset, String position, String positionRegExp  )
    {
        this.channelSubset = channelSubset;
        this.position = position;
        this.positionRegExp = positionRegExp;
        this.directory = directory;
        this.filesInFolders = filesInFolders;
    }

    public void run()
    {
        // TODO: Can I generalise this to also use it for Viventis??
        //   STACK = POSITION ?

        SwingUtilities.invokeLater( () ->  {

            regExp = positionRegExp.replace( NamingSchemes.P, position );
            channels = Arrays.stream( channelSubset.split( "," ) ).map( String::trim ).toArray( String[]::new );

            if ( regExp.contains( NamingSchemes.HDF5 ) )
            {
                outputImage = BigDataProcessor2.openHDF5Series(
                                directory.toString(),
                                filesInFolders,
                        regExp,
                               "Data",
                        channels );
            }
            else if ( regExp.contains( NamingSchemes.TIF ) )
            {
                outputImage = BigDataProcessor2.openTIFFSeries(
                        directory.toString(),
                        filesInFolders,
                        regExp,
                        channels );
            }

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
            MacroRecorder recorder = new MacroRecorder( this.COMMAND_FULL_NAME, viewingModality, outputImage );
            recorder.addCommandParameter( AbstractOpenFileSeriesCommand.DIRECTORY_PARAMETER, directory.getAbsolutePath() );
            recorder.addCommandParameter( AbstractOpenFileSeriesCommand.ARBITRARY_PLANE_SLICING_PARAMETER, enableArbitraryPlaneSlicing );
            recorder.addCommandParameter( this.POSITION_REGEXP_PARAMETER, positionRegExp );
            recorder.addCommandParameter( this.POSITION_PARAMETER, position );
            recorder.addCommandParameter( this.CHANNELS_PARAMETER, channelSubset );
            recorder.record();
        }
    }

    @Override
    public void recordJythonCall()
    {
        MacroRecorder recorder = new MacroRecorder( outputImage );
        recorder.recordImportStatements( true );

        if ( regExp.contains( NamingSchemes.HDF5 ) )
        {
            recorder.setAPIFunctionName( "openHDF5Series" );
            recorder.addAPIFunctionPrequel( "# " + this.COMMAND_NAME );
            recorder.addAPIFunctionParameter( recorder.quote( directory.toString() ) );
            recorder.addAPIFunctionParameter( recorder.quote( regExp ) );
            recorder.addAPIFunctionParameter( recorder.quote( "Data" ) );
            recorder.addAPIFunctionParameter( channels );
        }
        else if ( regExp.contains( NamingSchemes.TIF ) )
        {
            recorder.setAPIFunctionName( "openTIFFSeries" );
            recorder.addAPIFunctionPrequel( "# " + this.COMMAND_NAME );
            recorder.addAPIFunctionParameter( recorder.quote( directory.toString() ) );
            recorder.addAPIFunctionParameter( recorder.quote( regExp ) );
            recorder.addAPIFunctionParameter( channels );
        }

        recorder.record();
    }
}
