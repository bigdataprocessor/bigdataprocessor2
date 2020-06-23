package de.embl.cba.bdp2.save;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.log.progress.ProgressListener;
import de.embl.cba.bdp2.open.core.CachedCellImgReader;
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
		int nIOThread = Math.max( 1, Math.min( savingSettings.numIOThreads, MAX_THREAD_LIMIT ));

		Logger.info( "Saving started; I/O threads: " + nIOThread );

		ExecutorService saveExecutorService = Executors.newFixedThreadPool( nIOThread );

		if ( ! savingSettings.saveFileType.equals( SavingSettings.SaveFileType.TIFF_PLANES ) )
		{
			Logger.info( "Saving: Configuring volume reader..." );

			// TODO: for cropped images only fully load the cropped region
			// TODO: for input data distributed across Tiff planes this should be reconsidered
			final CachedCellImg< R, ? > volumeCachedCellImg
					= CachedCellImgReader.getVolumeCachedCellImg( image.getFileInfos() );
			final RandomAccessibleInterval< R > volumeLoadedRAI =
					new CachedCellImgReplacer( image.getRai(), volumeCachedCellImg ).get();
			savingSettings.rai = volumeLoadedRAI;
		}
		else
		{
			savingSettings.rai = image.getRai();
		}

		savingSettings.voxelSpacing = image.getVoxelSpacing();
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
