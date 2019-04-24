package de.embl.cba.bdp2.process;

import de.embl.cba.bdp2.fileinfosource.FileInfoConstants;
import static de.embl.cba.bdp2.ui.BigDataProcessorCommand.logger;
import de.embl.cba.bdp2.ui.BdvMenus;
import de.embl.cba.bdp2.ui.BigDataProcessor;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.bdp2.viewers.ImageViewer;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class CroppedView < T extends RealType< T > & NativeType< T > >
{
	public CroppedView( ImageViewer< T > imageViewer )
	{
		FinalInterval interval = imageViewer.get5DIntervalFromUser();

		if (interval != null) {
			RandomAccessibleInterval croppedRAI = BigDataProcessor.crop( imageViewer.getRai(), interval);
			ImageViewer newImageViewer = imageViewer.newImageViewer();
			newImageViewer.show(
					croppedRAI,
					FileInfoConstants.CROPPED_VIEW_NAME,
					imageViewer.getVoxelSize(),
					imageViewer.getCalibrationUnit(),
					false );
			logger.info( "Cropped view size [GB]: "
					+ Utils.getSizeGB( croppedRAI ) );
			BdvMenus menus = new BdvMenus();
			newImageViewer.addMenus(menus);
			imageViewer.replicateViewerContrast(newImageViewer);
		}

	}
}
