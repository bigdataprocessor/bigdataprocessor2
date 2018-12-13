import de.embl.cba.bigDataToolViewerIL2.dataStreamingGUI.LazyLoadingCommand;
import net.imagej.ImageJ;
import org.scijava.command.Command;
import org.scijava.command.DynamicCommand;
import org.scijava.command.Interactive;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.widget.Button;

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