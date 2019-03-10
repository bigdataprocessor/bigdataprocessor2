import de.embl.cba.bdp2.CachedCellImageCreator;
import de.embl.cba.bdp2.fileinfosource.FileInfoSource;
import de.embl.cba.bdp2.ui.BigDataProcessor;
import de.embl.cba.bdp2.fileinfosource.FileInfoConstants;
import de.embl.cba.bdp2.saving.SavingSettings;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import de.embl.cba.bdp2.viewers.ImageViewer;
import de.embl.cba.bdp2.viewers.ViewerUtils;
import ij.ImageJ;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.type.numeric.integer.UnsignedShortType;

public class TestIJ1ViewerSaving
{
	//TODO: reinstate this when IJ1Viewer is active.
	public static void main( String[] args ){
//		new ImageJ();
//
//		BigDataProcessor bigDataProcessor = new BigDataProcessor();
//
//		String imageDirectory = TestBdvViewer.class.getResource( "tiff-nc1-nt2"  ).getFile().toString();
//
//		final FileInfoSource fileInfoSource = new FileInfoSource(imageDirectory, FileInfoConstants.SINGLE_CHANNEL_TIMELAPSE,
//				".*", "");
//		CachedCellImg cachedCellImg = CachedCellImageCreator.create(fileInfoSource, null);

//		ImageViewer imageViewer = new IJ1ImageViewer<UnsignedShortType>(
//				cachedCellImg,
//				"input",
//				new double[]{1.0, 1.0, 1.0},
//				"pixel");
//		imageViewer.show();
//		imageViewer.setDisplayRange(0, 800, 0);

//      final SavingSettings savingSettings = SavingSettings.getDefaults();
//		savingSettings.voxelSize =imageViewer.getVoxelSize();
//		savingSettings.unit = imageViewer.getCalibrationUnit();
//		BigDataProcessor.saveImage( savingSettings, bigDataProcessor.getImageViewer() );
	}
}
