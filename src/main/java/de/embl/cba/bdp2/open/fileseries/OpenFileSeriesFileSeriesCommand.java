package de.embl.cba.bdp2.open.fileseries;

import de.embl.cba.bdp2.macro.MacroRecorder;
import de.embl.cba.bdp2.open.AbstractOpenFileSeriesCommand;
import de.embl.cba.bdp2.process.calibrate.CalibrationUtils;
import de.embl.cba.bdp2.dialog.Utils;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.BigDataProcessor2;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import javax.swing.*;

import static de.embl.cba.bdp2.open.NamingSchemes.*;
import static de.embl.cba.bdp2.utils.Utils.COMMAND_BDP2_PREFIX;


/**
 * TODO: How to add a HELP button for the regular expression without screwing up the macro recording?
 *
 *
 * @param <R>
 */
@Plugin(type = Command.class, menuPath = Utils.BIGDATAPROCESSOR2_COMMANDS_MENU_ROOT + AbstractOpenFileSeriesCommand.COMMAND_OPEN_PATH + OpenFileSeriesFileSeriesCommand.COMMAND_FULL_NAME )
public class OpenFileSeriesFileSeriesCommand< R extends RealType< R > & NativeType< R > > extends AbstractOpenFileSeriesCommand< R >
{
    public static final String COMMAND_NAME = "Open File Series...";
    public static final String COMMAND_FULL_NAME = COMMAND_BDP2_PREFIX + COMMAND_NAME;
    public static final String USE_CUSTOM = "Use below custom regular expression";

    //@Parameter(label = "Subset file using regular expression" )
    //String filterPattern = ".*";

    //@Parameter(label = "Regular expression help", callback = "showRegExpHelp", required = false)
    //Button regExpHelpButton;

    @Parameter(label = "File extension", choices = { TIF, OME_TIF, TIFF, H_5 })
    String fileExtension = ".tif";

    @Parameter(label = "Regular expression (excluding file extension)")
    String regExp = MULTI_CHANNEL_VOLUMES;

    @Parameter(label = "Hdf5 data set name (optional)", required = false)
    String hdf5DataSetName = "Data";

    public void run()
    {
        SwingUtilities.invokeLater( () ->  {

            regExp += fileExtension;

            if ( regExp.endsWith( ".h5" ) )
            {
                outputImage = BigDataProcessor2.openHdf5Series(
                        directory.toString(),
                        regExp,
                        hdf5DataSetName );

                recordJythonCall( "openHdf5Series" );
            }
            else if ( regExp.contains( ".tif" ) ) // covers .tif, .tiff, .ome.tif
            {
                outputImage = BigDataProcessor2.openTiffSeries(
                        directory.toString(),
                        regExp );

                recordJythonCall( "openTiffSeries" );
            }

            fixVoxelSpacing( outputImage );

            handleOutputImage( autoContrast, false );
        });
    }

    private void recordJythonCall( String apiFunctionName )
    {
        MacroRecorder recorder = new MacroRecorder( outputImage );
        recorder.recordImportStatements( true );
        recorder.setAPIFunction( apiFunctionName );
        recorder.addAPIFunctionParameter( recorder.quote( directory.toString() ) );
        recorder.addAPIFunctionParameter( recorder.quote( regExp ) );
        if ( apiFunctionName.equals( "openHdf5Series" ) )
            recorder.addAPIFunctionParameter( recorder.quote( hdf5DataSetName ) );
        recorder.record();
    }

    private void fixVoxelSpacing( Image< R > image )
    {
        // Sometimes Leica is calibrated as cm, which makes no sense
        final double[] voxelSpacing = image.getVoxelSize();
        final String voxelUnit = CalibrationUtils.fixVoxelSizeAndUnit( voxelSpacing, image.getVoxelUnit().toString() );
        image.setVoxelSize( voxelSpacing );
        image.setVoxelUnit( voxelUnit );
    }

    @Override
    public void recordJythonCall()
    {

    }
}
