package de.embl.cba.bdp2.open.command;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.calibrate.CalibrationUtils;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.open.files.FileInfos;
import de.embl.cba.bdp2.utils.Utils;
import loci.common.DebugTools;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Plugin;

import javax.swing.*;

@Plugin(type = Command.class, menuPath = "Plugins>BigDataProcessor2>Open>" + OpenLeicaDSLTiffPlanesCommand.COMMAND_NAME )
public class OpenLeicaDSLTiffPlanesCommand< R extends RealType< R > & NativeType< R > > extends AbstractOpenCommand< R >
{

    public static final String COMMAND_NAME = Utils.COMMAND_PREFIX + "Open Leica DSL Tiff Planes...";

    public void run()
    {
        SwingUtilities.invokeLater( () ->  {
            DebugTools.setRootLevel( "OFF" ); // Bio-Formats

            outputImage =
                    BigDataProcessor2.openImage(
                            directory.toString(),
                            FileInfos.LEICA_LIGHT_SHEET_TIFF,
                            ".*.tif" );

            fixVoxelSpacing( outputImage );

            handleOutputImage( true, false );
        });
    }

    private void fixVoxelSpacing( Image< R > image )
    {
        // Sometimes Leica is calibrated as cm, which makes no sense
        final double[] voxelSpacing = image.getVoxelSpacing();
        final String voxelUnit = CalibrationUtils.fixVoxelSpacingAndUnit( voxelSpacing, image.getVoxelUnit() );
        image.setVoxelSpacing( voxelSpacing );
        image.setVoxelUnit( voxelUnit );
    }
}
