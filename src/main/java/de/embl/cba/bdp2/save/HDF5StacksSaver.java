package de.embl.cba.bdp2.save;

import de.embl.cba.bdp2.open.core.FileInfos;
import de.embl.cba.bdp2.utils.DimensionOrder;
import de.embl.cba.bdp2.log.progress.Progress;
import de.embl.cba.bdp2.utils.Utils;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class HDF5StacksSaver extends AbstractImgSaver {

    private SavingSettings savingSettings;
    private ExecutorService es;
    private AtomicBoolean stop;

    public HDF5StacksSaver( SavingSettings savingSettings, ExecutorService es) {
        this.savingSettings = savingSettings;
        this.es = es;
        this.stop = new AtomicBoolean(false);
    }

    @Override
    public void startSave() {
        List<Future> futures = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger(0);
        final long startTime = System.currentTimeMillis();
        long timeFrames = savingSettings.rai.dimension(DimensionOrder.T);
        NativeType imageType = Util.getTypeFromInterval(savingSettings.rai);
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
            }
        }
        // Monitor the progress
        Thread thread = new Thread(() -> Progress.informProgressListeners(futures,
                FileInfos.PROGRESS_UPDATE_MILLISECONDS, progressListeners ));
        thread.start();
    }

    @Override
    public void stopSave() {
        this.stop.set(true);
        Utils.shutdownThreadPack(es,TIME_OUT_SECONDS);
    }
}
