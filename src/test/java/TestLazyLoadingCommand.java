import de.embl.cba.bigDataTools2.dataStreamingGUI.LazyLoadingCommand;
import net.imagej.ImageJ;

public class TestLazyLoadingCommand
{
	public static void main ( String... args )
	{
		final ImageJ ij = new ImageJ();
		ij.ui().showUI();

		// invoke the plugin
		ij.command().run( LazyLoadingCommand.class, true );
	}
}