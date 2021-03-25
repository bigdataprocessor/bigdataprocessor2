package de.embl.cba.bdp2.open.bioformats;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.dialog.DialogUtils;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.record.ScriptRecorder;
import de.embl.cba.bdp2.open.AbstractOpenCommand;
import de.embl.cba.bdp2.open.AbstractOpenFileCommand;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import javax.swing.*;

import static de.embl.cba.bdp2.BigDataProcessor2Menu.COMMAND_BDP2_PREFIX;

@Plugin(type = Command.class, menuPath = DialogUtils.BIGDATAPROCESSOR2_COMMANDS_MENU_ROOT + AbstractOpenCommand.COMMAND_OPEN_PATH + OpenBDVBioFormatsCommand.COMMAND_FULL_NAME, initializer = "iniMessage")
public class OpenBDVBioFormatsCommand< R extends RealType< R > & NativeType< R >> extends AbstractOpenFileCommand< R >
{
    public static final String COMMAND_NAME = "Open Bio-Formats...";
    public static final String COMMAND_FULL_NAME = COMMAND_BDP2_PREFIX + COMMAND_NAME;

    @Parameter( label = "Series index", min = "0", persist = false )
    private int seriesIndex = 0;

    @Override
    public void run() {
        SwingUtilities.invokeLater( () ->  {
            String filePath = file.getAbsolutePath();
            Logger.info( "# " + COMMAND_NAME);
            Logger.info( "Opening file: " + filePath);

            outputImage = BigDataProcessor2.openBioFormats( filePath, seriesIndex );
            handleOutputImage( true, false );
            recordAPICall();
        });
    }

    public void recordAPICall()
    {
        ScriptRecorder recorder = new ScriptRecorder( outputImage );
        recorder.recordImportStatements( true );
        recorder.setBDP2FunctionName( "openBioFormats" );
        recorder.addAPIFunctionParameter( recorder.quote( file.getAbsolutePath() ) );
        recorder.addAPIFunctionParameter( String.valueOf( seriesIndex ) );
        recorder.record();
    }
}