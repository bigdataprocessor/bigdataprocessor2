package headless;

import de.embl.cba.bdp2.command.BatchMergeSplitChipCommand;
import net.imagej.ImageJ;

public class TestBatchMergeSplitChipCommand
{
	public static void main ( String... args )
	{
		final ImageJ ij = new ImageJ();
		ij.ui().showUI();

		// invoke the plugin
		final BatchMergeSplitChipCommand command = new BatchMergeSplitChipCommand();
		command.test();
	}

}