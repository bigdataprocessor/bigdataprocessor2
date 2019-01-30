import de.embl.cba.bigDataTools2.dataStreamingGUI.BigDataConverter;
import de.embl.cba.bigDataTools2.fileInfoSource.FileInfoConstants;
import de.embl.cba.bigDataTools2.fileInfoSource.FileInfoSource;
import de.embl.cba.bigDataTools2.viewers.ViewerUtils;
import ij.IJ;
import ij.ImageJ;
import ij.io.FileInfo;
import ij.io.TiffDecoder;

import java.io.File;
import java.io.IOException;

public class ImageCalibrationBdvVisualisationTest
{
	public static void main( String[] args ) throws IOException
	{
		new ImageJ();

		final File file = new File(
				TestBdvViewer.class.getResource( "nc1-nt1-calibrated-tiff" ).getFile() );

		BigDataConverter bigDataConverter = new BigDataConverter();

		String imageDirectory = file.toString();

		bigDataConverter.openFromDirectory(
				imageDirectory.toString(),
				FileInfoConstants.SINGLE_CHANNEL_TIMELAPSE,
				".*",
				null,
				true,
				ViewerUtils.getImageViewer( ViewerUtils.BIG_DATA_VIEWER ) );
	}
}
