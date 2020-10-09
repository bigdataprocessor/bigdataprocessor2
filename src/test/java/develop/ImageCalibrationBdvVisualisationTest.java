package develop;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.open.NamingSchemes;
import ij.ImageJ;

import java.io.File;
import java.io.IOException;

public class ImageCalibrationBdvVisualisationTest
{
	public static void main( String[] args ) throws IOException
	{
		new ImageJ();

		final File file = new File(
				ImageCalibrationBdvVisualisationTest.class.getResource( "nc1-nt1-calibrated-tiff" ).getFile() );

		String imageDirectory = file.toString();

		final Image image = BigDataProcessor2.openImage(
				imageDirectory.toString(),
				NamingSchemes.SINGLE_CHANNEL_TIMELAPSE,
				".*" );
	}
}
