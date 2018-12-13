package de.embl.cba.bigDataToolViewerIL2.imaris;

import de.embl.cba.bigDataToolViewerIL2.utils.Utils;
import ij.ImagePlus;
import org.scijava.ItemVisibility;
import org.scijava.command.Command;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import java.io.File;


@Plugin( type = Command.class, menuPath = "Plugins>BigDataTools>Imaris Writer" )
public class ImarisWriterCommand implements Command
{
	@Parameter
	public LogService logService;

	@Parameter( visibility = ItemVisibility.MESSAGE, persist = false )
	private String information1 = "Creates an Imaris readable file with channels and time-points as separate files.";

	@Parameter
	public ImagePlus imagePlus;

	@Parameter( label = "Directory", style = "directory" )
	public File directory;

	@Parameter( label = "Binning [ x, y, z ]")
	public String binningString = "1,1,1";

	// TODO: multi-threaded writing, using multiple IJ instances
	// TODO: multi-threaded writing, using the cluster
	// TODO: conversion to 8-bit

	@Override
	public void run()
	{
		if ( ! isInputValid() ) return;

		ImarisWriter writer = new ImarisWriter( imagePlus, directory.getAbsolutePath() );

		writer.setLogService( logService );

		setBinning( writer );

		writer.write();
	}

	private void setBinning( ImarisWriter writer )
	{
		final int[] binning = Utils.delimitedStringToIntegerArray( binningString, "," );
		writer.setBinning( binning );
	}

	private boolean isInputValid()
	{
		return true;
	}


}