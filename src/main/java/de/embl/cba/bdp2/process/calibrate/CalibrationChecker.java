/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2023 EMBL
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
package de.embl.cba.bdp2.process.calibrate;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.log.Logger;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import ome.units.quantity.Length;
import ome.units.unit.Unit;

public abstract class CalibrationChecker< R extends RealType< R > & NativeType< R > >
{
	public static < R extends RealType< R > & NativeType< R > > void correctCalibrationViaDialogIfNecessary( Image< R > inputImage )
	{
		if ( ! checkVoxelDimension( inputImage.getVoxelDimensions() ) || ! checkVoxelUnit( inputImage.getVoxelUnit() ) )
		{
			Image< R > calibratedImage = null;
			while( calibratedImage == null )
			{
				calibratedImage = new CalibrationDialog<>( inputImage ).showDialog();
			}
			inputImage.setVoxelUnit( calibratedImage.getVoxelUnit() );
			inputImage.setVoxelDimensions( calibratedImage.getVoxelDimensions() );
		}
	}

	public static boolean checkVoxelDimension( double[] voxelSize )
	{
		if ( voxelSize == null )
		{
			return false;
		}

		for ( int d = 0; d < 3; d++ )
		{
			if ( Double.isNaN( voxelSize[ d ] ) || voxelSize[ d ] <= 0.0 )
			{
				return false;
			}
		}

		return true;
	}

	public static boolean checkImage( Image< ? > image )
	{
		if ( ! checkVoxelUnit( image.getVoxelUnit() ) )
			return false;

		if ( ! checkVoxelDimension( image.getVoxelDimensions() ) )
			return false;

		return true;
	}

	public static boolean checkVoxelUnit( Unit< Length > voxelUnit )
	{
		if ( voxelUnit == null )
		{
			return false;
		}

		return true;
	}
}
