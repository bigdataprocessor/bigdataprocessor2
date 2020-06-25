package users.maxim;

import de.embl.cba.bdp2.tools.OpenMultipleImagesInBdvCommand;
import net.imagej.ImageJ;

import java.io.File;

import static de.embl.cba.bdv.utils.FileUtils.getFileList;

public class ExploreAllXRayData
{
	public static void main(final String... args)
	{
		final ImageJ ij = new ImageJ();
		ij.ui().showUI();

		final OpenMultipleImagesInBdvCommand command = new OpenMultipleImagesInBdvCommand();
		command.inputFiles = getFileList( new File( "/Volumes/cba/exchange/maxim/ver2" ), ".*.xml", false ).toArray( new File[]{} );

		command.run();
	}
}
