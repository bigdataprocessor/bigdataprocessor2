package de.embl.cba.bdp2.open.fileseries.hdf5;

import ch.systemsx.cisd.base.mdarray.MDByteArray;
import ch.systemsx.cisd.base.mdarray.MDShortArray;
import ch.systemsx.cisd.hdf5.HDF5DataSetInformation;
import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5Reader;
import de.embl.cba.bdp2.log.Logger;
import net.imglib2.Interval;

public class HDF5Int8CellLoader
{
	public static void load(
			Interval interval,
			byte[] array,
			String filePath,
			String h5DataSet )
	{
		IHDF5Reader reader = HDF5Factory.openForReading( filePath );
		HDF5DataSetInformation dsInfo = reader.getDataSetInformation( h5DataSet );
		String dsTypeString = HDF5Helper.hdf5InfoToString(dsInfo);

		final long[] longDimensions = {
				interval.dimension( 2 ),
				interval.dimension( 1 ),
				interval.dimension( 0 ) };

		final int[] intDimensions = {
				( int ) interval.dimension( 2 ),
				( int ) interval.dimension( 1 ),
				( int ) interval.dimension( 0 ) };

		final long[] longMins = {
				interval.min( 2 ),
				interval.min( 1 ),
				interval.min( 0 ) };

		final int[] memoryOffset = { 0, 0, 0 };

		final MDByteArray mdByteArray = new MDByteArray(
				array,
				longDimensions );

		if ( dsTypeString.equals("int8") )
		{
			reader.int8().readToMDArrayBlockWithOffset(
					h5DataSet,
					mdByteArray,
					intDimensions,
					longMins,
					memoryOffset );

		}
		else if ( dsTypeString.equals("uint8") )
		{
			reader.uint8().readToMDArrayBlockWithOffset(
					h5DataSet,
					mdByteArray,
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
}
