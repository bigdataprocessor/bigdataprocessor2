package de.embl.cba.bdp2.open.fileseries;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.open.OpenFileType;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.cache.img.DiskCachedCellImgOptions;
import net.imglib2.cache.img.ReadOnlyCachedCellImgFactory;
import net.imglib2.cache.img.ReadOnlyCachedCellImgOptions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.util.Util;

import java.io.File;

import static de.embl.cba.bdp2.open.fileseries.OpenerExtension.readCroppedPlaneFromTiffIntoImageStack.COMPRESSION_NONE;
import static net.imglib2.cache.img.ReadOnlyCachedCellImgOptions.options;

public class FileSeriesCachedCellImageCreator
{
    public static final int MAX_ARRAY_LENGTH = Integer.MAX_VALUE - 100;
    private final FileInfos fileInfos;
    private int[] imageDimsXYZ;

    public FileSeriesCachedCellImageCreator( FileInfos fileInfos )
    {
        this.fileInfos = fileInfos;
        this.imageDimsXYZ = new int[]{ fileInfos.nX, fileInfos.nY, fileInfos.nZ };
    }

    public < R extends RealType< R > & NativeType< R > > Image< R > createImage()
    {
        // TODO: The image should not need the file infos anymore, rather
        return asImage( fileInfos, createPlaneWiseCachedCellImg() );
    }

    /**
     * Compute cell dimensions that are good for fast z-plane-wise loading
     *
     * @param imageDimsXYZ
     * @param bitDepth
     * @return
     */
    private int[] createPlaneWiseCellDimsXYZCT( int[] imageDimsXYZ, int bitDepth )
    {
        int[] cellDimsXYZCT = new int[ 5 ];

        cellDimsXYZCT[ 0 ] = imageDimsXYZ[ 0 ]; // load whole rows

        final int bytesPerRow = imageDimsXYZ[ 0 ] * bitDepth / 8;
        final double megaBitsPerPlane = imageDimsXYZ[ 0 ] * imageDimsXYZ[ 1 ] * bitDepth / 1000000.0;
        final int numRowsPerFileSystemBlock = 4096 / bytesPerRow;

        if ( megaBitsPerPlane > 10.0 ) // would take longer to load than one second at 10 MBit/s bandwidth
        {
            cellDimsXYZCT[ 1 ] = (int) Math.ceil( imageDimsXYZ[ 1 ] / 3.0 ); // TODO: find a better value?
        }
        else
        {
            cellDimsXYZCT[ 1 ] = imageDimsXYZ[ 1 ];
        }

        //cellDimsXYZCT[ 1 ] = ( int ) Math.ceil( imageDimsXYZCT[ 1 ] / 10 );

        // load one plane
        cellDimsXYZCT[ 2 ] = 1;

        // load one channel
        cellDimsXYZCT[ 3 ] = 1;

        // load one timepoint
        cellDimsXYZCT[ 4 ] = 1;

        if ( fileInfos.fileType.equals( OpenFileType.TIFF_PLANES ) || fileInfos.fileType.equals( OpenFileType.TIFF_STACKS ) )
        {
            if ( fileInfos.numTiffStrips == 1 && fileInfos.compression != COMPRESSION_NONE )
            {
                // File is compressed plane-wise => we need to load the whole plane
                cellDimsXYZCT[ 1 ] = fileInfos.nY;
            }
        }

        return cellDimsXYZCT;
    }

    private CachedCellImg createPlaneWiseCachedCellImg()
    {
        int[] cellDimsXYZCT = createPlaneWiseCellDimsXYZCT( imageDimsXYZ, fileInfos.bitDepth );

        return createCachedCellImg( cellDimsXYZCT );
    }

//    public static < R extends RealType< R > & NativeType< R > > Image< R > createImage( FileInfos fileInfos, int[] cellDimsXYZ )
//    {
//        CachedCellImg cachedCellImg = createCachedCellImg( fileInfos, cellDimsXYZ );
//
//        return asImage( fileInfos, cachedCellImg );
//    }

    private CachedCellImg createCachedCellImg( int[] cellDimsXYZCT )
    {
        final FileSeriesCellLoader loader = new FileSeriesCellLoader<>( fileInfos, cellDimsXYZCT );

        final ReadOnlyCachedCellImgOptions options = options().cellDimensions( loader.getCellDims() );

        final CachedCellImg cachedCellImg =
                new ReadOnlyCachedCellImgFactory().create(
                    loader.getDimensions(),
                    fileInfos.getType(),
                    loader,
                    options);

        return cachedCellImg;
    }

    /**
     * Useful for saving to load the whole volume in one go as this
     * speeds up read performance significantly
     *
     * @param cacheSize
     *                  This should be set taking into consideration potential concurrent
     *                  access to different timepoints and channels.
	 * @return
     */
    public CachedCellImg createVolumeCachedCellImg( long cacheSize )
    {
        int cellDimX = fileInfos.nX;
        int cellDimY = fileInfos.nY;
        int cellDimZ = fileInfos.nZ;

        long numPixels = (long) cellDimX * (long) cellDimY * (long) cellDimZ;

        if ( numPixels > MAX_ARRAY_LENGTH )
        {
            Logger.info( "Adapting cell size in Z to satisfy java array indexing limit.");
            Logger.info( "Desired cell size in Z: " + cellDimZ );
            cellDimZ = MAX_ARRAY_LENGTH / ( cellDimX * cellDimY ) ;
            Logger.info( "Adapted cell size in Z: " + cellDimZ );
        }

        int[] cellDimsXYZCT = { cellDimX, cellDimY, cellDimZ, 1, 1 };

        final FileSeriesCellLoader loader = new FileSeriesCellLoader<>( fileInfos, cellDimsXYZCT );

        final ReadOnlyCachedCellImgOptions options = options()
                .cellDimensions( loader.getCellDims() )
                .cacheType( DiskCachedCellImgOptions.CacheType.BOUNDED )
                .maxCacheSize( cacheSize );

        final CachedCellImg cachedCellImg = new ReadOnlyCachedCellImgFactory().create(
                loader.getDimensions(),
                fileInfos.getType(),
                loader,
                options);

        return cachedCellImg;
    }

    private static Image asImage( FileInfos fileInfos, CachedCellImg< ?, ? > cachedCellImg )
    {
        return new Image(
                cachedCellImg,
                new File( fileInfos.directory ).getName(),
                fileInfos.channelNames,
                fileInfos.voxelSize,
                fileInfos.voxelUnit,
                fileInfos
                );
    }

    public static int getBitDepth( RandomAccessibleInterval< ? > raiXYZCT )
    {
        int bitDepth;
        final Object typeFromInterval = Util.getTypeFromInterval( raiXYZCT );
        if ( typeFromInterval instanceof UnsignedByteType )
            bitDepth = 8;
        else if ( typeFromInterval instanceof UnsignedShortType )
            bitDepth = 16;
        else
            throw new UnsupportedOperationException( "Type not supported: " + typeFromInterval );
        return bitDepth;
    }
}
