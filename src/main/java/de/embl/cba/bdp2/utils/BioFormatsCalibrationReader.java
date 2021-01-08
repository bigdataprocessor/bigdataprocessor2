package de.embl.cba.bdp2.utils;

import loci.common.DebugTools;
import loci.common.services.ServiceFactory;
import loci.formats.IFormatReader;
import loci.formats.ImageReader;
import loci.formats.meta.IMetadata;
import loci.formats.services.OMEXMLService;

import java.io.File;

public class BioFormatsCalibrationReader
{
	private String unit;
	private double[] voxelSize;

	public BioFormatsCalibrationReader()
	{
	}

	public boolean readCalibration( File file )
	{
		DebugTools.setRootLevel("OFF");

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
			reader.setSeries( 0 );

			// read calibration
			unit = meta.getPixelsPhysicalSizeX( 0 ).unit().getSymbol();
			voxelSize = new double[ 3 ];
			voxelSize[ 0 ] = meta.getPixelsPhysicalSizeX( 0 ).value().doubleValue();
			voxelSize[ 1 ] = meta.getPixelsPhysicalSizeY( 0 ).value().doubleValue();
			voxelSize[ 2 ] = meta.getPixelsPhysicalSizeZ( 0 ).value().doubleValue();
			return true;
		}
		catch ( Exception e )
		{
			e.printStackTrace();
			return false;
		}
	}

	public String getUnit()
	{
		return unit;
	}

	public double[] getVoxelSize()
	{
		return voxelSize;
	}
}
