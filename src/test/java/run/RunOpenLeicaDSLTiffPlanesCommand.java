package run;

import de.embl.cba.bdp2.open.OpenAdvancedCommand;
import de.embl.cba.bdp2.open.OpenLuxendoChannelsFromFoldersCommand;
import de.embl.cba.bdp2.scijava.Services;
import ij.plugin.frame.Recorder;
import net.imagej.ImageJ;

public class RunOpenLuxendoChannelsCommand
{
	public static void main ( String... args )
	{
		final ImageJ ij = new ImageJ();
		ij.ui().showUI();

		new Recorder();

		Services.commandService = ij.command();

		ij.command().run( OpenLuxendoChannelsFromFoldersCommand.class, true );
	}
}