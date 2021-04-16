package de.embl.cba.bdp2.open.fileseries;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.dialog.DialogUtils;
import de.embl.cba.bdp2.open.AbstractOpenCommand;
import de.embl.cba.bdp2.open.NamingSchemes;
import de.embl.cba.bdp2.record.ScriptRecorder;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import javax.swing.*;
import java.io.File;

import static de.embl.cba.bdp2.BigDataProcessor2Menu.COMMAND_BDP2_PREFIX;

@Plugin(type = Command.class, menuPath = DialogUtils.BIGDATAPROCESSOR2_COMMANDS_MENU_ROOT + AbstractOpenCommand.COMMAND_OPEN_PATH + OpenSingleHDF5VolumeCommand.COMMAND_FULL_NAME )
public class OpenSingleHDF5VolumeCommand< R extends RealType< R > & NativeType< R > > extends AbstractOpenCommand< R >
{
    public static final String COMMAND_NAME = "Open Single HDF5 Volume...";
    public static final String COMMAND_FULL_NAME = COMMAND_BDP2_PREFIX + COMMAND_NAME;

    @Parameter( label = "HDF5 file")
    File file;

    @Parameter( label = "HDF5 dataset path")
    String hdf5DatasetPath = "Data";

    private String directory;
    private String regExp;

    public void run()
    {
        SwingUtilities.invokeLater( () ->  {
            directory = file.getParent();
            regExp = "(?<T>"+file.getName()+")";
            regExp += NamingSchemes.NONRECURSIVE;
            outputImage = BigDataProcessor2.openHDF5Series( directory, regExp, hdf5DatasetPath );
            recordAPICall();
            handleOutputImage( true, false );
        });
    }

    @Override
    public void recordAPICall()
    {
        ScriptRecorder recorder = new ScriptRecorder( outputImage );
        recorder.recordImportStatements( true );
        recorder.setBDP2FunctionName( "openHDFS5eries" );
        recorder.addAPIFunctionParameter( recorder.quote( directory) );
        recorder.addAPIFunctionParameter( recorder.quote( regExp ) );
        recorder.addAPIFunctionParameter( recorder.quote( hdf5DatasetPath ) );
        recorder.record();
    }
}
