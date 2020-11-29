package de.embl.cba.bdp2.save.tiff;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.log.progress.Progress;
import de.embl.cba.bdp2.open.fileseries.FileInfos;
import de.embl.cba.bdp2.save.AbstractImageSaver;
import de.embl.cba.bdp2.save.SavingSettings;
import de.embl.cba.bdp2.utils.DimensionOrder;
import de.embl.cba.bdp2.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public class TiffPlanesSaver extends AbstractImageSaver
{
    private final Image image;
    private SavingSettings settings;
    private ExecutorService es;
    private AtomicBoolean stop;

    public TiffPlanesSaver( Image image, SavingSettings settings, ExecutorService es ) {
        this.image = image;
        this.settings = settings;
        this.es = es;
        this.stop = new AtomicBoolean(false);
    }

    public void startSave() {
        List<Future> futures = new ArrayList<>();
        final long numChannels = image.getRai().dimension(DimensionOrder.C);
        final long numFrames = image.getRai().dimension(DimensionOrder.T);
        final long numPlanes = image.getRai().dimension(DimensionOrder.Z);

        for (int c = 0; c < numChannels; c++) {
            for (int t = 0; t < numFrames; t++) {
                for (int z = 0; z < numPlanes; z++) {
                    futures.add( es.submit(
                            new TiffPlaneSaver( c, t, z, image, settings, stop )
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
