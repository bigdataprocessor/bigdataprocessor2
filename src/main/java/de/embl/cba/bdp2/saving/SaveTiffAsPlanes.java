package de.embl.cba.bdp2.saving;

import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.utils.DimensionOrder;
import de.embl.cba.bdp2.utils.MonitorThreadPoolStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public class SaveTiffAsPlanes extends AbstractImgSaver {

    private SavingSettings savingSettings;
    private ExecutorService es;
    private Integer saveId;

    public SaveTiffAsPlanes( SavingSettings savingSettings, ExecutorService es, Integer saveId ) {
        this.savingSettings = savingSettings;
        this.es = es;
        this.saveId = saveId;
    }

    public void startSave() {
        AtomicBoolean stop = new AtomicBoolean(false);
        //updateTrackers(saveId, stop);

        List<Future> futures = new ArrayList<>();

        final long numChannels = savingSettings.rai.dimension( DimensionOrder.C );
        final long numFrames = savingSettings.rai.dimension( DimensionOrder.T );
        final long numPlanes = savingSettings.rai.dimension( DimensionOrder.Z );

        for ( int c = 0; c < numChannels; c++) {
            for ( int t = 0; t < numFrames; t++) {
                for ( int z = 0; z < numPlanes; z++) {
                    futures.add(es.submit(
                            new SaveImgAsTIFFPlanes(c, t, z, savingSettings, stop)
                    ));
                }
            }
        }

        // Monitor the progress
        Thread thread = new Thread(() -> MonitorThreadPoolStatus.showProgressAndWaitUntilDone(
                futures,
              //  saveId,
                "Saved to disk: ",
                FileInfos.PROGRESS_UPDATE_MILLISECONDS,
                progressListener));
        thread.start();


    }

    @Override
    public void stopSave()
    {
        // kill all the futures and the
    }


}
