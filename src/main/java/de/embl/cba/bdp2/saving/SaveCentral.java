package de.embl.cba.bdp2.saving;

import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.logging.Logger;
import de.embl.cba.bdp2.utils.DimensionOrder;
import de.embl.cba.bdp2.ui.BigDataProcessor2;
import de.embl.cba.imaris.ImarisDataSet;
import de.embl.cba.imaris.ImarisUtils;
import de.embl.cba.imaris.ImarisWriter;
import de.embl.cba.bdp2.utils.MonitorThreadPoolStatus;
import de.embl.cba.bdp2.utils.Utils;
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

public class SaveCentral {

    // TODO: remove the image from the settings
    public static void goSave(SavingSettings savingSettings, ExecutorService es, int saveId) {
        if (savingSettings.fileType.equals( SavingSettings.FileType.TIFF_PLANES )) {
            saveTIFFAsPlanes(savingSettings, es, saveId);
        } else if (savingSettings.fileType.equals( SavingSettings.FileType.TIFF_STACKS )) {
            saveTIFFAsStacks(savingSettings, es, saveId);
        } else if (savingSettings.fileType.equals( SavingSettings.FileType.HDF5_STACKS )) {
            saveHDFStacks(savingSettings, es, saveId);
        } else if (savingSettings.fileType.equals( SavingSettings.FileType.IMARIS_STACKS )) {
            saveIMARIStacks(savingSettings, es, saveId);
        }
    }

    private static void saveTIFFAsPlanes(
            SavingSettings savingSettings, ExecutorService es, Integer saveId )
    {
        AtomicBoolean stop = new AtomicBoolean(false);
        updateTrackers(saveId, stop);
        List<Future> futures = new ArrayList<>();
        for ( int c = 0; c < savingSettings.rai.dimension(DimensionOrder.C); c++) {
            for ( int t = 0; t < savingSettings.rai.dimension(DimensionOrder.T); t++) {
                for ( int z = 0; z < savingSettings.rai.dimension(DimensionOrder.Z); z++) {
                    futures.add(es.submit(
                            new SaveImgAsTIFFPlanes(c, t, z, savingSettings, stop)
                    ));
                }
            }
        }
        // Monitor the progress
        Thread thread = new Thread(() -> MonitorThreadPoolStatus.showProgressAndWaitUntilDone(futures, saveId, "Saved to disk: ", FileInfos.PROGRESS_UPDATE_MILLISECONDS));
        thread.start();
    }

    private static void saveTIFFAsStacks(SavingSettings savingSettings, ExecutorService es, Integer saveId) {
        saveTIFFForEachChannelAndTimePoint(savingSettings, es, saveId);
    }

    private static void saveHDFStacks(SavingSettings savingSettings, ExecutorService es, Integer saveId) {
        saveHDF5ForEachChannelAndTimePoint(savingSettings, es, saveId);
    }

    private static void saveIMARIStacks(SavingSettings savingSettings, ExecutorService es, Integer saveId) {
        saveIMARISForEachChannelAndTimePoint(savingSettings, es, saveId);
    }

    private static void saveTIFFForEachChannelAndTimePoint(SavingSettings savingSettings, ExecutorService es, Integer saveId) {
        List<Future> futures = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger(0);
        AtomicBoolean stop = new AtomicBoolean(false);
        updateTrackers(saveId, stop);
        final long startTime = System.currentTimeMillis();
        long timeFrames = savingSettings.rai.dimension(DimensionOrder.T);
        for (int t = 0; t < timeFrames; t++) {
            futures.add(
                    es.submit(
                            new SaveImgAsTIFFStacks(t, savingSettings, counter, startTime,stop)
                    ));
        }
        // Monitor the progress
        Thread thread = new Thread(() -> MonitorThreadPoolStatus.showProgressAndWaitUntilDone(futures, saveId, "Saved to disk: ", FileInfos.PROGRESS_UPDATE_MILLISECONDS));
        thread.start();
    }

    private static void saveHDF5ForEachChannelAndTimePoint(SavingSettings savingSettings, ExecutorService es, Integer saveId) {
        List<Future> futures = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger(0);
        AtomicBoolean stop = new AtomicBoolean(false);
        updateTrackers(saveId, stop);
        final long startTime = System.currentTimeMillis();
        long timeFrames = savingSettings.rai.dimension(DimensionOrder.T);
        NativeType imageType = Util.getTypeFromInterval(savingSettings.rai );
        for (int t = 0; t < timeFrames; t++) {
            if (imageType instanceof UnsignedByteType) {
                futures.add(es.submit(
                        new FastHDF5StackWriter<UnsignedByteType>("Data", savingSettings, t, counter, startTime, stop)
                ));
            } else if (imageType instanceof UnsignedShortType) {
                futures.add(es.submit(
                        new FastHDF5StackWriter<UnsignedShortType>("Data", savingSettings, t, counter, startTime, stop)
                ));
            } else if (imageType instanceof FloatType) {
                futures.add(es.submit(
                        new FastHDF5StackWriter<FloatType>("Data", savingSettings, t, counter, startTime, stop)
                ));
            } else {
                // throw Type not found exception : TODO --ashis
            }
        }
        // Monitor the progress
        Thread thread = new Thread(() -> MonitorThreadPoolStatus.showProgressAndWaitUntilDone(futures, saveId, "Saved to disk: ", FileInfos.PROGRESS_UPDATE_MILLISECONDS));
        thread.start();
    }


    private static void saveIMARISForEachChannelAndTimePoint(
            SavingSettings savingSettings,
            ExecutorService es, Integer saveId) {
        List<Future> futures = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger(0);
        AtomicBoolean stop = new AtomicBoolean(false);
        updateTrackers(saveId, stop);
        ImarisDataSet imarisDataSetProperties = getImarisDataSet(savingSettings,stop);
        final long startTime = System.currentTimeMillis();
        long timeFrames = savingSettings.rai.dimension(DimensionOrder.T);
        NativeType imageType = Util.getTypeFromInterval(savingSettings.rai );
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
                                new SaveImgAsIMARIS<FloatType>(savingSettings, imarisDataSetProperties, t, counter, startTime,stop)
                        ));
            }
        }
        // Monitor the progress
        Thread thread = new Thread(() -> MonitorThreadPoolStatus.showProgressAndWaitUntilDone(futures, saveId, "Saved to disk: ", FileInfos.PROGRESS_UPDATE_MILLISECONDS));
        thread.start();
    }

    private static ImarisDataSet getImarisDataSet(SavingSettings savingSettings,AtomicBoolean stop) {

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

    private static void updateTrackers(Integer saveId, AtomicBoolean stop) {
        BigDataProcessor2.saveTracker.put(saveId, stop);
        BigDataProcessor2.progressTracker.put(saveId, 0);
    }
}
