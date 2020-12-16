package de.embl.cba.bdp2.open.fileseries;

import ch.systemsx.cisd.base.mdarray.MDShortArray;
import ch.systemsx.cisd.hdf5.HDF5DataSetInformation;
import ch.systemsx.cisd.hdf5.HDF5DataTypeInformation;
import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5Reader;
import de.embl.cba.bdp2.log.Logger;
import net.imglib2.Interval;

public class HDF5CellLoader
{
	public static void load(
			Interval interval,
			short[] array,
			String filePath,
			String h5DataSet )
	{
//		if ( ! checkDataCubeSize( nz, nx, ny ) ) return null;

		IHDF5Reader reader = HDF5Factory.openForReading( filePath );
		HDF5DataSetInformation dsInfo = reader.getDataSetInformation( h5DataSet );
		String dsTypeString = hdf5InfoToString(dsInfo);

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

		final MDShortArray mdShortArray = new MDShortArray(
				array,
				longDimensions );

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

	public static boolean checkDataCubeSize( int nz, long nx, int ny )
	{
		long maxSize = (1L << 31) - 1;
		long nPixels = nx * ny * nz;
		if (nPixels > maxSize) {
			Logger.error("H5 Loader: nPixels > 2^31 => Currently not supported.");
			return false;
		}

		return true;
	}


	static String hdf5InfoToString(HDF5DataSetInformation dsInfo)
	{
		//
		// Code copied from Ronneberger
		//
		HDF5DataTypeInformation dsType = dsInfo.getTypeInformation();
		String typeText = "";

		if (dsType.isSigned() == false) {
			typeText += "u";
		}

		switch( dsType.getDataClass())
		{
			case INTEGER:
				typeText += "int" + 8*dsType.getElementSize();
				break;
			case FLOAT:
				typeText += "float" + 8*dsType.getElementSize();
				break;
			default:
				typeText += dsInfo.toString();
		}
		return typeText;
	}

}
