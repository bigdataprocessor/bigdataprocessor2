package de.embl.cba.bdp2.open.leica;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.open.AbstractOpenFileSeriesCommand;
import de.embl.cba.bdp2.process.calibrate.CalibrationUtils;
import de.embl.cba.bdp2.dialog.Utils;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.open.NamingSchemes;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Plugin;

import javax.swing.*;

import static de.embl.cba.bdp2.utils.Utils.COMMAND_BDP2_PREFIX;

@Plugin(type = Command.class, menuPath = Utils.BIGDATAPROCESSOR2_COMMANDS_MENU_ROOT + AbstractOpenFileSeriesCommand.COMMAND_OPEN_PATH + OpenLeicaDSLTiffPlanesFileSeriesCommand.COMMAND_FULL_NAME )
public class OpenLeicaDSLTiffPlanesFileSeriesCommand< R extends RealType< R > & NativeType< R > > extends AbstractOpenFileSeriesCommand< R >
{
    public static final String COMMAND_NAME = "Open Leica DSL Tiff Planes...";
    public static final String COMMAND_FULL_NAME = COMMAND_BDP2_PREFIX + COMMAND_NAME;

    public void run()
    {
        SwingUtilities.invokeLater( () ->  {
            outputImage =
                    BigDataProcessor2.openTiffSeries(
                            directory.toString(),
                            NamingSchemes.LEICA_DSL_TIFF_PLANES_REG_EXP,
                            ".*.tif" );

            fixVoxelSpacing( outputImage );

            handleOutputImage( true, false );
        });
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
