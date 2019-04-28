package de.embl.cba.bdp2.loading;

import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.loading.files.FileInfos;
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

    // TODO: make this return an Image
    public static CachedCellImg asCachedCellImg( FileInfos fileInfos )
    {
        CachedCellImg cachedCellImg;

        ImageLoader loader = new ImageLoader( fileInfos );

        final ReadOnlyCachedCellImgOptions options = options()
                .cellDimensions(loader.getCellDims())
                .cacheType(CacheType.BOUNDED)
                .maxCacheSize(100).volatileAccesses( true );

        cachedCellImg = new ReadOnlyCachedCellImgFactory().create(
                loader.getDimensions(),
                fileInfos.getType(),
                loader,
                options);

        return cachedCellImg;
    }

    public static < R extends RealType< R > & NativeType< R > >
    Image< R > loadImage( FileInfos fileInfos )
    {
        CachedCellImg cachedCellImg;

        ImageLoader loader = new ImageLoader( fileInfos );

        final ReadOnlyCachedCellImgOptions options = options()
                .cellDimensions(loader.getCellDims())
                .cacheType(CacheType.BOUNDED)
                .maxCacheSize(100).volatileAccesses( true );

        cachedCellImg = new ReadOnlyCachedCellImgFactory().create(
                loader.getDimensions(),
                fileInfos.getType(),
                loader,
                options);

        final Image image = new Image<>(
                cachedCellImg,
                new File( fileInfos.directory ).getName(),
                fileInfos.voxelSpacing,
                fileInfos.unit
        );

        return image;

    }
}
