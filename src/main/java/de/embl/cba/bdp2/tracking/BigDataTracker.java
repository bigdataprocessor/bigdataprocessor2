package de.embl.cba.bdp2.tracking;

import bdv.util.BdvHandleFrame;
import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.ui.BdvMenus;
import de.embl.cba.bdp2.ui.BigDataProcessor2;
import de.embl.cba.bdp2.ui.DisplaySettings;
import de.embl.cba.bdp2.utils.DimensionOrder;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import de.embl.cba.bdp2.viewers.ImageViewer;
import javafx.geometry.Point3D;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class BigDataTracker< R extends RealType< R > & NativeType< R > > {

    public Track< R > trackResults;
    public TrackingSettings< R > trackingSettings;

    public BigDataTracker(){
        kickOffThreadPack(Runtime.getRuntime().availableProcessors()*2); //TODO: decide if this n threads is ok --ashis
    }

    public void kickOffThreadPack(int numThreads){
        if(null == BigDataProcessor2.trackerThreadPool
                ||  BigDataProcessor2.trackerThreadPool.isTerminated()){
            BigDataProcessor2.trackerThreadPool = Executors.newFixedThreadPool(numThreads);
        }
    }

    public void shutdownThreadPack(){
        Utils.shutdownThreadPack( BigDataProcessor2.trackerThreadPool,5);
    }

    // TODO:
    // is the imageViewer needed???
    // separate image from settings
    public AbstractObjectTracker trackObject( TrackingSettings< R > trackingSettings, ImageViewer imageViewer )
    {
        this.trackingSettings = trackingSettings;
        Point3D minInit = trackingSettings.pMin;
        Point3D maXinit = trackingSettings.pMax;
        AtomicBoolean stop = new AtomicBoolean(false);
        AbstractObjectTracker objectTracker = new ObjectTracker(trackingSettings,stop);
        BigDataProcessor2.trackerThreadPool.submit(()-> {
                    this.trackResults = objectTracker.getTrackingPoints();
            if(!stop.get()) {
                ImageViewer newTrackedView =  imageViewer.newImageViewer();
                newTrackedView.show(
                        trackingSettings.rai,
                        FileInfos.TRACKED_IMAGE_NAME,
                        imageViewer.getImage().getVoxelSpacing(),
                        imageViewer.getImage().getVoxelUnit(),
                        false);
                imageViewer.replicateViewerContrast(newTrackedView);

                if(newTrackedView instanceof BdvImageViewer) {
                    TrackedAreaBoxOverlay tabo = new TrackedAreaBoxOverlay(this.trackResults,
                            ((BdvHandleFrame) ((BdvImageViewer) newTrackedView).getBdvStackSource().getBdvHandle()).getBigDataViewer().getViewer(),
                            ((BdvHandleFrame) ((BdvImageViewer) newTrackedView).getBdvStackSource().getBdvHandle()).getBigDataViewer().getSetupAssignments(), 9991,
                            Intervals.createMinMax((long) minInit.getX(), (long) minInit.getY(), (long) minInit.getZ(), (long) maXinit.getX(), (long) maXinit.getY(), (long) maXinit.getZ()));
                }
            }else{
                this.trackResults = null; // Qualifying trackResults for garbage collection.
            }
        });
        return objectTracker;
    }

    public< T extends RealType< T > & NativeType< T >> void showTrackedObjects(
            ImageViewer imageViewer)
    {
        if(trackResults!=null) {
            List<RandomAccessibleInterval<T>> tracks = new ArrayList<>();
            int nChannels = (int) trackingSettings.rai.dimension( DimensionOrder.C );
            for (Map.Entry<Integer, Point3D[]> entry : this.trackResults.locations.entrySet()) {
                Point3D[] pMinMax = entry.getValue();
                long[] range = {(long) pMinMax[0].getX(),
                                (long) pMinMax[0].getY(),
                                (long) pMinMax[0].getZ(),
                                0,
                                entry.getKey(),
                                (long) pMinMax[1].getX(),
                                (long) pMinMax[1].getY(),
                                (long) pMinMax[1].getZ(),
                                nChannels-1,
                                entry.getKey()}; //XYZCT order
                FinalInterval trackedInterval = Intervals.createMinMax(range);
                RandomAccessibleInterval trackedRegion = Views.interval((RandomAccessible) Views.extendZero(trackingSettings.rai ), trackedInterval); // Views.extendZero in case range is beyond the original image.
                RandomAccessibleInterval timeRemovedRAI = Views.zeroMin(Views.hyperSlice(trackedRegion,4, entry.getKey()));
                tracks.add(timeRemovedRAI);
            }
            RandomAccessibleInterval stackedRAI = Views.stack(tracks);
            ImageViewer newTrackedView = imageViewer.newImageViewer();
            newTrackedView.show(
                    stackedRAI,
                    FileInfos.TRACKED_IMAGE_NAME,
                    imageViewer.getImage().getVoxelSpacing(),
                    imageViewer.getImage().getVoxelUnit(),
                    false);
            newTrackedView.addMenus(new BdvMenus());
            for (int channel=0; channel<nChannels; ++channel){ // TODO: change to method replicateViewerContrast --ashis
                DisplaySettings setting = imageViewer.getAutoContrastDisplaySettings(channel);
                newTrackedView.setDisplayRange(setting.getMinValue(),setting.getMaxValue(),channel);
            }
        }
    }
}