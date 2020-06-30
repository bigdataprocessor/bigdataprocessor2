package de.embl.cba.bdp2.open.ui;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.calibrate.CalibrationUtils;
import de.embl.cba.bdp2.dialog.Utils;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.open.core.NamingSchemes;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Plugin;

import javax.swing.*;

import static de.embl.cba.bdp2.utils.Utils.COMMAND_BDP_PREFIX;

@Plugin(type = Command.class, menuPath = Utils.BIGDATAPROCESSOR2_COMMANDS_MENU_ROOT + AbstractOpenCommand.COMMAND_OPEN_PATH + OpenLeicaDSLTiffPlanesCommand.COMMAND_FULL_NAME )
public class OpenLeicaDSLTiffPlanesCommand< R extends RealType< R > & NativeType< R > > extends AbstractOpenCommand< R >
{
    public static final String COMMAND_NAME = "Open Leica DSL Tiff Planes...";
    public static final String COMMAND_FULL_NAME = COMMAND_BDP_PREFIX + COMMAND_NAME;

    public void run()
    {
        SwingUtilities.invokeLater( () ->  {
            outputImage =
                    BigDataProcessor2.openImage(
                            directory.toString(),
                            NamingSchemes.LEICA_LIGHT_SHEET_TIFF,
                            ".*.tif" );

            fixVoxelSpacing( outputImage );

            handleOutputImage( true, false );
        });
    }

    private void fixVoxelSpacing( Image< R > image )
    {
        // Sometimes Leica is calibrated as cm, which makes no sense
        final double[] voxelSpacing = image.getVoxelSize();
        final String voxelUnit = CalibrationUtils.fixVoxelSizeAndUnit( voxelSpacing, image.getVoxelUnit() );
        image.setVoxelSize( voxelSpacing );
        image.setVoxelUnit( voxelUnit );
    }
}
