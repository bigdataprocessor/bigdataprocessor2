package de.embl.cba.bdp2.loading;

import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.logging.Logger;
import de.embl.cba.bdp2.utils.Utils;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.cache.img.DiskCachedCellImgOptions.CacheType;
import net.imglib2.cache.img.ReadOnlyCachedCellImgFactory;
import net.imglib2.cache.img.ReadOnlyCachedCellImgOptions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import java.io.File;

import static net.imglib2.cache.img.ReadOnlyCachedCellImgOptions.options;

public class CachedCellImgReader
{

    public static final int MAX_ARRAY_LENGTH = Integer.MAX_VALUE - 100;

    public static CachedCellImg getCachedCellImg( FileInfos fileInfos )
    {
        // TODO: optimise somehow....
        final int cellDimY = ( int ) Math.ceil( fileInfos.nY / 10 );

        if ( fileInfos.fileType.equals( Utils.FileType.HDF5.toString() ) )
        {
            return getCachedCellImg( fileInfos,
                    fileInfos.nX, cellDimY, 1 );
        }
        else if ( fileInfos.fileType.equals( Utils.FileType.TIFF_STACKS.toString() ) )
        {
            return getCachedCellImg( fileInfos,
                    fileInfos.nX, cellDimY, 1 );
        }
        else if ( fileInfos.fileType.equals( Utils.FileType.SINGLE_PLANE_TIFF.toString() ) )
        {
            return getCachedCellImg( fileInfos,
                    fileInfos.nX, cellDimY, 1 );
        }
        else
        {
            return null;
        }

    }


    public static < R extends RealType< R > & NativeType< R > >
    Image< R > loadImage( FileInfos fileInfos )
    {
        CachedCellImg cachedCellImg = getCachedCellImg( fileInfos );
        return asImage( fileInfos, cachedCellImg );
    }

    public static < R extends RealType< R > & NativeType< R > >
    Image< R > loadImage( FileInfos fileInfos,
                          int cellDimX,
                          int cellDimY,
                          int cellDimZ )
    {
        CachedCellImg cachedCellImg = getCachedCellImg(
                fileInfos, cellDimX, cellDimY, cellDimZ );
        return asImage( fileInfos, cachedCellImg );

    }

    public static CachedCellImg getCachedCellImg( FileInfos fileInfos,
                                                  int cellDimX,
                                                  int cellDimY,
                                                  int cellDimZ )
    {
        final ImageLoader loader =
                new ImageLoader( fileInfos, cellDimX, cellDimY, cellDimZ );

        final ReadOnlyCachedCellImgOptions options = options()
                .cellDimensions( loader.getCellDims() );

        final CachedCellImg cachedCellImg =
                new ReadOnlyCachedCellImgFactory().create(
                    loader.getDimensions(),
                    fileInfos.getType(),
                    loader,
                    options);

        return cachedCellImg;
    }

    public static CachedCellImg getVolumeCachedCellImg( FileInfos fileInfos )
    {
        int cellDimX = fileInfos.nX;
        int cellDimY = fileInfos.nY;
        int cellDimZ = fileInfos.nZ;

        if ( cellDimX * cellDimY * cellDimZ > MAX_ARRAY_LENGTH )
        {
            Logger.info( "Adapting cell size in Z to satisfy java array indexing limit.");
            Logger.info( "Desired cell size in Z: " + cellDimZ );
            cellDimZ = MAX_ARRAY_LENGTH / ( cellDimY * cellDimZ );
            Logger.info( "Adapted cell size in Z: " + cellDimZ );
        }

        final ImageLoader loader = new ImageLoader(
                fileInfos,
                cellDimX,
                cellDimY,
                cellDimZ );

        final ReadOnlyCachedCellImgOptions options = options()
                .cellDimensions( loader.getCellDims() );

        final CachedCellImg cachedCellImg = new ReadOnlyCachedCellImgFactory().create(
                loader.getDimensions(),
                fileInfos.getType(),
                loader,
                options);

        return cachedCellImg;
    }

    public static Image asImage( FileInfos fileInfos, CachedCellImg cachedCellImg )
    {
        return new Image<>(
                cachedCellImg,
                new File( fileInfos.directory ).getName(),
                fileInfos.voxelSpacing,
                fileInfos.voxelUnit,
                fileInfos
                );
    }
}
