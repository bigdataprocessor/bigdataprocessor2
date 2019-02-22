import de.embl.cba.bigDataTools2.fileInfoSource.FileInfoConstants;
import de.embl.cba.bigDataTools2.fileInfoSource.FileInfoSource;
import ij.IJ;
import ij.ImageJ;
import ij.io.FileInfo;
import ij.io.TiffDecoder;

import java.io.File;
import java.io.IOException;

public class ImageCalibrationTiffReaderTest
{
	public static void main( String[] args ) throws IOException
	{
		new ImageJ();

		final File file = new File(
				TestBdvViewer.class.getResource( "nc1-nt1-calibrated-tiff" ).getFile() );

		IJ.openImage( file.toString() + "/mri-stack.tif" ).show();

		final TiffDecoder tiffDecoder = new TiffDecoder(
				file.toString(), "mri-stack.tif" );
		final FileInfo[] tiffInfo = tiffDecoder.getTiffInfo();

		final FileInfoSource fileInfoSource = new FileInfoSource(
				file.toString(),
				FileInfoConstants.SINGLE_CHANNEL_TIMELAPSE,
				".*",
				"" );

//		BigDataProcessor bigDataProcessor = new BigDataProcessor();
//
//		String imageDirectory = TestBdvViewer.class.getResource( "tiff-nc1-nt2"  ).getFile().toString();
//
//		bigDataProcessor.openFromDirectory(
//				imageDirectory.toString(),
//				FileInfoConstants.SINGLE_CHANNEL_TIMELAPSE,
//				".*",
//				null,
//				ViewerUtils.getImageViewer( ViewerUtils.IJ1_VIEWER ) );
	}
}
