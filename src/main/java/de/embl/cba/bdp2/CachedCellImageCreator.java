package de.embl.cba.bdp2;

import de.embl.cba.bdp2.files.FileInfos;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.cache.img.DiskCachedCellImgOptions.CacheType;
import net.imglib2.cache.img.ReadOnlyCachedCellImgFactory;
import net.imglib2.cache.img.ReadOnlyCachedCellImgOptions;

import java.util.concurrent.ExecutorService;

import static net.imglib2.cache.img.ReadOnlyCachedCellImgOptions.options;

public class CachedCellImageCreator {

    public static CachedCellImg create( FileInfos fileInfos, ExecutorService executorService) {
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
}
