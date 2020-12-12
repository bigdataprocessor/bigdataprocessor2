package de.embl.cba.bdp2.open.fileseries;
import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.dialog.DialogUtils;
import de.embl.cba.bdp2.macro.MacroRecorder;
import de.embl.cba.bdp2.open.AbstractOpenCommand;
import de.embl.cba.bdp2.open.AbstractOpenFileSeriesCommand;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import javax.swing.*;

import java.io.File;

import static de.embl.cba.bdp2.BigDataProcessor2Menu.COMMAND_BDP2_PREFIX;

@Plugin(type = Command.class, menuPath = DialogUtils.BIGDATAPROCESSOR2_COMMANDS_MENU_ROOT + AbstractOpenCommand.COMMAND_OPEN_PATH + OpenSingleTiffVolumeCommand.COMMAND_FULL_NAME )
public class OpenSingleTiffVolumeCommand< R extends RealType< R > & NativeType< R > > extends AbstractOpenCommand< R >
{
    public static final String COMMAND_NAME = "Open Single Tiff Volume...";
    public static final String COMMAND_FULL_NAME = COMMAND_BDP2_PREFIX + COMMAND_NAME;

    @Parameter( label = "Tiff file")
    File file;

    private String directory;
    private String regExp;

    public void run()
    {
        SwingUtilities.invokeLater( () ->  {
            directory = file.getParent();
            regExp = "(?<T>"+file.getName()+")";
            regExp += FileInfos.NONRECURSIVE;
            outputImage = BigDataProcessor2.openTiffSeries( directory, regExp );
            recordJythonCall();
            handleOutputImage( true, false );
        });
    }

    @Override
    public void recordJythonCall()
    {
        MacroRecorder recorder = new MacroRecorder( outputImage );
        recorder.recordImportStatements( true );
        recorder.setAPIFunctionName( "openTiffSeries" );
        recorder.addAPIFunctionParameter( recorder.quote( directory) );
        recorder.addAPIFunctionParameter( recorder.quote( regExp ) );
        recorder.record();
    }
}
