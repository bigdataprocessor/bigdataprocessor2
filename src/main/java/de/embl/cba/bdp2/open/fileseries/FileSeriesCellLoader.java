package de.embl.cba.bdp2.open.fileseries;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.service.PerformanceService;
import de.embl.cba.bdp2.utils.DimensionOrder;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;
import de.embl.cba.bdp2.utils.Point3D;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.cache.img.SingleCellArrayImg;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static de.embl.cba.bdp2.utils.DimensionOrder.Z;

public class FileSeriesCellLoader< T extends NativeType< T > > implements CellLoader< T > {

    private String directory;
    private long[] dimensions;
    private int[] cellDims;
    private LoadingCache< int[], BDP2FileInfo[] > serializableFileInfoCache;
    private final FileSeriesFileType fileType;
    private short[][] cache;

    public FileSeriesCellLoader( FileInfos fileInfos, int[] cellDimsXYZCT )
    {
        this.cellDims = cellDimsXYZCT;
        this.dimensions = fileInfos.getDimensions();
        this.directory = fileInfos.directory;
        this.fileType = fileInfos.fileType;

        CacheLoader< int[], BDP2FileInfo[] > loader =
                new CacheLoader< int[], BDP2FileInfo[]>(){
                    @Override
                    public BDP2FileInfo[] load( int[] ct ){
                        return fileInfos.getSerializableFileStackInfo( ct[ 0 ], ct[ 1 ] );
                    }
        };

        serializableFileInfoCache = CacheBuilder.newBuilder().maximumSize( 50 ).build( loader );
    }

    /**
     *
     * @param cell must be XYZCT
     */
    @Override
    public void load( final SingleCellArrayImg< T, ? > cell )
    {
        long start = System.currentTimeMillis();

        long[] min = new long[ cell.numDimensions() ];
        long[] max = new long[ cell.numDimensions()];
        cell.min( min );
        cell.max( max );

        assert cell.numDimensions() == DimensionOrder.N;
        assert min[ DimensionOrder.C ] == max[ DimensionOrder.C ];
        assert min[ DimensionOrder.T ] == max[ DimensionOrder.T ];

        int[] ct = new int[ 2 ];
        ct[ 0 ] = Math.toIntExact( max[ DimensionOrder.C ] );
        ct[ 1 ] = Math.toIntExact( max[ DimensionOrder.C ] );
        BDP2FileInfo[] fileInfos = getVolumeFileInfos( ct );

        if ( fileType.toString().toLowerCase().contains( "tif" ) )
        {
            TiffCellLoader.load( cell, directory, fileInfos, BigDataProcessor2.threadPool );
        }
        else if ( fileType.toString().toLowerCase().contains( "hdf5" ) )
        {
            // Unchecked assumptions:
            // - data is unsigned short
            // - all z planes are in the same file
            HDF5DataCubeReader.read16bitDataCubeIntoArray(
                    cell,
                    (short[]) cell.getStorageArray(),
                    getFullPath( directory, fileInfos[ 0 ] ),
                    fileInfos[ 0 ].h5DataSet );
        }

        long timeMillis = System.currentTimeMillis() - start;
        log( min, max, timeMillis );
        PerformanceService.getPerformanceMonitor().addReadPerformance( cell, timeMillis  );
    }

    private static void log( long[] min, long[] max, long timeMillis )
    {
        Logger.benchmark( "Read " + Arrays.toString( min ) + " - " + Arrays.toString( max ) + " in " + timeMillis + " ms" );
    }

    public long[] getDimensions()
    {
        return dimensions;
    }

    public int[] getCellDims()
    {
        return cellDims;
    }

    private BDP2FileInfo getFileInfo( int[] cell )
    {
        int c = Math.toIntExact( cell.max( DimensionOrder.C ) );
        int t = Math.toIntExact( cell.max( DimensionOrder.T ) );
        int z = Math.toIntExact( cell.max( Z ));

        List<Integer> c_t = Arrays.asList(c,t);
        BDP2FileInfo[] infos_c_t = getVolumeFileInfos(c_t);
        BDP2FileInfo fileInfo = infos_c_t[ z ];
        return fileInfo;
    }

    private ImagePlus getDataCube( long[] min,  long[] max )
    {
        int channel = Math.toIntExact( max[ DimensionOrder.C ]);
        int time = Math.toIntExact( max[ DimensionOrder.T ]);
        List<Integer> c_t = Arrays.asList(channel,time);
        BDP2FileInfo[] infos_c_t = getVolumeFileInfos(c_t);

//        po = getOffset( min[ DimensionOrder.X ], min[ DimensionOrder.Y ], z );
//        ps = getSize( min, max );

        Point3D po = getOffset( min );
        Point3D ps = getSize( min, max );

        //TODO: get rid of ImagePlus
        ImagePlus imagePlus = new TiffAndHDF5Opener().readDataCube(
                directory,
                infos_c_t,
                1,
                po,
                ps,
                BigDataProcessor2.threadPool );

        return imagePlus;
    }

    private Point3D getOffset( long[] min )
    {
        Point3D po;
        po = new Point3D( min[ 0 ], min[ 1 ], min[ 2 ] );
        return po;
    }

    private Point3D getOffset( long minX, long minY, int z )
    {
        Point3D po;
        po = new Point3D( minX, minY, z );
        return po;
    }

    private Point3D getSize( long[] min, long[] max )
    {
        long sX = max[ DimensionOrder.X  ] - min[ DimensionOrder.X ] + 1;
        long sY = max[ DimensionOrder.Y  ] - min[ DimensionOrder.Y ] + 1;
        long sZ = max[ DimensionOrder.Z  ] - min[ DimensionOrder.Z ] + 1;
        Point3D ps = new Point3D( sX, sY, sZ );
        return ps;
    }

    private BDP2FileInfo[] getVolumeFileInfos( int[] ct ) {
        try {
             return serializableFileInfoCache.get( ct );
        } catch ( ExecutionException e ) {
            e.printStackTrace();
            throw new RuntimeException( e );
        }
    }

    private static String getFullPath( String directory, BDP2FileInfo fileInfo )
    {
        return directory + File.separator + fileInfo.directory + File.separator + fileInfo.fileName;
    }
}
