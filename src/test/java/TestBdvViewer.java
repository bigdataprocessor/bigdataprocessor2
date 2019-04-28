import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.ui.BigDataProcessor2;
import de.embl.cba.bdp2.viewers.ViewerUtils;

public class TestBdvViewer
{
	public static void main( String[] args )
	{
		BigDataProcessor2 bigDataProcessor2 = new BigDataProcessor2();

		String imageDirectory = TestBdvViewer.class.getResource( "tiff-nc1-nt2"  ).getFile().toString();

		bigDataProcessor2.openFromDirectory(
<<<<<<< HEAD
				imageDirectory.toString(),
				FileInfos.SINGLE_CHANNEL_TIMELAPSE,
=======
				imageDirectory,
				FileInfoConstants.SINGLE_CHANNEL_TIMELAPSE,
>>>>>>> 2202dde07b585e396be413df004a30ec11a7df68
				".*",
				true,
				ViewerUtils.getImageViewer( ViewerUtils.BIG_DATA_VIEWER ) );
	}
}
