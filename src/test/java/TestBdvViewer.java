import de.embl.cba.bdp2.ui.BigDataProcessor;
import de.embl.cba.bdp2.fileinfosource.FileInfoConstants;
import de.embl.cba.bdp2.viewers.ViewerUtils;

public class TestBdvViewer
{
	public static void main( String[] args )
	{
		BigDataProcessor bigDataProcessor = new BigDataProcessor();

		String imageDirectory = TestBdvViewer.class.getResource( ""  ).toString();

		bigDataProcessor.openFromDirectory(
				imageDirectory.toString(),
				FileInfoConstants.SINGLE_CHANNEL_TIMELAPSE,
				".*",
				null,
				true,
				ViewerUtils.getImageViewer( ViewerUtils.BIG_DATA_VIEWER ) );
	}
}
