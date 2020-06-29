package de.embl.cba.bdp2.tools;

import de.embl.cba.bdv.utils.io.BdvImagePlusExport;
import ij.ImagePlus;
import ij.plugin.FolderOpener;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.io.File;


@Plugin(type = Command.class, menuPath = "Plugins>BigDataTools>Convert Tiff Slices to XML/HDF5")
public class BdvTiffPlanesWriterCommand implements Command
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
