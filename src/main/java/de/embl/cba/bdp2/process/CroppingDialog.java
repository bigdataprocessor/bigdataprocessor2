package de.embl.cba.bdp2.process;

import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.logging.Logger;
import de.embl.cba.bdp2.ui.BdvMenus;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.bdp2.viewers.ImageViewer;
import net.imglib2.FinalInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class CroppingDialog< T extends RealType< T > & NativeType< T > >
{
	public CroppingDialog( ImageViewer< T > imageViewer )
	{
		Logger.info( "\nCropping..." );
		FinalInterval interval = imageViewer.get5DIntervalFromUser();
		final Image< T > image = imageViewer.getImage();

		if (interval != null) {
			Image< T > cropped = Cropper.crop( image, interval );
			ImageViewer newImageViewer = imageViewer.newImageViewer();
			newImageViewer.show( cropped, false );
			Logger.info( "Cropped view size [GB]: " + Utils.getSizeGB( cropped.getRai() ) );
			BdvMenus menus = new BdvMenus();
			newImageViewer.addMenus(menus);
			imageViewer.replicateViewerContrast(newImageViewer);
		}

	}

}
