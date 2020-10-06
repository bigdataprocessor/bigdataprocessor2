package run;

import de.embl.cba.bdp2.open.ui.OpenCustomCommand;
import de.embl.cba.bdp2.scijava.Services;
import ij.plugin.frame.Recorder;
import net.imagej.ImageJ;

public class RunOpenCommand
{
	public static void main ( String... args )
	{
		final ImageJ ij = new ImageJ();
		ij.ui().showUI();

		new Recorder();

		Services.setCommandService( ij.command() );
		Services.getCommandService().run( OpenCustomCommand.class, true );
	}
}