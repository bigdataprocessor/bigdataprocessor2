package run;

import ij.plugin.frame.Recorder;
import net.imagej.ImageJ;
import de.embl.cba.bdp2.scijava.command.*;

public class RunOpenCommand
{
	public static void main ( String... args )
	{
		final ImageJ ij = new ImageJ();
		ij.ui().showUI();

		new Recorder();

		Services.commandService = ij.command();

		ij.command().run( OpenCommand.class, true );
	}
}