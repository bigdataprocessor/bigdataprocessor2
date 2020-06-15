package example;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.read.CachedCellImgReader;
import de.embl.cba.bdp2.read.FileInfos;
import de.embl.cba.bdp2.read.NamingScheme;
import de.embl.cba.bdp2.save.CachedCellImgReplacer;
import de.embl.cba.bdp2.BigDataProcessor2;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.CachedCellImg;

public class ReplaceCachedTwoChannelCellImg
{
	public static void main( String[] args )
	{
		final BigDataProcessor2 bdp = new BigDataProcessor2();

		final String directory = "/Users/tischer/Documents/isabell-schneider-splitchipmerge/two_channels";

		final String loadingScheme = NamingScheme.LOAD_CHANNELS_FROM_FOLDERS;
		final String filterPattern = ".*.h5";
		final String dataset = "Data";

		final Image image = bdp.openImageFromHdf5(
				directory,
				loadingScheme,
				filterPattern,
				dataset );

//		bdp.showImage( image );

		FileInfos fileInfos =
				new FileInfos(
						directory,
						loadingScheme,
						filterPattern,
						dataset );

		final CachedCellImg cachedCellImg = CachedCellImgReader
				.createCachedCellImg( fileInfos );

		final CachedCellImg cachedCellImg2 = CachedCellImgReader
				.getVolumeCachedCellImg(
				fileInfos );

		final RandomAccessibleInterval replaced =
				new CachedCellImgReplacer( cachedCellImg, cachedCellImg2 ).get();
	}
}
