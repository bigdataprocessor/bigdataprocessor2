import de.embl.cba.bigDataTools2.bigDataProcessorUI.BigDataProcessor;
import de.embl.cba.bigDataTools2.fileInfoSource.FileInfoConstants;
import de.embl.cba.bigDataTools2.viewers.ViewerUtils;
import ij.ImageJ;

public class TestIJ1ImageViewer
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
	}
}
