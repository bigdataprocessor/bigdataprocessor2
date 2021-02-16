package de.embl.cba.bdp2.save;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.log.progress.ProgressListener;
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
		ExecutorService executorService = Executors.newFixedThreadPool( savingSettings.numIOThreads );

		// create a copy in order not to change the cache
		// of the currently shown image
		Image< R > imageForSaving = new Image<>( image );

		// change cache to load the whole volume
		// as this is faster
		if ( ! savingSettings.fileType.equals( SaveFileType.TIFFPlanes ) )
		{
			// TODO: for cropped images only fully load the cropped region
			// TODO: for input data distributed across TIFF planes this should be reconsidered
			long cacheSize = image.getDimensionsXYZCT()[ DimensionOrder.C ] * savingSettings.numIOThreads;
			imageForSaving.setVolumeCache( DiskCachedCellImgOptions.CacheType.BOUNDED, (int) cacheSize );
		}

		saver = new ImageSaverFactory().getSaver( imageForSaving, savingSettings, executorService );
		saver.addProgressListener( progressListener );
	}

	public ImageSaver getSaver()
	{
		return saver;
	}
}
