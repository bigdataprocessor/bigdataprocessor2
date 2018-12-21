import de.embl.cba.bigDataTools2.dataStreamingGUI.DataStreamingTools;
import de.embl.cba.bigDataTools2.fileInfoSource.FileInfoConstants;
import de.embl.cba.bigDataTools2.viewers.ViewerUtils;
import ij.ImageJ;

public class TestIJ1ImageViewer
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
	}
}
