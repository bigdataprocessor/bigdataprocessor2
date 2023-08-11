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

import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.utils.DimensionOrder;
import net.imglib2.cache.img.SingleCellArrayImg;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class TIFFCellLoader
{
    public static void load( SingleCellArrayImg cell, String directory, BDP2FileInfo[] fileInfos, ExecutorService executorService)
    {
        assert cell.min( DimensionOrder.C ) == cell.max( DimensionOrder.C );
        assert cell.min( DimensionOrder.T ) == cell.max( DimensionOrder.T );

        log( cell, directory, fileInfos );

        final long min = cell.min( DimensionOrder.Z );
        final long max = cell.max( DimensionOrder.Z );

        for ( long z = min; z <= max; z++ )
        {
            new TIFFPlaneCellLoader( cell, (int) z, directory, fileInfos[ (int) z ] ).run();
        }

        // TODO: BDV is multi-thread already, think about when it makes sense to
        //   add more multithreading on top, probably when loading the whole volume?
        //        List<Future> futures = new ArrayList<>();
        //        for (int z = min[ DimensionOrder.Z ]; z <= max[ DimensionOrder.Z ]; z++ )
        //        {
        //            futures.add(
        //                executorService.submit(
        //                    new PartialTIFFPlaneCellLoader(
        //                        cell,
        //                        z,
        //                        directory,
        //                        fileInfos[ z ] )
        //                )
        //            );
        //        }
        //        waitUntilDone( futures );


    }

    private static void log( SingleCellArrayImg cell, String directory, BDP2FileInfo[] fileInfos )
    {
        if ( Logger.getLevel().equals( Logger.Level.Debug ) )
        {
            BDP2FileInfo fi = fileInfos[ Math.toIntExact( cell.min( DimensionOrder.Z ) ) ];
            Logger.debug( "# TIFFCellLoader" );
            Logger.debug( "root directory: " + directory );
            Logger.debug( "fileInfos.length: " + fileInfos.length );
            Logger.debug( "fileInfo.directory: " + fi.directory );
            Logger.debug( "fileInfo.filename: " + fi.fileName );
            Logger.debug( "fileInfo.compression: " + fi.compression );
            Logger.debug( "fileInfo.intelByteOrder: " + fi.intelByteOrder );
            Logger.debug( "fileInfo.bytesPerPixel: " + fi.bytesPerPixel );
            long[] longMin = new long[ cell.numDimensions() ];
            long[] longMax = new long[ cell.numDimensions() ];
            cell.min( longMin );
            cell.max( longMax );
            Logger.debug( "min: " + Arrays.toString( longMin ) );
            Logger.debug( "max: " + Arrays.toString( longMax ) );
        }
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


    private TIFFCellLoader()
    {

    }

}
