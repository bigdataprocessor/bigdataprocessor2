package de.embl.cba.bdp2.calibrate;

import de.embl.cba.bdp2.process.AbstractProcessingCommand;
import de.embl.cba.bdp2.utils.Utils;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Command.class, menuPath = "Plugins>BigDataProcessor2>" + AbstractProcessingCommand.COMMAND_PROCESS_PATH + CalibrateCommand.COMMAND_FULL_NAME )
public class CalibrateCommand< R extends RealType< R > & NativeType< R > > extends AbstractProcessingCommand< R >
{
    public static final String COMMAND_NAME = "Set Voxel Size...";
    public static final String COMMAND_FULL_NAME = Utils.COMMAND_BDP_PREFIX + COMMAND_NAME;

    @Parameter(label = "Unit", choices = {"micrometer", "nanometer"}, persist = false)
    public String unit = "micrometer";

    @Parameter(label = "Voxel spacing X", persist = false)
    public double voxelSpacingX = 1.0;

    @Parameter(label = "Voxel spacing Y", persist = false)
    public double voxelSpacingY = 1.0;

    @Parameter(label = "Voxel spacing Z", persist = false)
    public double voxelSpacingZ = 1.0;

    public void run()
    {
        outputImage = inputImage;
        outputImage.setVoxelUnit( unit );
        outputImage.setVoxelSpacing( new double[]{voxelSpacingX, voxelSpacingY, voxelSpacingZ} );
        handleOutputImage( false, false );
    }
}
