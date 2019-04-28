import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.ui.BigDataProcessor2;
import de.embl.cba.bdp2.viewers.ViewerUtils;
import ij.ImageJ;

public class TestIJ1ImageViewer
{
	public static void main( String[] args )
	{
		new ImageJ();

		BigDataProcessor2 bigDataProcessor2 = new BigDataProcessor2();

		String imageDirectory = TestBdvViewer.class.getResource( "tiff-nc1-nt2"  ).getFile().toString();

		bigDataProcessor2.openFromDirectory(
				imageDirectory.toString(),
				FileInfos.SINGLE_CHANNEL_TIMELAPSE,
				".*",
				null,
				true,
				ViewerUtils.getImageViewer( ViewerUtils.IJ1_VIEWER ) );
	}
}
