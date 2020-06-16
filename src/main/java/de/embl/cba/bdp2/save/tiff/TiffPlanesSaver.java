package de.embl.cba.bdp2.save.tiff;

import de.embl.cba.bdp2.open.core.FileInfos;
import de.embl.cba.bdp2.save.AbstractImgSaver;
import de.embl.cba.bdp2.save.SavingSettings;
import de.embl.cba.bdp2.utils.DimensionOrder;
import de.embl.cba.bdp2.log.progress.Progress;
import de.embl.cba.bdp2.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public class TiffPlanesSaver extends AbstractImgSaver
{

    private SavingSettings savingSettings;
    private ExecutorService es;
    private AtomicBoolean stop;

    public TiffPlanesSaver( SavingSettings savingSettings, ExecutorService es ) {
        this.savingSettings = savingSettings;
        this.es = es;
        this.stop = new AtomicBoolean(false);
    }

    public void startSave() {
        List<Future> futures = new ArrayList<>();
        final long numChannels = savingSettings.rai.dimension(DimensionOrder.C);
        final long numFrames = savingSettings.rai.dimension(DimensionOrder.T);
        final long numPlanes = savingSettings.rai.dimension(DimensionOrder.Z);

        for (int c = 0; c < numChannels; c++) {
            for (int t = 0; t < numFrames; t++) {
                for (int z = 0; z < numPlanes; z++) {
                    futures.add( es.submit(
                            new TiffPlaneSaver( c, t, z, savingSettings, stop )
                    ));
                }
            }
        }

        // Monitor the progress
        Thread thread = new Thread(() -> Progress.informProgressListeners(
                futures,
                FileInfos.PROGRESS_UPDATE_MILLISECONDS,
				progressListeners ));

        thread.start();
    }

    @Override
    public void stopSave() {
        stop.set(true);
        Utils.shutdownThreadPack(es,TIME_OUT_SECONDS);
    }
}
