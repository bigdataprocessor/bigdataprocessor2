package de.embl.cba.bdp2.loading;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import de.embl.cba.bdp2.ui.BigDataProcessor2;
import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.loading.files.SerializableFileInfo;
import de.embl.cba.bdp2.utils.DimensionOrder;
import ij.ImagePlus;
import javafx.geometry.Point3D;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.cache.img.SingleCellArrayImg;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import org.mozilla.javascript.tools.debugger.Dim;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static de.embl.cba.bdp2.utils.DimensionOrder.Z;


public class ImageLoader< T extends NativeType< T > > implements CellLoader< T > {

    private String directory;
    private long[] dimensions;
    private int[] cellDims;
    private LoadingCache< List< Integer >, SerializableFileInfo[] > serializableFileInfoCache;

    public ImageLoader( FileInfos infoSource ) {

        // TODO: optimiseRegions2D based on input file format
        int cellDimX = infoSource.nX;
        int cellDimY = infoSource.nY; // 45;

        this.cellDims = new int[]{ cellDimX, cellDimY, 1, 1, 1 };
        this.dimensions = infoSource.getDimensions();
        this.directory = infoSource.directory;

        //Google Guava cache
        CacheLoader< List<Integer>, SerializableFileInfo[] > loader =
                new CacheLoader<List<Integer>, SerializableFileInfo[]>(){
                    @Override
                    public SerializableFileInfo[] load( List<Integer> c_t ){
                        return infoSource.getSerializableFileStackInfo( c_t.get(0), c_t.get(1) );
                    }
        };
        serializableFileInfoCache = CacheBuilder.newBuilder().maximumSize( 50 ).build(loader);
    }

    private SerializableFileInfo getFileInfo( SingleCellArrayImg cell )
    {
        int c = Math.toIntExact( cell.max( DimensionOrder.C ) );
        int t = Math.toIntExact( cell.max( DimensionOrder.T ) );
        int z = Math.toIntExact( cell.max( Z ));

        List<Integer> c_t = Arrays.asList(c,t);
        SerializableFileInfo[] infos_c_t = getFileInfoStack(c_t);
        SerializableFileInfo fileInfo = infos_c_t[ z ];
        return fileInfo;
    }

    public ImagePlus getDataCube( long[] min,  long[] max )
    {
        int z = Math.toIntExact( max[ Z ]);
        int channel = Math.toIntExact( max[ DimensionOrder.C ]);
        int time = Math.toIntExact( max[ DimensionOrder.T ]);
        List<Integer> c_t = Arrays.asList(channel,time);
        SerializableFileInfo[] infos_c_t = getFileInfoStack(c_t);
        SerializableFileInfo fileInfo = infos_c_t[z];
        Point3D po, ps;
        po = getOffset( min[ DimensionOrder.X ], min[ DimensionOrder.Y ], z );
        ps = getSize( min, max );
        //TODO: get rid of ImagePlus
        ImagePlus imagePlus = new OpenerExtension().readDataCube(
                directory,
                infos_c_t,
                1,
                po,
                ps,
                BigDataProcessor2.generalThreadPool);

        return imagePlus;
    }

    private Point3D getOffset( long minX, long minY, int z )
    {
        Point3D po;
        po = new Point3D( minX, minY, z );
        return po;
    }

    private Point3D getSize( long[] min, long[] max )
    {
        Point3D ps;
        long sX = max[ DimensionOrder.X  ] - min[ DimensionOrder.X ] + 1;
        long sY = max[ DimensionOrder.Y  ] - min[ DimensionOrder.Y ] + 1;
        ps = new Point3D( sX, sY, 1);
        return ps;
    }

    private SerializableFileInfo[] getFileInfoStack(List<Integer> c_t) {
        SerializableFileInfo[] infos_c_t = null;
        try {
             infos_c_t = serializableFileInfoCache.get(c_t);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return infos_c_t;
    }

    @Override
    public synchronized void load( final SingleCellArrayImg< T, ? > cell ) {
        long[] min = new long[ FileInfos.TOTAL_AXES ];
        long[] max = new long[ FileInfos.TOTAL_AXES ];
        cell.min(min);
        cell.max(max);
        if (cell.firstElement() instanceof UnsignedByteType) {
            ImagePlus imagePlus = getDataCube( min, max );
            final byte[] impData = (byte[]) imagePlus.getProcessor().getPixels();
            final byte[] cellData = (byte[]) cell.getStorageArray();
            System.arraycopy(impData, 0, cellData, 0, cellData.length);
        }
        else if (cell.firstElement() instanceof UnsignedShortType)
        {
            final SerializableFileInfo fileInfo = getFileInfo( cell );

            Hdf5DataCubeReader.read16bitDataCubeIntoArray(
                    cell,
                    (short[]) cell.getStorageArray(),
                    getFilePath( fileInfo ),
                    fileInfo.h5DataSet );

        } else if (cell.firstElement() instanceof FloatType) {
            ImagePlus imagePlus = getDataCube( min, max );
            final float[] impData = (float[]) imagePlus.getProcessor().getPixels();
            final float[] cellData = (float[]) cell.getStorageArray();
            System.arraycopy(impData, 0, cellData, 0, cellData.length);
        }
    }

    public String getFilePath( SerializableFileInfo fileInfo )
    {
        return directory + File.separator
                + fileInfo.directory + File.separator + fileInfo.fileName;
    }

    public long[] getDimensions() {
        return dimensions;
    }

    public int[] getCellDims() {
        return cellDims;
    }

}
