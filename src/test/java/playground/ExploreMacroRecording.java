package playground;

import de.embl.cba.bdp2.open.OpenAdvancedCommand;
import ij.plugin.frame.Recorder;
import net.imagej.ImageJ;

public class ExploreMacroRecording
{
	public static void main( String[] args )
	{
		final ImageJ ij = new ImageJ();
		ij.ui().showUI();

		final Recorder recorder = new Recorder();

		// invoke the plugin
		ij.command().run( OpenAdvancedCommand.class, true );
	}
}
