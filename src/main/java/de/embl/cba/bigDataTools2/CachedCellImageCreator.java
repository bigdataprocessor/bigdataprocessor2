package de.embl.cba.bigDataTools2;

import de.embl.cba.bigDataTools2.fileInfoSource.FileInfoSource;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.cache.img.DiskCachedCellImgOptions.CacheType;
import net.imglib2.cache.img.ReadOnlyCachedCellImgFactory;
import net.imglib2.cache.img.ReadOnlyCachedCellImgOptions;

import java.util.concurrent.ExecutorService;

import static net.imglib2.cache.img.ReadOnlyCachedCellImgOptions.options;

public class CachedCellImageCreator {

    public static CachedCellImg create(FileInfoSource fileInfoSource, ExecutorService executorService) {
        CachedCellImg cachedCellImg;
        ImageLoader loader = new ImageLoader(fileInfoSource);
        final ReadOnlyCachedCellImgOptions options = options()
                .cellDimensions(loader.getCellDims())
                .cacheType(CacheType.BOUNDED)
                .maxCacheSize(100);
        cachedCellImg = new ReadOnlyCachedCellImgFactory().create(loader.getDimensions(), fileInfoSource.getType(), loader, options);
        return cachedCellImg;

    }
}
