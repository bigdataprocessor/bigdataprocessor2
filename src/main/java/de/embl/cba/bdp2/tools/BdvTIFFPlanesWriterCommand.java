/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2021 EMBL
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

import de.embl.cba.bdv.utils.io.BdvImagePlusExport;
import ij.ImagePlus;
import ij.plugin.FolderOpener;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.io.File;


@Plugin(type = Command.class, menuPath = "Plugins>BigDataTools>Convert TIFF Slices to XML/HDF5")
public class BdvTIFFPlanesWriterCommand implements Command
{
	@Parameter( label = "Input folder", style = "directory, open" )
	public File inputFolder;

	@Parameter( label = "Output file path (.xml)", style = "save" )
	public File xmlOutputPath;

	@Parameter( label = "Voxel unit" )
	public String voxelUnit = "micrometer";

	@Parameter( label = "Voxel size X" )
	public Double voxelSizeX;

	@Parameter( label = "Voxel size Y" )
	public Double voxelSizeY;

	@Parameter( label = "Voxel size Z" )
	public Double voxelSizeZ;

	@Override
	public void run()
	{
		final ImagePlus imp = openVirtualImagePlus();
		BdvImagePlusExport.saveAsBdv( imp, xmlOutputPath );
	}

	private ImagePlus openVirtualImagePlus()
	{
		final FolderOpener folderOpener = new FolderOpener();
		folderOpener.openAsVirtualStack( true );
		ImagePlus imp = folderOpener.openFolder( inputFolder.getAbsolutePath() );

		imp.getCalibration().pixelWidth = voxelSizeX;
		imp.getCalibration().pixelHeight = voxelSizeY;
		imp.getCalibration().pixelDepth = voxelSizeZ;
		imp.getCalibration().setUnit( voxelUnit );

		return imp;
	}
}
