package explore;

import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.ui.BigDataProcessor2;
import de.embl.cba.bdp2.viewers.ImageViewer;

public class ExploreCropFreeze
{
	public static void main( String[] args )
	{
		BigDataProcessor2 bigDataProcessor2 = new BigDataProcessor2();

		String imageDirectory =
				ExploreCropFreeze.class
						.getResource( "/nc1-nt3-calibrated-tiff"  ).getFile();

		final Image image = bigDataProcessor2.openTiffData(
				imageDirectory,
				FileInfos.SINGLE_CHANNEL_TIMELAPSE,
				".*" );

		final ImageViewer viewer = bigDataProcessor2.showImage( image );

		viewer.get5DIntervalFromUser();
	}
}
