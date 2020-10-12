package de.embl.cba.bdp2.save;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.log.progress.ProgressListener;
import de.embl.cba.bdp2.open.CacheUtils;
import de.embl.cba.bdp2.utils.DimensionOrder;
import de.embl.cba.bdp2.utils.Utils;
import net.imglib2.cache.img.DiskCachedCellImgOptions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static de.embl.cba.bdp2.BigDataProcessor2.MAX_THREAD_LIMIT;

public class ImageSaverCreator < R extends RealType< R > & NativeType< R > >
{
	private final ImageSaver saver;

	public ImageSaverCreator( Image< R > image, SavingSettings savingSettings, ProgressListener progressListener )
	{
		int numIOThreads = Math.max( 1, Math.min( savingSettings.numIOThreads, MAX_THREAD_LIMIT ));

		Logger.info( "Saving started; I/O threads: " + numIOThreads );

		ExecutorService saveExecutorService = Executors.newFixedThreadPool( numIOThreads );

		if ( ! savingSettings.saveFileType.equals( SavingSettings.SaveFileType.TIFF_PLANES ) )
		{
			// TODO: for cropped images only fully load the cropped region
			// TODO: for input data distributed across Tiff planes this should be reconsidered

			long cacheSize = image.getDimensionsXYZCT()[ DimensionOrder.C ] * numIOThreads;

			Logger.info( "Configuring volume reader with a cache size of " + cacheSize + " volumes." );

			image.setCache( CacheUtils.volumeWiseCellDims( image.getDimensionsXYZCT() ), DiskCachedCellImgOptions.CacheType.BOUNDED, 100 );

			savingSettings.rai = image.getRai();
		}
		else
		{
			savingSettings.rai = image.getRai();
		}

		// TODO: consider giving the whole image to the SavingSettings instead of the rai?
		savingSettings.type = Util.getTypeFromInterval( savingSettings.rai );
		savingSettings.channelNames = image.getChannelNames();
		savingSettings.voxelSize = image.getVoxelSize();
		savingSettings.voxelUnit = image.getVoxelUnit();
		ImgSaverFactory factory = new ImgSaverFactory();

		if ( savingSettings.saveVolumes )
			Utils.createFilePathParentDirectories( savingSettings.volumesFilePathStump );

		if ( savingSettings.saveProjections )
			Utils.createFilePathParentDirectories( savingSettings.projectionsFilePathStump );

		saver = factory.getSaver( savingSettings, saveExecutorService );
		saver.addProgressListener( progressListener );

		// TODO: reset cellDims to planeWise?
	}

	public ImageSaver getSaver()
	{
		return saver;
	}
}
