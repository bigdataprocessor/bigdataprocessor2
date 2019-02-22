import de.embl.cba.bigDataTools2.bigDataProcessorUI.BigDataProcessorCommand;
import net.imagej.ImageJ;

public class RunBigDataProcessorCommand
{
	public static void main ( String... args )
	{
		final ImageJ ij = new ImageJ();
		ij.ui().showUI();

		// invoke the plugin
		ij.command().run( BigDataProcessorCommand.class, true );
	}
}