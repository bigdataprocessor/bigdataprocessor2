/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2023 EMBL
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package de.embl.cba.bdp2.tools;

import de.embl.cba.bdv.utils.io.BdvToVoxelGridImageConverter;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.widget.FileWidget;

import java.io.File;

@Plugin(type = Command.class, menuPath = "Plugins>BigDataTools>Convert XML/HDF5 to Single Resolution Image")
public class BdvToVoxelGridImageCommand< T extends RealType< T > & NativeType< T > > implements Command
{
	@Parameter( label = "XML/HDF5 source image", style = FileWidget.OPEN_STYLE )
	public File bdvSourceImage;

	@Parameter( label = "XML/HDF5 reference, providing target image for voxel and image size", style = FileWidget.OPEN_STYLE )
	public File bdvReferenceImage;

	@Parameter( label = "Output file", style = FileWidget.SAVE_STYLE )
	public File outputPath;

	// TODO: replace by enum, once possible
	@Parameter( label = "Output file format", choices = { "TIFF", "Bdv" } )
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
