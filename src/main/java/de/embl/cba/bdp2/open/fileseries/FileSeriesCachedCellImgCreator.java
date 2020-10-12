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

import static de.embl.cba.bdp2.open.CacheUtils.isPlaneWiseCompressed;
import static net.imglib2.cache.img.ReadOnlyCachedCellImgOptions.options;

public class FileSeriesCachedCellImgCreator< R extends RealType< R > & NativeType< R > > implements CachedCellImgCreator< R >
{
    private final FileInfos fileInfos;
    private final String imageName;
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
        this.imageName = new File( fileInfos.directory ).getName();
    }

    public Image< R > createImage()
    {
        Image< R > image = new Image(
                this,
                new File( fileInfos.directory ).getName(),
                channelNames,
                voxelSize,
                voxelUnit );

        return image;
    }

    @Override
    public String getImageName()
    {
        return imageName;
    }

    @Override
    public String[] getChannelNames()
    {
        return channelNames;
    }

    @Override
    public double[] getVoxelSize()
    {
        return voxelSize;
    }

    @Override
    public String getVoxelUnit()
    {
        return voxelUnit;
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

    @Override
    public boolean isPlaneWiseChunked()
    {
        return isPlaneWiseCompressed( fileInfos );
    }


}
