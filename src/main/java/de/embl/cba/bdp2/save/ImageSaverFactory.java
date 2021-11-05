/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2021 EMBL
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
package de.embl.cba.bdp2.save;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.save.bdvhdf5.BigDataViewerXMLHDF5Saver;
import de.embl.cba.bdp2.save.imaris.ImarisImageSaver;
import de.embl.cba.bdp2.save.tiff.TIFFPlanesSaver;
import de.embl.cba.bdp2.save.tiff.TIFFFramesSaver;
import de.embl.cba.bdp2.utils.DimensionOrder;
import net.imglib2.cache.img.DiskCachedCellImgOptions;
import net.imglib2.cache.img.optional.CacheOptions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageSaverFactory < R extends RealType< R > & NativeType< R > >
{
    public ImageSaver getSaver( Image< R > image, SavingSettings settings )
    {
        // create a copy in order not to change the caching strategy
        // of the currently shown image
        Image< R > imageForSaving = new Image<>( image );

        // change cache to load the whole volume,
        // because this is faster for saving
        if ( ! settings.fileType.equals( SaveFileType.TIFFPlanes ) )
        {
            // TODO: for cropped images only fully load the cropped region
            // TODO: for input data distributed across TIFF planes this should be reconsidered
            try
            {
                long cacheSize = image.getDimensionsXYZCT()[ DimensionOrder.C ] * settings.numIOThreads;
                imageForSaving.setVolumeCache( CacheOptions.CacheType.BOUNDED, ( int ) cacheSize );
            }
            catch ( Exception e )
            {
                System.out.printf( "Cache could not be optimised for saving.\n" );
                System.out.printf( "Saving anyway, but it might be slower...\n" );
            }
        }

        // Prepare multi-threaded I/O
        ExecutorService executorService = Executors.newFixedThreadPool( settings.numIOThreads );

        switch ( settings.fileType )
        {
            case TIFFPlanes:
                return new TIFFPlanesSaver( imageForSaving, settings, executorService );
            case TIFFVolumes:
                return new TIFFFramesSaver( imageForSaving, settings, executorService );
            case ImarisVolumes:
                return new ImarisImageSaver( imageForSaving, settings, executorService );
            case BigDataViewerXMLHDF5:
                return new BigDataViewerXMLHDF5Saver<>( imageForSaving, settings );
            default:
                throw new UnsupportedOperationException( settings.fileType.toString() );
        }
    }
}
