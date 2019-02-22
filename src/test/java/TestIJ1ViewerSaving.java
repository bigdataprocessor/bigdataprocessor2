import de.embl.cba.bdp2.ui.BigDataProcessor;
import de.embl.cba.bdp2.fileinfosource.FileInfoConstants;
import de.embl.cba.bdp2.saving.SavingSettings;
import de.embl.cba.bdp2.viewers.ViewerUtils;
import ij.ImageJ;

public class TestIJ1ViewerSaving
{
	public static void main( String[] args )
	{
		new ImageJ();

		BigDataProcessor bigDataProcessor = new BigDataProcessor();

		String imageDirectory = TestBdvViewer.class.getResource( "tiff-nc1-nt2"  ).getFile().toString();

		bigDataProcessor.openFromDirectory(
				imageDirectory.toString(),
				FileInfoConstants.SINGLE_CHANNEL_TIMELAPSE,
				".*",
				null,
				true,
				ViewerUtils.getImageViewer( ViewerUtils.IJ1_VIEWER ) );


		/*final SavingSettings savingSettings = new SavingSettings();

		savingSettings.compression = SavingSettings.NONE;
		savingSettings.bin = "1,1,1"; // TODO: is this correct?
		savingSettings.saveVolume = true;
		savingSettings.saveProjection = false;
		savingSettings.convertTo8Bit = false;
		savingSettings.convertTo16Bit = false;
		savingSettings.gate = false;
		savingSettings.filePath = "/Users/tischer/Desktop/bc-saving/im";
		savingSettings.fileType = SavingSettings.FileType.TIFF_as_STACKS;
		*/
        final SavingSettings savingSettings = SavingSettings.getDefaults();

		BigDataProcessor.saveImage( savingSettings, bigDataProcessor.getImageViewer() );

	}
}
