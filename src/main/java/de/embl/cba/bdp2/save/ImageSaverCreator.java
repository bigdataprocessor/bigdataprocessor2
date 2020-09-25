package de.embl.cba.bdp2.save;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.log.progress.ProgressListener;
import de.embl.cba.bdp2.open.core.CachedCellImgReader;
import de.embl.cba.bdp2.utils.DimensionOrder;
import de.embl.cba.bdp2.utils.Utils;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

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

			long cacheSize = image.getVoxelDimensionsXYZCT()[ DimensionOrder.C ] * numIOThreads;

			Logger.info( "Configuring volume reader with a cache size of " + cacheSize + " volumes." );
			Logger.info( "Given the size of one volume this will amount to a memory load of about " + cacheSize * image.getSizeGB() + " GB." );

			final CachedCellImg< R, ? > volumeCachedCellImg = CachedCellImgReader.createVolumeCachedCellImg( image.getFileInfos(), cacheSize );
			final RandomAccessibleInterval< R > volumeLoadedRAI = new CachedCellImgReplacer( image.getRai(), volumeCachedCellImg ).get();
			savingSettings.rai = volumeLoadedRAI;
		}
		else
		{
			savingSettings.rai = image.getRai();
		}

		// TODO: consider giving the whole image to the SavingSettings instead of the rai?
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
	}

	public ImageSaver getSaver()
	{
		return saver;
	}
}
