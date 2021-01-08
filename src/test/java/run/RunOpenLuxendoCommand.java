package run;

import de.embl.cba.bdp2.open.fileseries.OpenLuxendoHDF5SeriesCommand;
import ij.plugin.frame.Recorder;
import net.imagej.ImageJ;

public class RunOpenLuxendoCommand
{
	public static void main ( String... args )
	{
		final ImageJ ij = new ImageJ();
		ij.ui().showUI();

		new Recorder();

		ij.command().run( OpenLuxendoHDF5SeriesCommand.class, true );
	}
}