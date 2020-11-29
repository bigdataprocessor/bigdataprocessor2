package de.embl.cba.bdp2.open.fileseries;

import de.embl.cba.bdp2.macro.MacroRecorder;
import de.embl.cba.bdp2.open.AbstractOpenFileSeriesCommand;
import de.embl.cba.bdp2.process.calibrate.CalibrationUtils;
import de.embl.cba.bdp2.dialog.DialogUtils;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.BigDataProcessor2;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import javax.swing.*;

import static de.embl.cba.bdp2.open.NamingSchemes.*;
import static de.embl.cba.bdp2.BigDataProcessor2Menu.COMMAND_BDP2_PREFIX;

@Plugin(type = Command.class, menuPath = DialogUtils.BIGDATAPROCESSOR2_COMMANDS_MENU_ROOT + AbstractOpenFileSeriesCommand.COMMAND_OPEN_PATH + OpenFileSeriesCommand.COMMAND_FULL_NAME )
public class OpenFileSeriesCommand< R extends RealType< R > & NativeType< R > > extends AbstractOpenFileSeriesCommand< R >
{
    public static final String COMMAND_NAME = "Open Custom File Series...";
    public static final String COMMAND_FULL_NAME = COMMAND_BDP2_PREFIX + COMMAND_NAME;

    @Parameter(label = "File extension", choices = { TIF, OME_TIF, TIFF, H_5 })
    String fileExtension = ".tif";

    @Parameter(label = "Regular expression (excluding file extension)")
    String regExp = MULTI_CHANNEL_VOLUMES;

    @Parameter(label = "HDF5 dataset path (for .h5)", required = false)
    String hdf5DataSetName = "Data";

    public void run()
    {
        SwingUtilities.invokeLater( () ->  {

            regExp += fileExtension;

            if ( regExp.endsWith( ".h5" ) )
            {
                outputImage = BigDataProcessor2.openHDF5Series(
                        directory.toString(),
                        regExp,
                        hdf5DataSetName );
            }
            else if ( regExp.contains( ".tif" ) ) // covers .tif, .tiff, .ome.tif
            {
                outputImage = BigDataProcessor2.openTiffSeries(
                        directory.toString(),
                        regExp );
            }

            recordJythonCall();
            fixVoxelSpacing( outputImage );
            handleOutputImage( autoContrast, false );
        });
    }

    private void fixVoxelSpacing( Image< R > image )
    {
        // Sometimes Leica is calibrated as cm, which makes no sense
        final double[] voxelSpacing = image.getVoxelDimension();
        final String voxelUnit = CalibrationUtils.fixVoxelSizeAndUnit( voxelSpacing, image.getVoxelUnit().toString() );
        image.setVoxelDimension( voxelSpacing );
        image.setVoxelUnit( voxelUnit );
    }

    @Override
    public void recordJythonCall()
    {
        if ( regExp.endsWith( ".h5" ) )
            recordJythonCall( "openHDF5Series" );

        if ( regExp.contains( ".tif" ) )
            recordJythonCall( "openTiffSeries" );
    }

    private void recordJythonCall( String apiFunctionName )
    {
        MacroRecorder recorder = new MacroRecorder( outputImage );
        recorder.recordImportStatements( true );
        recorder.setAPIFunctionName( apiFunctionName );
        recorder.addAPIFunctionParameter( recorder.quote( directory.toString() ) );
        recorder.addAPIFunctionParameter( recorder.quote( regExp ) );
        if ( apiFunctionName.equals( "openHDF5Series" ) )
            recorder.addAPIFunctionParameter( recorder.quote( hdf5DataSetName ) );
        recorder.record();
    }
}
