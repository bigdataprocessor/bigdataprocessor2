package users.maxim;

import de.embl.cba.bdp2.tools.OpenMultipleImagesInBdvCommand;
import net.imagej.ImageJ;

import java.io.File;

public class ExploreXRayDataAndSBEM
{
	public static void main(final String... args)
	{
		final ImageJ ij = new ImageJ();
		ij.ui().showUI();

		final File[] files = new File[ 2 ];
		files[ 0 ] = new File( "/Volumes/cba/exchange/maxim/ver2/Platy-88_15_tomo.xml" );

		//files[ 0 ] = new File( "/Volumes/cba/exchange/maxim/ver2/transformed/Platy-88_01_tomo-transformed.xml" );
		files[ 1 ] = new File( "/Volumes/arendt/EM_6dpf_segmentation/platy-browser-data/data/rawdata/sbem-6dpf-1-whole-raw.xml" );

		final OpenMultipleImagesInBdvCommand command = new OpenMultipleImagesInBdvCommand();
		command.inputFiles = files;

		command.run();
	}
}
