package de.embl.cba.bdp2.open.fileseries.hdf5;

import ch.systemsx.cisd.base.mdarray.MDShortArray;
import ch.systemsx.cisd.hdf5.HDF5DataSetInformation;
import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5Reader;
import de.embl.cba.bdp2.log.Logger;
import net.imglib2.Interval;

public class HDF5Int16CellLoader
{

	private static long[] longDimensions;
	private static int[] intDimensions;
	private static long[] longMins;
	private static int[] memoryOffset;
	private static MDShortArray mdShortArray;

	public static void load(
			Interval interval,
			short[] array,
			String filePath,
			String h5DataSet,
			boolean containsHDF5DatasetSingletonDimension )
	{
		IHDF5Reader reader = HDF5Factory.openForReading( filePath );
		HDF5DataSetInformation dsInfo = reader.getDataSetInformation( h5DataSet );
		String dsTypeString = HDF5Helper.hdf5InfoToString(dsInfo);

		if ( containsHDF5DatasetSingletonDimension )
			initWithSingleton4thDimension( interval, array );
		else
			init( interval, array );

		if ( dsTypeString.equals("int16") )
		{
			reader.int16().readToMDArrayBlockWithOffset(
				h5DataSet,
				mdShortArray,
				intDimensions,
				longMins,
				memoryOffset );
		}
		else if ( dsTypeString.equals("uint16") )
		{
			reader.uint16().readToMDArrayBlockWithOffset(
				h5DataSet,
				mdShortArray,
				intDimensions,
				longMins,
				memoryOffset );
		}
		else
		{
			Logger.error("Data type " + dsTypeString + " is currently not supported.");
			return;
		}
	}

	private static void init( Interval interval, short[] array )
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

		mdShortArray = new MDShortArray(
				array,
				longDimensions );
	}

	private static void initWithSingleton4thDimension( Interval interval, short[] array )
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

		mdShortArray = new MDShortArray( array, longDimensions );
	}
}
