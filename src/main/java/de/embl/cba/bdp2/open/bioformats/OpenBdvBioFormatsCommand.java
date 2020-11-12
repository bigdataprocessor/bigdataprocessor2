package de.embl.cba.bdp2.open.bioformats;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.dialog.Utils;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.macro.MacroRecorder;
import de.embl.cba.bdp2.open.AbstractOpenCommand;
import de.embl.cba.bdp2.open.AbstractOpenFileCommand;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Plugin;

import javax.swing.*;

import static de.embl.cba.bdp2.BigDataProcessor2Menu.COMMAND_BDP2_PREFIX;

@Plugin(type = Command.class, menuPath = Utils.BIGDATAPROCESSOR2_COMMANDS_MENU_ROOT + AbstractOpenCommand.COMMAND_OPEN_PATH + OpenBdvBioFormatsCommand.COMMAND_FULL_NAME )
public class OpenBdvBioFormatsCommand< R extends RealType< R > & NativeType< R >> extends AbstractOpenFileCommand< R >
{
    public static final String COMMAND_NAME = "Open Bio-Formats...";
    public static final String COMMAND_FULL_NAME = COMMAND_BDP2_PREFIX + COMMAND_NAME;

//    @Parameter( label = "Series index"  )
    private int seriesIndex = 0;

    @Override
    public void run() {
        SwingUtilities.invokeLater( () ->  {
            String filePath = file.getAbsolutePath();
            Logger.info( "# " + COMMAND_NAME);
            Logger.info( "Opening file: " + filePath);

            outputImage = BigDataProcessor2.openBioFormats( filePath, seriesIndex );
            handleOutputImage( true, false );
            recordJythonCall();
        });
    }

    public void recordJythonCall()
    {
        MacroRecorder recorder = new MacroRecorder( outputImage );
        recorder.recordImportStatements( true );
        recorder.setAPIFunctionName( "openBioFormats" );
        recorder.addAPIFunctionParameter( recorder.quote( file.getAbsolutePath() ) );
        recorder.addAPIFunctionParameter( String.valueOf( seriesIndex ) );
        recorder.record();
    }
}