package de.embl.cba.bdp2.open.bioformats;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.dialog.DialogUtils;
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

@Plugin(type = Command.class, menuPath = DialogUtils.BIGDATAPROCESSOR2_COMMANDS_MENU_ROOT + AbstractOpenCommand.COMMAND_OPEN_PATH + OpenBdvBioFormatsCommand.COMMAND_FULL_NAME, initializer = "iniMessage")
public class OpenBdvBioFormatsCommand< R extends RealType< R > & NativeType< R >> extends AbstractOpenFileCommand< R >
{
    public static final String COMMAND_NAME = "Open Bio-Formats...";
    public static final String COMMAND_FULL_NAME = COMMAND_BDP2_PREFIX + COMMAND_NAME;

    // TODO: eventually implement pre-parsing of the file
    //  enabling the user to obtain upfront information about the image series.
    //  Such that he user could select which series to open.
    //  The minimal useful information would be how many series there are to chose from.
    //  Current issue is that reading this information can take a very long time for big image data
    //  and is thus not practical in general.
    //  WIP is on this branch: bioFormatsSeriesParser
    //  An alternative approach, maybe better,  would be to fetch this information while the file is opened anyway
    //  and only present the user with a choice if there happen to be more than one image series, using a dialog
    //@Parameter( label = "Series (image within file) index", min = "0", persist = false )
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
        MacroRecorder recorder = new MacroRecorder( outputImage );
        recorder.recordImportStatements( true );
        recorder.setAPIFunctionName( "openBioFormats" );
        recorder.addAPIFunctionParameter( recorder.quote( file.getAbsolutePath() ) );
        recorder.addAPIFunctionParameter( String.valueOf( seriesIndex ) );
        recorder.record();
    }
}