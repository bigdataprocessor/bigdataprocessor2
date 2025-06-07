/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2025 EMBL
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
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
