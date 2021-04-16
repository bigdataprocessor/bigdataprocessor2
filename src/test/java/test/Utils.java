package test;

import de.embl.cba.bdp2.BigDataProcessor2UI;
import de.embl.cba.bdp2.scijava.Services;
import loci.common.DebugTools;
import net.imagej.ImageJ;

public class Utils
{
	public static void prepareInteractiveMode()
	{
		DebugTools.setRootLevel("OFF"); // Bio-Formats
		ImageJ imageJ = new ImageJ();
		imageJ.ui().showUI();
		Services.setContext( imageJ.getContext() );
		Services.setCommandService( imageJ.command() );
		BigDataProcessor2UI.showUI();
	}
}
