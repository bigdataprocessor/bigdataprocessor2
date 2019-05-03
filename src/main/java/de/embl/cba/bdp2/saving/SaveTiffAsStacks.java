package de.embl.cba.bdp2.saving;

import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.utils.DimensionOrder;
import de.embl.cba.bdp2.utils.MonitorThreadPoolStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class SaveTiffAsStacks extends AbstractImgSaver {
    private SavingSettings savingSettings;
    private ExecutorService es;
    private Integer saveId;

    public SaveTiffAsStacks(SavingSettings savingSettings, ExecutorService es, Integer saveId) {
        this.savingSettings = savingSettings;
        this.es = es;
        this.saveId = saveId;
    }
    public void startSave() {
        List<Future> futures = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger(0);
        AtomicBoolean stop = new AtomicBoolean(false);
        //updateTrackers(saveId, stop);
        final long startTime = System.currentTimeMillis();
        long timeFrames = savingSettings.rai.dimension(DimensionOrder.T);
        for (int t = 0; t < timeFrames; t++) {
            futures.add(
                    es.submit(
                            new SaveImgAsTIFFStacks(t, savingSettings, counter, startTime,stop)
                    ));
        }


        // Monitor the progress
        Thread thread =
                new Thread(() -> MonitorThreadPoolStatus.showProgressAndWaitUntilDone(
                        futures, saveId, "Saved to disk: ", FileInfos.PROGRESS_UPDATE_MILLISECONDS));


        thread.start();
    }
}
