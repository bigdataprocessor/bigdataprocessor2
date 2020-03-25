package run;

import de.embl.cba.bdp2.open.command.OpenLeicaDSLTiffPlanesCommand;
import de.embl.cba.bdp2.scijava.Services;
import ij.plugin.frame.Recorder;
import net.imagej.ImageJ;

public class RunOpenLeicaDSLTiffPlanesCommand
{
	public static void main ( String... args )
	{
		final ImageJ ij = new ImageJ();
		ij.ui().showUI();

		new Recorder();

		Services.commandService = ij.command();

		ij.command().run( OpenLeicaDSLTiffPlanesCommand.class, true );
	}
}