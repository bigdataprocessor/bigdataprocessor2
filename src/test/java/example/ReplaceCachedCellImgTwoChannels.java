package example;

import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.loading.CachedCellImgReader;
import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.saving.CachedCellImgReplacer;
import de.embl.cba.bdp2.ui.BigDataProcessor2;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.cache.util.LoaderCacheAsCacheAdapter;

public class ReplaceCachedCellImgTwoChannels
{
	public static void main( String[] args )
	{
		final BigDataProcessor2 bdp = new BigDataProcessor2();

		final String directory = "/Users/tischer/Documents/isabell-schneider-splitchipmerge/two_channels";

		final String loadingScheme = FileInfos.LOAD_CHANNELS_FROM_FOLDERS;
		final String filterPattern = ".*.h5";
		final String dataset = "Data";


//		final Image image = bdp.openHdf5Data(
//				directory,
//				loadingScheme,
//				filterPattern,
//				dataset );

		FileInfos fileInfos =
				new FileInfos(
						directory,
						loadingScheme,
						filterPattern,
						dataset );


		final CachedCellImg cachedCellImg = CachedCellImgReader.getCachedCellImg( fileInfos );

		final CachedCellImg cachedCellImg2 = CachedCellImgReader.getCachedCellImg(
				fileInfos,
				fileInfos.nX,
				fileInfos.nY,
				2 );


		final RandomAccessibleInterval replaced =
				new CachedCellImgReplacer( cachedCellImg, cachedCellImg2 ).getReplaced();


	}
}
