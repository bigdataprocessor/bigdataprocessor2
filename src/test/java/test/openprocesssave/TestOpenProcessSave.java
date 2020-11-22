package test.openprocesssave;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import org.jfree.chart.ui.Align;

import java.util.ArrayList;

public class TestOpenProcessSave
{
	public static void main( String[] args )
	{

	}

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
	}
}
