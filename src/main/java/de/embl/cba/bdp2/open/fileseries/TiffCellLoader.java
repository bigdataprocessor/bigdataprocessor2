/* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as
* published by the Free Software Foundation, either version 3 of the
* License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*/

package de.embl.cba.bdp2.open.fileseries;

import ch.systemsx.cisd.base.mdarray.MDByteArray;
import ch.systemsx.cisd.hdf5.HDF5DataSetInformation;
import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5Reader;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.utils.DimensionOrder;
import ij.ImagePlus;
import ij.ImageStack;
import net.imglib2.cache.img.SingleCellArrayImg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class TiffCellLoader
{

    // Compression modes
    public static final int COMPRESSION_UNKNOWN = 0;
    public static final int COMPRESSION_NONE = 1;
    public static final int LZW = 2;
    public static final int LZW_WITH_DIFFERENCING = 3;

    /**
     *
     * @param cell
     * @param directory
     * @param fileInfos
     * @param executorService
     */
    public static void load( SingleCellArrayImg cell, String directory, BDP2FileInfo[] fileInfos, ExecutorService executorService)
    {
        long[] longMin = new long[ cell.numDimensions() ];
        long[] longMax = new long[ cell.numDimensions() ];
        cell.min( longMin );
        cell.max( longMax );
        int[] min = Arrays.stream( longMin ).mapToInt( l -> Math.toIntExact( l ) ).toArray();
        int[] max = Arrays.stream( longMax ).mapToInt( l -> Math.toIntExact( l ) ).toArray();

        assert cell.min( DimensionOrder.C ) == cell.max( DimensionOrder.C );
        assert cell.min( DimensionOrder.T ) == cell.max( DimensionOrder.T );

        BDP2FileInfo fi = fileInfos[ min[ DimensionOrder.Z ] ];

        if ( Logger.getLevel().equals( Logger.Level.Debug ) )
        {
            Logger.debug( "# TiffCellLoader" );
            Logger.debug( "root directory: " + directory );
            Logger.debug( "fileInfos.length: " + fileInfos.length );
            Logger.debug( "fileInfo.directory: " + fi.directory );
            Logger.debug( "fileInfo.filename: " + fi.fileName );
            Logger.debug( "fileInfo.compression: " + fi.compression );
            Logger.debug( "fileInfo.intelByteOrder: " + fi.intelByteOrder );
            Logger.debug( "fileInfo.bytesPerPixel: " + fi.bytesPerPixel );
            Logger.debug( "min: " + Arrays.toString( longMin ) );
            Logger.debug( "max: " + Arrays.toString( longMax ) );
        }


        List<Future> futures = new ArrayList<>();
        for (int z = min[ DimensionOrder.Z ]; z <= max[ DimensionOrder.Z ]; z++ )
        {
            futures.add(
                    executorService.submit(
                            new PartialTiffPlaneCellLoader(
                                    cell,
                                    z,
                                    directory,
                                    fileInfos[ z ] )
                    )
            );
        }
        waitUntilDone( futures );
    }

    private static void waitUntilDone( List< Future > futures )
    {
        for (Future future : futures)
        {
            try
            {
                future.get();
            } catch ( InterruptedException e )
            {
                e.printStackTrace();
            } catch ( ExecutionException e )
            {
                e.printStackTrace();
            }
        }
    }


    private TiffCellLoader()
    {

    }

}
