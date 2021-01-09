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
		int numIOThreads = Math.max( 1, Math.min( savingSettings.numIOThreads, MAX_THREAD_LIMIT ) );

		Logger.info( "\n# Save" );
		Logger.info( "Saving started; I/O threads: " + numIOThreads );

		ExecutorService saveExecutorService = Executors.newFixedThreadPool( numIOThreads );

		Image< R > imageForSaving = new Image<>( image ); // create a copy in order not to change the cache of the currently shown image

		if ( ! savingSettings.fileType.equals( SaveFileType.TIFFPlanes ) )
		{
			// TODO: for cropped images only fully load the cropped region
			// TODO: for input data distributed across TIFF planes this should be reconsidered
			long cacheSize = image.getDimensionsXYZCT()[ DimensionOrder.C ] * numIOThreads;
			imageForSaving.setVolumeCache( DiskCachedCellImgOptions.CacheType.BOUNDED, (int) cacheSize );
		}

		if ( savingSettings.saveVolumes )
			Utils.createFilePathParentDirectories( savingSettings.volumesFilePathStump );

		if ( savingSettings.saveProjections )
			Utils.createFilePathParentDirectories( savingSettings.projectionsFilePathStump );

		ImageSaverFactory factory = new ImageSaverFactory();
		saver = factory.getSaver( imageForSaving, savingSettings, saveExecutorService );
		saver.addProgressListener( progressListener );
	}

	public ImageSaver getSaver()
	{
		return saver;
	}
}
