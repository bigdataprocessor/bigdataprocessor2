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
        // because this is faster
        if ( ! settings.fileType.equals( SaveFileType.TIFFPlanes ) )
        {
            // TODO: for cropped images only fully load the cropped region
            // TODO: for input data distributed across TIFF planes this should be reconsidered
            long cacheSize = image.getDimensionsXYZCT()[ DimensionOrder.C ] * settings.numIOThreads;
            imageForSaving.setVolumeCache( CacheOptions.CacheType.BOUNDED, (int) cacheSize );
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
