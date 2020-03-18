package de.embl.cba.bdp2.calibrate;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.service.BdvService;
import de.embl.cba.bdp2.service.ImageService;
import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Command.class, menuPath = "Plugins>BigDataProcessor2>Image>BDP2_Calibrate...")
public class CalibrateCommand< R extends RealType< R > & NativeType< R > > implements Command
{
    @Parameter(label = "Input image name")
    public Image inputImage = ImageService.nameToImage.values().iterator().next();

    @Parameter(label = "Unit", choices = {"micrometer", "nanometer"}, persist = false)
    public String unit = "micrometer";

    @Parameter(label = "Voxel spacing X", persist = false)
    public double voxelSpacingX = 1.0;

    @Parameter(label = "Voxel spacing Y", persist = false)
    public double voxelSpacingY = 1.0;

    @Parameter(label = "Voxel spacing Z", persist = false)
    public double voxelSpacingZ = 1.0;

    @Override
    public void run()
    {
        inputImage.setVoxelUnit( unit );
        inputImage.setVoxelSpacing( new double[]{voxelSpacingX, voxelSpacingY, voxelSpacingZ} );

        // TODO: change with replace and re-centre
        final BdvImageViewer viewer = BdvService.imageNameToBdv.get( inputImage.getName() );
        if ( viewer != null ) viewer.close();

        BigDataProcessor2.showImage( inputImage );
    }
}
