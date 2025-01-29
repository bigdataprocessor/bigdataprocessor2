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
package de.embl.cba.bdp2.open.fileseries.hdf5;

import ch.systemsx.cisd.base.mdarray.MDByteArray;
import ch.systemsx.cisd.hdf5.HDF5DataSetInformation;
import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5Reader;
import de.embl.cba.bdp2.log.Logger;
import hdf.hdf5lib.exceptions.HDF5JavaException;
import net.imglib2.Interval;

public class HDF5Int8CellLoader
{
	private static long[] longDimensions;
	private static int[] intDimensions;
	private static long[] longMins;
	private static int[] memoryOffset;
	private static MDByteArray mdByteArray;

	public static void load(
			Interval interval,
			byte[] array,
			String filePath,
			String h5DataSet, boolean containsHDF5DatasetSingletonDimension )
	{
		IHDF5Reader reader = HDF5Factory.openForReading( filePath );
		HDF5DataSetInformation dsInfo = reader.getDataSetInformation( h5DataSet );
		String dsTypeString = HDF5Helper.hdf5InfoToString(dsInfo);

		if ( containsHDF5DatasetSingletonDimension )
			initWithSingleton4thDimension( interval, array );
		else
			init( interval, array );

		if ( dsTypeString.equals("int8") )
		{
			try
			{
				reader.int8().readToMDArrayBlockWithOffset(
						h5DataSet,
						mdByteArray,
						intDimensions,
						longMins,
						memoryOffset );
			}
			catch ( HDF5JavaException e )
			{
				initWithSingleton4thDimension( interval, array );

				reader.int8().readToMDArrayBlockWithOffset(
						h5DataSet,
						mdByteArray,
						intDimensions,
						longMins,
						memoryOffset );
			}
		}
		else if ( dsTypeString.equals("uint8") )
		{
			try
			{
				reader.uint8().readToMDArrayBlockWithOffset(
						h5DataSet,
						mdByteArray,
						intDimensions,
						longMins,
						memoryOffset );
			}
			catch ( HDF5JavaException e )
			{
				initWithSingleton4thDimension( interval, array );

				reader.int8().readToMDArrayBlockWithOffset(
						h5DataSet,
						mdByteArray,
						intDimensions,
						longMins,
						memoryOffset );
			}
		}
		else
		{
			Logger.error("Data type " + dsTypeString + " is currently not supported.");
			return;
		}
	}

	private static void init( Interval interval, byte[] array )
	{
		longDimensions = new long[]{
				interval.dimension( 2 ),
				interval.dimension( 1 ),
				interval.dimension( 0 ) };

		intDimensions = new int[]{
				( int ) interval.dimension( 2 ),
				( int ) interval.dimension( 1 ),
				( int ) interval.dimension( 0 ) };

		longMins = new long[]{
				interval.min( 2 ),
				interval.min( 1 ),
				interval.min( 0 ) };

		memoryOffset = new int[]{ 0, 0, 0 };

		mdByteArray = new MDByteArray( array, longDimensions );
	}

	private static void initWithSingleton4thDimension( Interval interval, byte[] array )
	{
		// try adding a singleton channel dimension (ilastik)

		longDimensions = new long[]{
				interval.dimension( 2 ),
				interval.dimension( 1 ),
				interval.dimension( 0 ),
				1
		};

		intDimensions = new int[] {
				( int ) interval.dimension( 2 ),
				( int ) interval.dimension( 1 ),
				( int ) interval.dimension( 0 ),
				1
		};

		longMins = new long[] {
				interval.min( 2 ),
				interval.min( 1 ),
				interval.min( 0 ),
				0
		};

		memoryOffset = new int[]{ 0, 0, 0, 0 };

		mdByteArray = new MDByteArray( array, longDimensions );
	}
}
