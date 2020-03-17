package explore;

import de.embl.cba.bdp2.scijava.command.SimpleCommand;
import ij.plugin.frame.Recorder;
import net.imagej.ImageJ;

public class ExploreSimpleCommand
{
	public static void main( String[] args )
	{
		final ImageJ imageJ = new ImageJ();
		imageJ.ui().showUI();

		new Recorder();

		imageJ.command().run( SimpleCommand.class, true, "binWidthXYPixels", 3 );
	}
}
