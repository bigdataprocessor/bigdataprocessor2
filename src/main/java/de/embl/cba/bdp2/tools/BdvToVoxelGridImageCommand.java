package de.embl.cba.bdp2.tools;

import de.embl.cba.bdv.utils.io.BdvToVoxelGridImageConverter;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.widget.FileWidget;

import java.io.File;

@Plugin(type = Command.class,
		menuPath = "Plugins>BigDataTools>Convert XML/HDF5 to Single Resolution Image")
public class BdvToVoxelGridImageCommand< T extends RealType< T > & NativeType< T > >
		implements Command
{

	@Parameter( label = "XML/HDF5 source image", style = FileWidget.OPEN_STYLE )
	public File bdvSourceImage;

	@Parameter( label = "XML/HDF5 reference, providing target image for voxel and image size", style = FileWidget.OPEN_STYLE )
	public File bdvReferenceImage;

	@Parameter( label = "Output file", style = FileWidget.SAVE_STYLE )
	public File outputPath;

	// TODO: replace by enum, once possible
	@Parameter( label = "Output file format", choices = { "Tiff", "Bdv" } )
	public String outputFileFormat;

	// TODO: replace by enum, once possible
	@Parameter( label = "Interpolation type", choices = { "NearestNeighbor", "NLinear"} )
	public String interpolationType;

	@Override
	public void run()
	{
		String outputPathWithoutExtension = outputPath.toString().substring(0, outputPath.toString().lastIndexOf('.'));

		final BdvToVoxelGridImageConverter< T > converter = new BdvToVoxelGridImageConverter<>(
				bdvReferenceImage.getAbsolutePath(),
				bdvSourceImage.getAbsolutePath(),
				BdvToVoxelGridImageConverter.InterpolationType.valueOf( interpolationType ) );

		converter.run(
				BdvToVoxelGridImageConverter.FileFormat.valueOf( outputFileFormat ),
				outputPathWithoutExtension );
	}

}
