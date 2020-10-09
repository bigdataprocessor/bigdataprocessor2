package de.embl.cba.bdp2.open.fileseries;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.open.CacheUtils;
import de.embl.cba.bdp2.open.CachedCellImgCreator;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.cache.img.DiskCachedCellImgOptions;
import net.imglib2.cache.img.ReadOnlyCachedCellImgFactory;
import net.imglib2.cache.img.ReadOnlyCachedCellImgOptions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import java.io.File;

import static net.imglib2.cache.img.ReadOnlyCachedCellImgOptions.options;

public class FileSeriesCachedCellImgCreator< R extends RealType< R > & NativeType< R > > implements CachedCellImgCreator< R >
{
    private final FileInfos fileInfos;
    private int[] imageDimsXYZ;
    private String[] channelNames;
    private double[] voxelSize;
    private String voxelUnit;

    public FileSeriesCachedCellImgCreator( FileInfos fileInfos )
    {
        this.fileInfos = fileInfos;
        this.imageDimsXYZ = new int[]{ fileInfos.nX, fileInfos.nY, fileInfos.nZ };
        this.channelNames = fileInfos.channelNames;
        this.voxelSize = fileInfos.voxelSize;
        this.voxelUnit = fileInfos.voxelUnit;
    }

    public Image< R > createImage()
    {
        int[] cellDimsXYZCT = CacheUtils.planeWiseCellDims( imageDimsXYZ, fileInfos.bitDepth, CacheUtils.isPlaneWiseCompressed( fileInfos ) );
        CachedCellImg< R, ? > planeWiseCachedCellImg = createCachedCellImg( cellDimsXYZCT, DiskCachedCellImgOptions.CacheType.BOUNDED, 100 );

        Image< R > image = new Image(
                planeWiseCachedCellImg,
                new File( fileInfos.directory ).getName(),
                channelNames,
                voxelSize,
                voxelUnit );

        return image;
    }

    public CachedCellImg< R, ? > createCachedCellImg( int[] cellDimsXYZCT, DiskCachedCellImgOptions.CacheType cacheType, long cacheSize )
    {
        final FileSeriesCellLoader loader = new FileSeriesCellLoader<>( fileInfos, cellDimsXYZCT );

        final ReadOnlyCachedCellImgOptions options = options()
                .cellDimensions( loader.getCellDims() )
                .cacheType( cacheType )
                .maxCacheSize( cacheSize );

        final CachedCellImg cachedCellImg = new ReadOnlyCachedCellImgFactory().create(
                loader.getDimensions(),
                fileInfos.getType(),
                loader,
                options);

        return cachedCellImg;
    }

}
