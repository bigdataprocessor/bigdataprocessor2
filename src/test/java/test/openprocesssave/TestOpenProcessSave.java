package test.openprocesssave;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.log.progress.LoggingProgressListener;
import de.embl.cba.bdp2.save.SaveFileType;
import de.embl.cba.bdp2.save.SavingSettings;
import org.jfree.chart.ui.Align;
import org.junit.Test;

import java.util.ArrayList;

public class TestOpenProcessSave
{
	public static void main( String[] args )
	{
		new TestOpenProcessSave().run();
	}

	@Test
	public void run()
	{
		Image image = BigDataProcessor2.openTiffSeries( "/Users/tischer/Documents/bigdataprocessor2/src/test/resources/test/tiff-nc2-nt2-16bit", ".*--C(?<C>\\d+)--T(?<T>\\d+).tif" );

		image = BigDataProcessor2.setVoxelSize( image, new double[]{2.0,2.0,2.0}, "µm" );

		BigDataProcessor2.rename( image, "image", new String[]{"ch0","ch1"} );

		ArrayList< long[] > shiftsXYZC = new ArrayList< long[] >();
		shiftsXYZC.add( new long[]{0,13,0,0} );
		shiftsXYZC.add( new long[]{0,1,5,0} );
		image = BigDataProcessor2.alignChannels( image, shiftsXYZC );

		image = BigDataProcessor2.bin( image, new long[]{2,2,1,1,1} );
		image.setName( "image-binned" );

		image = BigDataProcessor2.convertToUnsignedByteType( image, new double[]{173.0,103.0}, new double[]{445.0,259.0} );
		image.setName( "image-binned-8bit" );

		image = BigDataProcessor2.crop( image, new long[]{5,24,0,0,0,47,58,82,1,1} );
		image.setName( "image-binned-8bit-crop" );

		final SavingSettings settings = SavingSettings.getDefaults();
		settings.volumesFilePathStump = "src/test/resources/test/output/imaris/" + image.getName();
		settings.image = image;
		settings.fileType = SaveFileType.ImarisVolumes;
		settings.numProcessingThreads = 4;
		settings.numIOThreads = 1;
		settings.compression = SavingSettings.COMPRESSION_NONE;
		settings.tStart = 0;
		settings.tEnd = image.getNumTimePoints() - 1;

		Logger.setLevel( Logger.Level.Debug );
		BigDataProcessor2.saveImage( image, settings, new LoggingProgressListener( "Files saved" ) );

	}
}
