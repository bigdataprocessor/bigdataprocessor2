import de.embl.cba.bigDataTools2.dataStreamingGUI.DataStreamingTools;
import de.embl.cba.bigDataTools2.fileInfoSource.FileInfoConstants;
import de.embl.cba.bigDataTools2.viewers.ViewerUtils;

public class TestBdvViewer
{
	public static void main( String[] args )
	{
		DataStreamingTools dataStreamingTools = new DataStreamingTools();

		String imageDirectory = TestBdvViewer.class.getResource( ""  ).toString();

		dataStreamingTools.openFromDirectory(
				imageDirectory.toString(),
				FileInfoConstants.SINGLE_CHANNEL_TIMELAPSE,
				".*",
				null,
				ViewerUtils.getImageViewer( ViewerUtils.BIG_DATA_VIEWER ) );
	}
}
