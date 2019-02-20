import de.embl.cba.bigDataTools2.bigDataConverterUI.BigDataConverterCommand;
import net.imagej.ImageJ;

public class RunBigDataConverterCommand
{
	public static void main ( String... args )
	{
		final ImageJ ij = new ImageJ();
		ij.ui().showUI();

		// invoke the plugin
		ij.command().run( BigDataConverterCommand.class, true );
	}
}