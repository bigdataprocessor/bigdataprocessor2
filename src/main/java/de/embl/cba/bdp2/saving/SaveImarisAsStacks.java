package de.embl.cba.bdp2.saving;

import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.logging.Logger;
import de.embl.cba.bdp2.utils.DimensionOrder;
import de.embl.cba.bdp2.utils.MonitorThreadPoolStatus;
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

public class SaveImarisAsStacks extends AbstractImgSaver {
    private SavingSettings savingSettings;
    private ExecutorService es;
    private AtomicBoolean stop;

    public SaveImarisAsStacks(SavingSettings savingSettings, ExecutorService es) {
        this.savingSettings = savingSettings;
        this.es = es;
        this.stop = new AtomicBoolean(false);
    }

    @Override
    public void startSave() {
        List<Future> futures = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger(0);
        ImarisDataSet imarisDataSetProperties = getImarisDataSet(savingSettings, stop);
        final long startTime = System.currentTimeMillis();
        long timeFrames = savingSettings.rai.dimension(DimensionOrder.T);
        NativeType imageType = Util.getTypeFromInterval(savingSettings.rai);
        for (int t = 0; t < timeFrames; t++) {
            if (imageType instanceof UnsignedByteType) {
                futures.add(es.submit(
                        new SaveImgAsIMARIS<UnsignedByteType>(savingSettings, imarisDataSetProperties, t, counter, startTime, stop)
                ));
            } else if (imageType instanceof UnsignedShortType) {
                futures.add(
                        es.submit(
                                new SaveImgAsIMARIS<UnsignedShortType>(
                                        savingSettings,
                                        imarisDataSetProperties,
                                        t,
                                        counter,
                                        startTime,
                                        stop)
                        ));
            } else if (imageType instanceof FloatType) {
                futures.add(
                        es.submit(
                                new SaveImgAsIMARIS<FloatType>(savingSettings, imarisDataSetProperties, t, counter, startTime, stop)
                        ));
            }
        }
        // Monitor the progress
        Thread thread = new Thread(() -> MonitorThreadPoolStatus.showProgressAndWaitUntilDone(futures,
                FileInfos.PROGRESS_UPDATE_MILLISECONDS, progressListener));
        thread.start();
    }

    @Override
    public void stopSave() {
        this.stop.set(true);
        Utils.shutdownThreadPack(es,TIME_OUT_SECONDS);
    }

    private ImarisDataSet getImarisDataSet(SavingSettings savingSettings, AtomicBoolean stop) {

        ImagePlus image = Utils.wrapToCalibratedImagePlus(
                savingSettings.rai,
                savingSettings.voxelSpacing,
                savingSettings.voxelUnit,
                "wrapped");

        String[] binnings = savingSettings.bin.split(";");
        int[] binning = Utils.delimitedStringToIntegerArray(binnings[0], ",");

        ImarisDataSet imarisDataSet = new ImarisDataSet(
                image,
                binning,
                savingSettings.parentDirectory,
                savingSettings.fileBaseNameIMARIS);

        imarisDataSet.setLogger(new de.embl.cba.logging.IJLazySwingLogger());

        if (stop.get()) {
            return null;
        }

        ImarisWriter.writeHeaderFile(
                imarisDataSet,
                savingSettings.parentDirectory,
                savingSettings.fileBaseNameIMARIS + ".ims"
        );

        ArrayList<File> imarisFiles = ImarisUtils.getImarisFiles(savingSettings.parentDirectory);
        if (imarisFiles.size() > 1) {
            ImarisWriter.writeCombinedHeaderFile(imarisFiles, "meta.ims");
        }

        // TODO: remove below
//        ImarisWriter.writeHeaderFile(
//                imarisDataSet, savingSettings.parentDirectory,
//                savingSettings.fileBaseNameIMARIS + ".h5");

        Logger.info("Image sizes at different resolutions:");
        Utils.logArrayList(imarisDataSet.getDimensions());
        Logger.info("Image chunking:");
        Utils.logArrayList(imarisDataSet.getChunks());
        return imarisDataSet;
    }
}
