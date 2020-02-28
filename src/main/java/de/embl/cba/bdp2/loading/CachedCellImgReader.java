package de.embl.cba.bdp2.loading;

import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.logging.Logger;
import de.embl.cba.bdp2.utils.Utils;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.cache.img.ReadOnlyCachedCellImgFactory;
import net.imglib2.cache.img.ReadOnlyCachedCellImgOptions;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.Intervals;

import java.io.File;

import static de.embl.cba.bdp2.loading.OpenerExtension.readCroppedPlaneFromTiffIntoImageStack.COMPRESSION_NONE;
import static net.imglib2.cache.img.ReadOnlyCachedCellImgOptions.options;

public class CachedCellImgReader
{
    public static final int MAX_ARRAY_LENGTH = Integer.MAX_VALUE - 100;

    public static CachedCellImg createCachedCellImg( FileInfos fileInfos )
    {
        int[] cellDimsXYZCT = getCellDimsXYZCT( fileInfos );

        if ( fileInfos.fileType.equals( Utils.FileType.HDF5.toString() ) )
        {
            return createCachedCellImg( fileInfos, cellDimsXYZCT );
        }
        else // Tiff
        {
            if ( fileInfos.numTiffStrips == 1 && fileInfos.compression != COMPRESSION_NONE )
            {
                // File is compressed plane-wise => we need to load the whole plane
                cellDimsXYZCT[ 1 ] = fileInfos.nY;
            }

            return createCachedCellImg( fileInfos, cellDimsXYZCT );
        }
    }

    public static int[] getCellDimsXYZCT( FileInfos fileInfos )
    {
        final int[] imageDimsXYZCT = { fileInfos.nX, fileInfos.nY, fileInfos.nZ, 1, 1 };
        return getCellDimsXYZCT( imageDimsXYZCT );
    }

    public static int[] getCellDimsXYZCT( int[] imageDimsXYZCT )
    {
        int[] cellDimsXYZCT = new int[ 5 ];

        // load whole rows
        cellDimsXYZCT[ 0 ] = imageDimsXYZCT[ 0 ];

        // load rows in blocks of 10
        cellDimsXYZCT[ 1 ] = ( int ) Math.ceil( imageDimsXYZCT[ 1 ] / 10 );

        // load one plane
        cellDimsXYZCT[ 2 ] = 1;

        // load one channel
        cellDimsXYZCT[ 3 ] = 1;

        // load one timepoint
        cellDimsXYZCT[ 4 ] = 1;

        return cellDimsXYZCT;
    }

    public static < R extends RealType< R > & NativeType< R > >
    Image< R > loadImage( FileInfos fileInfos )
    {
        CachedCellImg cachedCellImg = createCachedCellImg( fileInfos );
        return asImage( fileInfos, cachedCellImg );
    }

    public static < R extends RealType< R > & NativeType< R > >
    Image< R > loadImage( FileInfos fileInfos, int[] cellDimsXYZ )
    {
        CachedCellImg cachedCellImg = createCachedCellImg(
                fileInfos, cellDimsXYZ );

        return asImage( fileInfos, cachedCellImg );
    }

    public static CachedCellImg createCachedCellImg( FileInfos fileInfos,
                                                     int[] cellDimsXYZCT )
    {
        final ImageLoader loader =
                new ImageLoader( fileInfos, cellDimsXYZCT );

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

        final long numPixels = (long) cellDimX * (long) cellDimY * (long) cellDimZ;

        if ( numPixels > MAX_ARRAY_LENGTH )
        {
            Logger.info( "Adapting cell size in Z to satisfy java array indexing limit.");
            Logger.info( "Desired cell size in Z: " + cellDimZ );
            cellDimZ = MAX_ARRAY_LENGTH / ( cellDimX * cellDimY );
            Logger.info( "Adapted cell size in Z: " + cellDimZ );
        }

        final ImageLoader loader = new ImageLoader(
                fileInfos, new int[]{ cellDimX, cellDimY, cellDimZ } );

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

    public static int[] getCellDimsXYZCT( RandomAccessibleInterval< ? > raiXYZCT )
    {
        final int[] imageDims = Intervals.dimensionsAsIntArray( raiXYZCT );
        return getCellDimsXYZCT( imageDims );
    }
}
