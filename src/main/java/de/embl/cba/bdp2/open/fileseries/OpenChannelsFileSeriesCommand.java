package de.embl.cba.bdp2.open.fileseries;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.dialog.DialogUtils;
import de.embl.cba.bdp2.record.ScriptRecorder;
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

@Plugin(type = Command.class, menuPath = DialogUtils.BIGDATAPROCESSOR2_COMMANDS_MENU_ROOT + AbstractOpenFileSeriesCommand.COMMAND_OPEN_PATH + OpenChannelsFileSeriesCommand.COMMAND_FULL_NAME )
public class OpenChannelsFileSeriesCommand< R extends RealType< R > & NativeType< R > > extends AbstractOpenFileSeriesCommand< R >
{
    public static final String COMMAND_NAME = "Open Position And Channel Subset...";
    public static final String COMMAND_FULL_NAME = COMMAND_BDP2_PREFIX + COMMAND_NAME;

    @Parameter(label = "Regular Expression"  )
    protected String regExp = "";
    protected static String REGEXP_PARAMETER = "regExp";

    @Parameter(label = "Channels")
    protected String channelSubset = "channel_0_Cam_Short,channel_0_Cam_Right";
    protected static String CHANNELS_PARAMETER = "channelSubset";

    private String[] channels;
    private String[] files;

    public OpenChannelsFileSeriesCommand( )
    {
    }

    /**
     * In principle, it would be nice to directly add the channel subsetting to
     * the regular expression. However, for the Luxendo naming scheme, the
     * channel is distributed across different groups of the pattern,
     * which makes this a bit tedious. Thus, for now the regular expression
     * matches all channels and the selection of the subset happens afterwards.
     *
     * @param directory
     * @param files
     * @param channelSubset
     * @param regExp
     */
    public OpenChannelsFileSeriesCommand( File directory, String[] files, String channelSubset, String regExp  )
    {
        this.channelSubset = channelSubset;
        this.regExp = regExp;
        this.directory = directory;
        this.files = files;
    }

    public void run()
    {
        SwingUtilities.invokeLater( () ->  {

            channels = Arrays.stream( channelSubset.split( "," ) ).map( String::trim ).toArray( String[]::new );

            if ( NamingSchemes.isLuxendoNamingScheme( regExp ) )
            {
                outputImage = BigDataProcessor2.openHDF5Series(
                                directory.toString(),
                                files, // @Nullable, e.g., if called via macro
                                regExp,
                               "Data",
                                channels );
            }
            else if ( regExp.contains( NamingSchemes.TIF ) )
            {
                outputImage = BigDataProcessor2.openTIFFSeries(
                                directory.toString(),
                                files, // @Nullable, e.g., if called via macro
                                regExp,
                                channels );
            }

            handleOutputImage( true, false );

            recordMacro();
        });
    }

    private void recordMacro()
    {
        if ( ScriptRecorder.isScriptMode() )
        {
            recordAPICall();
        }
        else
        {
            ScriptRecorder recorder = new ScriptRecorder( this.COMMAND_FULL_NAME, viewingModality, outputImage );
            recorder.addCommandParameter( AbstractOpenFileSeriesCommand.DIRECTORY_PARAMETER, directory.getAbsolutePath() );
            recorder.addCommandParameter( AbstractOpenFileSeriesCommand.ARBITRARY_PLANE_SLICING_PARAMETER, enableArbitraryPlaneSlicing );
            recorder.addCommandParameter( this.REGEXP_PARAMETER, regExp );
            recorder.addCommandParameter( this.CHANNELS_PARAMETER, channelSubset );
            recorder.record();
        }
    }

    @Override
    public void recordAPICall()
    {
        ScriptRecorder recorder = new ScriptRecorder( outputImage );
        recorder.recordImportStatements( true );
        recorder.recordShowImage( true );

        if ( regExp.contains( NamingSchemes.HDF5 ) )
        {
            recorder.setAPIFunctionName( "openHDF5Series" );
            recorder.addAPIFunctionPrequelComment( this.COMMAND_NAME );
            recorder.addAPIFunctionParameter( recorder.quote( directory.toString() ) );
            recorder.addAPIFunctionParameter( recorder.quote( regExp ) );
            recorder.addAPIFunctionParameter( recorder.quote( "Data" ) );
            recorder.addAPIFunctionParameter( channels );
        }
        else if ( regExp.contains( NamingSchemes.TIF ) )
        {
            recorder.setAPIFunctionName( "openTIFFSeries" );
            recorder.addAPIFunctionPrequelComment( this.COMMAND_NAME );
            recorder.addAPIFunctionParameter( recorder.quote( directory.toString() ) );
            recorder.addAPIFunctionParameter( recorder.quote( regExp ) );
            recorder.addAPIFunctionParameter( channels );
        }

        recorder.record();
    }
}
