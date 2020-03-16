package headless;

import de.embl.cba.bdp2.scijava.command.LuxendoBatchMergeSplitChipCommand;
import net.imagej.ImageJ;

public class TestBatchMergeSplitChipCommand
{
	public static void main ( String... args )
	{
		final ImageJ ij = new ImageJ();
		ij.ui().showUI();

		final LuxendoBatchMergeSplitChipCommand command = new LuxendoBatchMergeSplitChipCommand();
		command.test();
	}
}