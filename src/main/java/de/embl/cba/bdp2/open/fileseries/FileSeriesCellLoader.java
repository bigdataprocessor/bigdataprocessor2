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
package de.embl.cba.bdp2.open.fileseries;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.log.CellLoaderLogger;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.open.fileseries.hdf5.HDF5Int16CellLoader;
import de.embl.cba.bdp2.open.fileseries.hdf5.HDF5Int8CellLoader;
import de.embl.cba.bdp2.service.PerformanceService;
import de.embl.cba.bdp2.utils.DimensionOrder;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.cache.img.SingleCellArrayImg;
import net.imglib2.type.NativeType;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

public class FileSeriesCellLoader< T extends NativeType< T > > implements CellLoader< T > {

    private final int bitDepth;
    private String directory;
    private long[] dimensions;
    private int[] cellDims;
    private LoadingCache< String, BDP2FileInfo[] > serializableFileInfoCache;
    private final FileSeriesFileType fileType;
    private short[][] cache;
    private final boolean containsHDF5DatasetSingletonDimension;

    public FileSeriesCellLoader( FileInfos fileInfos, int[] cellDimsXYZCT )
    {
        cellDims = cellDimsXYZCT;
        dimensions = fileInfos.getDimensions();
        directory = fileInfos.directory;
        fileType = fileInfos.fileType;
        bitDepth = fileInfos.bitDepth;
        containsHDF5DatasetSingletonDimension = fileInfos.containsHDF5DatasetSingletonDimension;

        CacheLoader< String, BDP2FileInfo[] > loader =
                new CacheLoader< String, BDP2FileInfo[]>(){
                    @Override
                    public BDP2FileInfo[] load( String c_t ){
                        final String[] split = c_t.split( "_" );
                        return fileInfos.getVolumeFileInfos( Integer.parseInt( split[ 0 ] ), Integer.parseInt( split[ 1 ] ) );
                    }
        };

        serializableFileInfoCache = CacheBuilder.newBuilder().maximumSize( 50 ).build( loader );
    }

    /*
     *
     * TODO: Not sure whether this should be synchronized or not; maybe safer it it is
     *
     * @param cell must be XYZCT
     */
    @Override
    public synchronized void load( final SingleCellArrayImg< T, ? > cell )
    {
        CellLoaderLogger< T > logger = new CellLoaderLogger<>( cell );
        logger.start();

        long[] min = new long[ cell.numDimensions() ];
        long[] max = new long[ cell.numDimensions()];
        cell.min( min );
        cell.max( max );

        assert cell.numDimensions() == DimensionOrder.N;
        assert min[ DimensionOrder.C ] == max[ DimensionOrder.C ];
        assert min[ DimensionOrder.T ] == max[ DimensionOrder.T ];

        int[] ct = new int[ 2 ];
        ct[ 0 ] = Math.toIntExact( max[ DimensionOrder.C ] );
        ct[ 1 ] = Math.toIntExact( max[ DimensionOrder.T ] );
        BDP2FileInfo[] fileInfos = getVolumeFileInfos( ct );

        if ( fileType.toString().toLowerCase().contains( "tif" ) )
        {
            TIFFCellLoader.load( cell, directory, fileInfos, BigDataProcessor2.threadPool );
        }
        else if ( fileType.toString().toLowerCase().contains( "hdf5" ) )
        {
            // Unchecked assumptions:
            // - data is unsigned short
            // - all z planes are in the same file

            if ( bitDepth == 8 )
            {
                HDF5Int8CellLoader.load(
                        cell,
                        ( byte[] ) cell.getStorageArray(),
                        getFullPath( fileInfos[ 0 ] ),
                        fileInfos[ 0 ].h5DataSet,
                        containsHDF5DatasetSingletonDimension);
            }
            else if ( bitDepth == 16 )
            {
                HDF5Int16CellLoader.load(
                        cell,
                        ( short[] ) cell.getStorageArray(),
                        getFullPath( fileInfos[ 0 ] ),
                        fileInfos[ 0 ].h5DataSet,
                        containsHDF5DatasetSingletonDimension );
            }
            else
            {
                throw new UnsupportedOperationException( "Bit depth " + bitDepth + " currently not supported for HDF5." );
            }
        }

        logger.stop();
        if ( Logger.getLevel().equals( Logger.Level.Benchmark ) )
        {
            Logger.benchmark( logger.getBenchmarkLog() );
        }
        PerformanceService.getPerformanceMonitor().addReadPerformance( cell.getStorageArray(), logger.getDurationNanos() / Math.pow( 10, 6 )  );
    }

    private static void log( long[] min, long[] max, long nanos )
    {
        Logger.benchmark( "Read " + Arrays.toString( min ) + " - " + Arrays.toString( max ) + " in " + ( nanos / 1000000.0 ) + " ms" );
    }

    public long[] getDimensions()
    {
        return dimensions;
    }

    public int[] getCellDims()
    {
        return cellDims;
    }

    private BDP2FileInfo[] getVolumeFileInfos( int[] ct ) {
        try {
             return serializableFileInfoCache.get( "" + ct[0] + "_" + ct[1] );
        } catch ( ExecutionException e ) {
            e.printStackTrace();
            throw new RuntimeException( e );
        }
    }

    private static String getFullPath( String directory, BDP2FileInfo fileInfo )
    {
        return directory + File.separator + fileInfo.directory + File.separator + fileInfo.fileName;
    }

    private static String getFullPath( BDP2FileInfo fileInfo )
    {
        return fileInfo.directory + File.separator + fileInfo.fileName;
    }
}
