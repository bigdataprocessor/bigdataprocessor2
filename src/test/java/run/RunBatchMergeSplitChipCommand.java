package run;

import de.embl.cba.bdp2.tools.batch.LuxendoBatchMergeSplitChipCommand;
import net.imagej.ImageJ;

public class RunBatchMergeSplitChipCommand
{
	public static void main ( String... args )
	{
		final ImageJ ij = new ImageJ();
		ij.ui().showUI();

		// invoke the plugin
		ij.command().run( LuxendoBatchMergeSplitChipCommand.class, true );
	}
}