import de.embl.cba.bigDataTools2.dataStreamingGUI.DataStreamingTools;
import de.embl.cba.bigDataTools2.fileInfoSource.FileInfoConstants;
import de.embl.cba.bigDataTools2.saving.SavingSettings;
import de.embl.cba.bigDataTools2.viewers.ViewerUtils;
import ij.ImageJ;

public class TestIJ1ViewerSaving
{
	public static void main( String[] args )
	{
		new ImageJ();

		DataStreamingTools dataStreamingTools = new DataStreamingTools();

		String imageDirectory = TestBdvViewer.class.getResource( "tiff-nc1-nt2"  ).getFile().toString();

		dataStreamingTools.openFromDirectory(
				imageDirectory.toString(),
				FileInfoConstants.SINGLE_CHANNEL_TIMELAPSE,
				".*",
				null,
				ViewerUtils.getImageViewer( ViewerUtils.IJ1_VIEWER ) );


		final SavingSettings savingSettings = new SavingSettings();

		savingSettings.compression = SavingSettings.NONE;
		savingSettings.bin = "1,1,1"; // TODO: is this correct?
		savingSettings.saveVolume = true;
		savingSettings.saveProjection = false;
		savingSettings.convertTo8Bit = false;
		savingSettings.convertTo16Bit = false;
		savingSettings.gate = false;
		savingSettings.filePath = "/Users/tischer/Desktop/bc-saving/im";
		savingSettings.fileType = SavingSettings.FileType.TIFF_as_STACKS;


		DataStreamingTools.saveImage( savingSettings );


	}
}
