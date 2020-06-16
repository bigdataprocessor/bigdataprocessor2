package de.embl.cba.bdp2.save;

import de.embl.cba.bdp2.open.core.FileInfos;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.utils.DimensionOrder;
import de.embl.cba.bdp2.log.progress.Progress;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.imaris.ImarisDataSet;
import de.embl.cba.imaris.ImarisUtils;
import de.embl.cba.imaris.ImarisWriter;
import ij.ImagePlus;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ImarisStacksSaver extends AbstractImgSaver {
    private SavingSettings savingSettings;
    private ExecutorService es;
    private AtomicBoolean stop;

    public ImarisStacksSaver( SavingSettings savingSettings, ExecutorService es) {
        this.savingSettings = savingSettings;
        this.es = es;
        this.stop = new AtomicBoolean(false);
    }

    @Override
    public void startSave() {
        List<Future> futures = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger(0);
        ImarisDataSet imarisDataSetProperties = getImarisDataSet( savingSettings, stop );
        final long startTime = System.currentTimeMillis();
        long timeFrames = savingSettings.rai.dimension(DimensionOrder.T);
        NativeType imageType = Util.getTypeFromInterval(savingSettings.rai);

        for (int t = 0; t < timeFrames; t++) {
            if (imageType instanceof UnsignedByteType)
            {
                futures.add(es.submit(
                        new SaveFrameAsImarisVolumes<UnsignedByteType>(
                                savingSettings,
                                imarisDataSetProperties,
                                t, counter, startTime, stop)
                ));
            }
            else if (imageType instanceof UnsignedShortType)
            {
                futures.add(
                        es.submit(
                                new SaveFrameAsImarisVolumes<UnsignedShortType>(
                                        savingSettings,
                                        imarisDataSetProperties,
                                        t, counter, startTime, stop)
                        ));
            } else if (imageType instanceof FloatType) {
                futures.add(
                        es.submit(
                                new SaveFrameAsImarisVolumes<FloatType>(
                                        savingSettings,
                                        imarisDataSetProperties,
                                        t, counter, startTime, stop)
                        ));
            }
        }

        // Monitor the progress
        // Todo: one could also use the counter for the progress, rather than the futures
        Thread thread = new Thread(() -> Progress.informProgressListeners(
                futures,
                FileInfos.PROGRESS_UPDATE_MILLISECONDS,
				progressListeners ));
        thread.start();
    }

    @Override
    public void stopSave() {
        this.stop.set(true);
        Utils.shutdownThreadPack( es, TIME_OUT_SECONDS );
    }

    private ImarisDataSet getImarisDataSet( SavingSettings settings, AtomicBoolean stop ) {

        final String directory = new File( settings.volumesFilePathStump ).getParent();
        final String filename = new File( settings.volumesFilePathStump ).getName();

        ImagePlus image = Utils.wrap5DRaiToCalibratedImagePlus(
                settings.rai,
                settings.voxelSpacing,
                settings.voxelUnit,
                "wrapped");

        int[] binning = new int[]{1,1,1};

        ImarisDataSet imarisDataSet = new ImarisDataSet(
                image,
                binning,
                directory,
                filename );

        imarisDataSet.setLogger( new de.embl.cba.logging.IJLazySwingLogger() );

        if (stop.get())  return null;

        ImarisWriter.writeHeaderFile(
                imarisDataSet,
                directory,
                filename + ".ims"
        );

        ArrayList<File> imarisFiles = ImarisUtils.getImarisFiles( directory );

        if (imarisFiles.size() > 1)
            ImarisWriter.writeCombinedHeaderFile(imarisFiles, "meta.ims");


        Logger.info("Image sizes at different resolutions:");
        Utils.logArrayList(imarisDataSet.getDimensions());
        Logger.info("Image chunking:");
        Utils.logArrayList(imarisDataSet.getChunks());
        return imarisDataSet;
    }
}
