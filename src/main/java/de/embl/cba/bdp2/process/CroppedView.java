package de.embl.cba.bdp2.process;

import de.embl.cba.bdp2.RaiPlus;

import static de.embl.cba.bdp2.ui.BigDataProcessorCommand.logger;
import de.embl.cba.bdp2.ui.BdvMenus;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.bdp2.viewers.ImageViewer;
import net.imglib2.FinalInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

public class CroppedView < T extends RealType< T > & NativeType< T > >
{
	// TODO: better split UI
	public CroppedView( ImageViewer< T > imageViewer )
	{
		logger.info( "\nCropping..." );
		FinalInterval interval = imageViewer.get5DIntervalFromUser();
		final RaiPlus< T > raiPlus = imageViewer.getRaiPlus();

		if (interval != null) {

			RaiPlus< T > cropped = crop( raiPlus, interval );

			ImageViewer newImageViewer = imageViewer.newImageViewer();
			newImageViewer.show( cropped, false );
			logger.info( "Cropped view size [GB]: " + Utils.getSizeGB( cropped.getRai() ) );
			BdvMenus menus = new BdvMenus();
			newImageViewer.addMenus(menus);
			imageViewer.replicateViewerContrast(newImageViewer);
		}

	}

	public static < T extends RealType< T > & NativeType< T > >
	RaiPlus< T > crop( RaiPlus< T > raiPlus, FinalInterval interval )
	{
		Views.zeroMin( Views.interval( raiPlus.getRai(), interval ) );

		return new RaiPlus<>(
				Views.zeroMin( Views.interval( raiPlus.getRai(), interval ) ),
				raiPlus.getName(),
				raiPlus.getVoxelSize(),
				raiPlus.getVoxelSizeUnit()
		);
	}
}
