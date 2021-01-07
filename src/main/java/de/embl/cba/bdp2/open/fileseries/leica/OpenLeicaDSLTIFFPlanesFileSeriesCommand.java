package de.embl.cba.bdp2.open.fileseries.leica;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.record.ScriptRecorder;
import de.embl.cba.bdp2.open.AbstractOpenFileSeriesCommand;
import de.embl.cba.bdp2.process.calibrate.CalibrationUtils;
import de.embl.cba.bdp2.dialog.DialogUtils;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.open.NamingSchemes;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Plugin;

import javax.swing.*;

import static de.embl.cba.bdp2.BigDataProcessor2Menu.COMMAND_BDP2_PREFIX;

@Plugin(type = Command.class, menuPath = DialogUtils.BIGDATAPROCESSOR2_COMMANDS_MENU_ROOT + AbstractOpenFileSeriesCommand.COMMAND_OPEN_PATH + OpenLeicaDSLTIFFPlanesFileSeriesCommand.COMMAND_FULL_NAME )
public class OpenLeicaDSLTIFFPlanesFileSeriesCommand< R extends RealType< R > & NativeType< R > > extends AbstractOpenFileSeriesCommand< R >
{
    public static final String COMMAND_NAME = "Open Leica DSL TIFF Plane File Series...";
    public static final String COMMAND_FULL_NAME = COMMAND_BDP2_PREFIX + COMMAND_NAME;
    private String regExp;

    public void run()
    {
        SwingUtilities.invokeLater( () ->  {
            regExp = NamingSchemes.LEICA_DSL_TIFF_PLANES;

            outputImage =
                    BigDataProcessor2.openTIFFSeries(
                            directory.toString(),
                            regExp );

            fixVoxelSpacing( outputImage );

            handleOutputImage( true, false );
        });
    }

    private void fixVoxelSpacing( Image< R > image )
    {
        // Sometimes Leica is calibrated as cm, which makes no sense
        final double[] voxelSpacing = image.getVoxelDimensions();
        final String voxelUnit = CalibrationUtils.fixVoxelSizeAndUnit( voxelSpacing, image.getVoxelUnit().toString() );
        image.setVoxelDimensions( voxelSpacing );
        image.setVoxelUnit( voxelUnit );
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
