package de.embl.cba.bdp2.save.hdf5;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.open.fileseries.FileInfos;
import de.embl.cba.bdp2.save.AbstractImageSaver;
import de.embl.cba.bdp2.save.SavingSettings;
import de.embl.cba.bdp2.save.hdf5.FastHDF5StackWriter;
import de.embl.cba.bdp2.utils.DimensionOrder;
import de.embl.cba.bdp2.log.progress.Progress;
import de.embl.cba.bdp2.utils.Utils;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class HDF5StacksSaver < R extends RealType< R > & NativeType< R > > extends AbstractImageSaver
{
    private final Image< R > image;
    private SavingSettings savingSettings;
    private ExecutorService es;
    private AtomicBoolean stop;

    public HDF5StacksSaver( Image< R > image, SavingSettings savingSettings, ExecutorService es) {
        this.image = image;
        this.savingSettings = savingSettings;
        this.es = es;
        this.stop = new AtomicBoolean(false);
    }

    @Override
    public void startSave() {
        List<Future> futures = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger(0);
        final long startTime = System.currentTimeMillis();
        long timeFrames = image.getRai().dimension(DimensionOrder.T);
        for (int t = 0; t < timeFrames; t++) {
                futures.add(
                    es.submit(
                        new FastHDF5StackWriter<>("Data", image, savingSettings, t, counter, startTime, stop)
                    )
                );
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
