package de.embl.cba.bdp2.open.core;

import bdv.viewer.Source;
import ch.epfl.biop.bdv.bioformats.bioformatssource.BioFormatsBdvOpener;
import ch.epfl.biop.bdv.bioformats.bioformatssource.BioFormatsBdvSource;
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
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;
import ome.units.UNITS;
import ome.units.quantity.Length;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import static de.embl.cba.bdp2.open.core.OpenerExtension.readCroppedPlaneFromTiffIntoImageStack.COMPRESSION_NONE;
import static net.imglib2.cache.img.ReadOnlyCachedCellImgOptions.options;

public class CachedCellImgCreator
{
    public static final int MAX_ARRAY_LENGTH = Integer.MAX_VALUE - 100;
    public static boolean isReadingVolumes = false;

    public static < R extends RealType< R > & NativeType< R > >
    Image< R > loadImage(String directory,
                         String dataLocation,
                         int series ) {

        List<Source> sources =
                BioFormatsBdvOpener.getOpener()
                        .location(dataLocation)
                        .auto() // patches opener based on specific file formats (-> PR to be  modified)
                        //.splitRGBChannels() // split RGB channels into 3 channels
                        //.switchZandC(true) // switch Z and C
                        //.centerPositionConvention() // bioformats location is center of the image
                        .cornerPositionConvention() // bioformats location is corner of the image
                        //.useCacheBlockSizeFromBioFormats(true) // true by default
                        //.cacheBlockSize(512,512,10) // size of cache block used by diskcached image
                        //.micronmeter() // unit = micrometer
                        .millimeter() // unit = millimeter
                        //.unit(UNITS.YARD) // Ok, if you really want...
                        //.getConcreteSources()
                        .positionReferenceFrameLength(new Length(1, UNITS.MICROMETER)) // Compulsory
                        .voxSizeReferenceFrameLength(new Length(100, UNITS.MICROMETER))
                        .getConcreteSources(series+".*") // code for all channels of the series indexed 'series'
                        .stream().map(src -> (Source) src).collect(Collectors.toList());


        List<BioFormatsBdvSource> sourcesBF = sources.stream().map(src ->
            BioFormatsBdvSource.class.cast( src )
        ).collect(Collectors.toList());

        BioFormatsBdvSource modelSource = sourcesBF.get(0);
        RandomAccessibleInterval modelRAI = sourcesBF.get(0).createSource(0,0);

        long sizeX = modelRAI.dimension(0); // limited to 2GPixels in one dimension
        long sizeY = modelRAI.dimension(1);
        long sizeZ = modelRAI.dimension(2);
        int sizeC = sourcesBF.size();
        long sizeT = modelSource.numberOfTimePoints;

        // TODO : sanity check identical size in XYZCT for all channels

        int[] cellDimsXYZCT = new int[]{(int)sizeX, (int)sizeY, (int)sizeZ, sizeC, (int)sizeT};

        //CachedCellImg cache TODO



        throw new UnsupportedOperationException();
    }

    public static CachedCellImg createCachedCellImg( FileInfos fileInfos )
    {
        int[] cellDimsXYZCT = getCellDimsXYZCT( fileInfos );

        if ( fileInfos.fileType.equals( OpenFileType.HDF5 ) )
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
        return getCellDimsXYZCT( fileInfos.bitDepth, imageDimsXYZCT );
    }

    public static int[] getCellDimsXYZCT( int bitDepth, int[] imageDimsXYZCT )
    {
        int[] cellDimsXYZCT = new int[ 5 ];

        cellDimsXYZCT[ 0 ] = imageDimsXYZCT[ 0 ]; // load whole rows

        final int bytesPerRow = imageDimsXYZCT[ 0 ] * bitDepth / 8;
        final double megaBitsPerPlane = imageDimsXYZCT[ 0 ] * imageDimsXYZCT[ 1 ] * bitDepth / 1000000.0;
        final int numRowsPerFileSystemBlock = 4096 / bytesPerRow;

        if ( megaBitsPerPlane > 10.0 ) // would take longer to load than one second at 10 MBit/s bandwidth
        {
            cellDimsXYZCT[ 1 ] = (int) Math.ceil( imageDimsXYZCT[ 1 ] / 3.0 ); // TODO: find a better value?
        }
        else
        {
            cellDimsXYZCT[ 1 ] = imageDimsXYZCT[ 1 ];
        }

        //cellDimsXYZCT[ 1 ] = ( int ) Math.ceil( imageDimsXYZCT[ 1 ] / 10 );

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
        final ImageLoader loader = new ImageLoader( fileInfos, cellDimsXYZCT );

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

    /**
     * Useful for saving to load the whole volume in one go as this
     * speeds up read performance significantly
     *
     * @param fileInfos
     * @param cacheSize
     *                  This should be set taking into consideration potential concurrent
     *                  access to different timepoints and channels.
	 * @return
     */
    public static CachedCellImg createVolumeCachedCellImg( FileInfos fileInfos, long cacheSize )
    {
        isReadingVolumes = true;

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

        final ImageLoader loader = new ImageLoader( fileInfos, new int[]{ cellDimX, cellDimY, cellDimZ, 1, 1 } );

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

    public static Image asImage( FileInfos fileInfos, CachedCellImg cachedCellImg )
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

    public static int[] getCellDimsXYZCT( RandomAccessibleInterval< ? > raiXYZCT )
    {
        int bitDepth = getBitDepth( raiXYZCT );
        final int[] imageDims = Intervals.dimensionsAsIntArray( raiXYZCT );
        return getCellDimsXYZCT( bitDepth, imageDims );
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
