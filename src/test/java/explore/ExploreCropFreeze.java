package explore;

import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.logging.Logger;
import de.embl.cba.bdp2.ui.BigDataProcessor2;
import de.embl.cba.bdp2.viewers.ImageViewer;
import net.imagej.ImageJ;

import javax.swing.*;

public class ExploreCropFreeze
{
	public static void main( String[] args )
	{
		final ImageJ imageJ = new ImageJ();
		imageJ.ui().showUI();

		BigDataProcessor2 bigDataProcessor2 = new BigDataProcessor2();

		String imageDirectory =
				ExploreCropFreeze.class
						.getResource( "/nc1-nt3-calibrated-tiff"  ).getFile();

		final Image image = bigDataProcessor2.openTiffData(
				imageDirectory,
				FileInfos.SINGLE_CHANNEL_TIMELAPSE,
				".*" );

		final ImageViewer viewer = bigDataProcessor2.showImage( image );

		new Thread( () ->
		{
			viewer.get5DIntervalFromUser();
		}).start();
	}
}
