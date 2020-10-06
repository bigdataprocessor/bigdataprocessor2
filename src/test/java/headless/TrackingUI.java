package headless;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.open.core.NamingSchemes;
import de.embl.cba.bdp2.viewers.ImageViewer;
import net.imagej.ImageJ;

public class TrackingUI
{
	public static void main( String[] args )
	{
		final ImageJ imageJ = new ImageJ();
		imageJ.ui().showUI();

		String imageDirectory = "/Users/tischer/Documents/fiji-plugin-bigDataProcessor2/" +
				"src/test/resources/test-data/microglia-drift-nt123/volumes";

		final Image image = BigDataProcessor2.openImage(
				imageDirectory,
				NamingSchemes.SINGLE_CHANNEL_TIMELAPSE,
				".*" );

		image.setVoxelUnit( "pixel" );
		image.setVoxelSize( 1.0, 1.0, 1.0 );

		final ImageViewer viewer = BigDataProcessor2.showImage( image);
		viewer.setDisplaySettings( 0, 150,0 );
	}

}
