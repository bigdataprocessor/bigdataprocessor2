package de.embl.cba.bdp2.open.fileseries.hdf5;

import ch.systemsx.cisd.base.mdarray.MDByteArray;
import ch.systemsx.cisd.base.mdarray.MDShortArray;
import ch.systemsx.cisd.hdf5.HDF5DataSetInformation;
import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5Reader;
import de.embl.cba.bdp2.log.Logger;
import ncsa.hdf.hdf5lib.exceptions.HDF5JavaException;
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
			String h5DataSet )
	{
		IHDF5Reader reader = HDF5Factory.openForReading( filePath );
		HDF5DataSetInformation dsInfo = reader.getDataSetInformation( h5DataSet );
		String dsTypeString = HDF5Helper.hdf5InfoToString(dsInfo);

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
				addSingleton4thDimension( interval, array );

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
				addSingleton4thDimension( interval, array );

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

	private static void addSingleton4thDimension( Interval interval, byte[] array )
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
