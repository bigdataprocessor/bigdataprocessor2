package de.embl.cba.bdp2.open.bioformats;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.dialog.Utils;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.macro.MacroRecorder;
import de.embl.cba.bdp2.open.AbstractOpenCommand;
import de.embl.cba.bdp2.open.AbstractOpenFileCommand;
import de.embl.cba.bdp2.open.AbstractOpenFileSeriesCommand;
import mdbtools.libmdb.file;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import javax.swing.*;

import java.io.File;

import static de.embl.cba.bdp2.utils.Utils.COMMAND_BDP2_PREFIX;

@Plugin(type = Command.class, menuPath = Utils.BIGDATAPROCESSOR2_COMMANDS_MENU_ROOT + AbstractOpenCommand.COMMAND_OPEN_PATH + OpenWithBdvBioFormatsCommand.COMMAND_FULL_NAME )
public class OpenWithBdvBioFormatsCommand< R extends RealType< R > & NativeType< R >> extends AbstractOpenFileCommand< R >
{
    public static final String COMMAND_NAME = "Open with Bio-Formats...";
    public static final String COMMAND_FULL_NAME = COMMAND_BDP2_PREFIX + COMMAND_NAME;

    @Parameter( label = "Series index"  )
    private int seriesIndex = 0;

    @Override
    public void run() {
        SwingUtilities.invokeLater( () ->  {
            String filePath = file.getAbsolutePath();
            Logger.info( "# " + COMMAND_NAME);
            Logger.info( "Opening file: " + filePath);
            outputImage = BigDataProcessor2.openWithBioFormats( filePath, seriesIndex );
            handleOutputImage( true, false );
            recordJythonCall();
        });
    }

    public void recordJythonCall()
    {
        MacroRecorder recorder = new MacroRecorder( outputImage );
        recorder.recordImportStatements( true );
        recorder.setAPIFunction( "openWithBioFormats" );
        recorder.addAPIFunctionParameter( recorder.quote( file.getAbsolutePath() ) );
        recorder.addAPIFunctionParameter( String.valueOf( seriesIndex ) );
        recorder.record();
    }
}