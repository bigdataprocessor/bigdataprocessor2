package develop;

import loci.common.services.ServiceFactory;
import loci.formats.IFormatReader;
import loci.formats.ImageReader;
import loci.formats.meta.IMetadata;
import loci.formats.services.OMEXMLService;

import java.io.File;

public class DevelopReadOmeCompanionFile
{
	public static void main( String[] args )
	{
		readImageCalibrationWithBioFormats( new File( "/Users/tischer/Downloads/OME.Tiff/Position 1_Settings 1/ome-tiff.companion.ome") );
	}

	public static void readImageCalibrationWithBioFormats( File file )
	{
		try
		{
			ServiceFactory factory = new ServiceFactory();
			OMEXMLService service = factory.getInstance( OMEXMLService.class );
			IMetadata meta = service.createOMEXMLMetadata();

			// create format reader
			IFormatReader reader = new ImageReader();
			reader.setMetadataStore( meta );

			// initialize file
			reader.setId( file.getAbsolutePath() );
			reader.setSeries(0);

			// read calibration
			String unit = meta.getPixelsPhysicalSizeX( 0 ).unit().getSymbol();
			final double value = meta.getPixelsPhysicalSizeX( 0 ).value().doubleValue();
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
	}
}
