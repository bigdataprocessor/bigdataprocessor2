package de.embl.cba.bdp2.open.fileseries;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.dialog.DialogUtils;
import de.embl.cba.bdp2.record.ScriptRecorder;
import de.embl.cba.bdp2.open.AbstractOpenCommand;
import de.embl.cba.bdp2.open.AbstractOpenFileSeriesCommand;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Plugin;

import javax.swing.*;

import static de.embl.cba.bdp2.BigDataProcessor2Menu.COMMAND_BDP2_PREFIX;

@Plugin(type = Command.class, menuPath = DialogUtils.BIGDATAPROCESSOR2_COMMANDS_MENU_ROOT + AbstractOpenCommand.COMMAND_OPEN_PATH + OpenEMTIFFPlanesFileSeriesCommand.COMMAND_FULL_NAME )
public class OpenEMTIFFPlanesFileSeriesCommand< R extends RealType< R > & NativeType< R > > extends AbstractOpenFileSeriesCommand< R >
{
    public static final String COMMAND_NAME = "Open EM TIFF Plane File Series...";
    public static final String COMMAND_FULL_NAME = COMMAND_BDP2_PREFIX + COMMAND_NAME;
    private String regExp = "(?<Z>.*)";

    public void run()
    {
        SwingUtilities.invokeLater( () ->  {
            outputImage = BigDataProcessor2.openTIFFSeries( directory.toString(), regExp );
            recordAPICall();
            handleOutputImage( true, false );
        });
    }

    @Override
    public void recordAPICall()
    {
        ScriptRecorder recorder = new ScriptRecorder( outputImage );
        recorder.recordImportStatements( true );
        recorder.setAPIFunctionName( "openTIFFSeries" );
        recorder.addAPIFunctionParameter( recorder.quote( directory.toString() ) );
        recorder.addAPIFunctionParameter( recorder.quote( regExp ) );
        recorder.record();
    }
}
