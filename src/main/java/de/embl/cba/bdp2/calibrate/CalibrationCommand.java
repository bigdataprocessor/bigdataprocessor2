package de.embl.cba.bdp2.calibrate;

import de.embl.cba.bdp2.scijava.command.AbstractProcessingCommand;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Command.class, menuPath = "Plugins>BigDataProcessor2>Image>BDP2_Calibrate...")
public class CalibrationCommand< R extends RealType< R > & NativeType< R > > extends AbstractProcessingCommand< R >
{
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
