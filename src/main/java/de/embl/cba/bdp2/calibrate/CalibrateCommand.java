package de.embl.cba.bdp2.calibrate;

import de.embl.cba.bdp2.process.AbstractProcessingCommand;
import de.embl.cba.bdp2.utils.Utils;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Command.class, menuPath = de.embl.cba.bdp2.dialog.Utils.BIGDATAPROCESSOR2_COMMANDS_MENU_ROOT + AbstractProcessingCommand.COMMAND_PROCESS_PATH + CalibrateCommand.COMMAND_FULL_NAME )
public class CalibrateCommand< R extends RealType< R > & NativeType< R > > extends AbstractProcessingCommand< R >
{
    public static final String COMMAND_NAME = "Set Voxel Size...";
    public static final String COMMAND_FULL_NAME = Utils.COMMAND_BDP2_PREFIX + COMMAND_NAME;

    @Parameter(label = "Unit", choices = {"micrometer", "nanometer"}, persist = false)
    public String unit = "micrometer";

    @Parameter(label = "Voxel size X", persist = false)
    public double voxelSizeX = 1.0;
    public static String VOXEL_SIZE_X_PARAMETER = "voxelSizeX";

    @Parameter(label = "Voxel size Y", persist = false)
    public double voxelSizeY = 1.0;
    public static String VOXEL_SIZE_Y_PARAMETER = "voxelSizeY";

    @Parameter(label = "Voxel size Z", persist = false)
    public double voxelSizeZ = 1.0;
    public static String VOXEL_SIZE_Z_PARAMETER = "voxelSizeZ";

    public void run()
    {
        outputImage = inputImage;
        outputImage.setVoxelUnit( unit );
        outputImage.setVoxelSize( new double[]{ voxelSizeX, voxelSizeY, voxelSizeZ } );
        handleOutputImage( false, false );
    }
}
