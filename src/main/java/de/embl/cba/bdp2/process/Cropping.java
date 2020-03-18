package de.embl.cba.bdp2.process;

import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.logging.Logger;
import de.embl.cba.bdp2.ui.BdvMenus;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.bdp2.viewers.ImageViewer;
import net.imglib2.FinalInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

public class Cropping< T extends RealType< T > & NativeType< T > >
{
	// TODO: better split UI
	public Cropping( ImageViewer< T > imageViewer )
	{
		// Logger.info( "\nCropping..." );
		FinalInterval interval = imageViewer.get5DIntervalFromUser();
		final Image< T > image = imageViewer.getImage();

		if (interval != null) {

			Image< T > cropped = crop( image, interval );

			ImageViewer newImageViewer = imageViewer.newImageViewer();
			newImageViewer.show( cropped, false );
			Logger.info( "Cropped view size [GB]: " + Utils.getSizeGB( cropped.getRai() ) );
			BdvMenus menus = new BdvMenus();
			newImageViewer.addMenus(menus);
			imageViewer.replicateViewerContrast(newImageViewer);
		}

	}

	public static < T extends RealType< T > & NativeType< T > >
	Image< T > crop( Image< T > image, FinalInterval interval )
	{
		Views.zeroMin( Views.interval( image.getRai(), interval ) );

		return new Image<>(
				Views.zeroMin( Views.interval( image.getRai(), interval ) ),
				image.getName(),
				image.getVoxelSpacing(),
				image.getVoxelUnit()
		);
	}
}
