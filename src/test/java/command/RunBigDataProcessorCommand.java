package command;

import de.embl.cba.bdp2.ui.BigDataProcessorCommand;
import net.imagej.ImageJ;

public class RunBigDataProcessorCommand
{
	public static void main ( String... args )
	{
		final ImageJ ij = new ImageJ();
		ij.ui().showUI();

		// invoke the plugin
		ij.command().run( BigDataProcessorCommand.class, true );
	}
}