package de.embl.cba.bdp2.motioncorrection;

import bdv.util.*;
import de.embl.cba.bdp2.ui.BdvMenus;
import de.embl.cba.bdp2.ui.BigDataProcessor;
import de.embl.cba.bdp2.ui.DisplaySettings;
import de.embl.cba.bdp2.fileinfosource.FileInfoConstants;
import static de.embl.cba.bdp2.ui.BigDataProcessorCommand.logger;
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
import java.util.Optional;
import java.util.concurrent.Executors;

public class BigDataTracker {

    public ArrayList<Track> tracks = new ArrayList<>();
    public Track trackResults;
    public TrackingSettings trackingSettings;
    private ObjectTracker objectTracker;

    public BigDataTracker(){
        kickOffThreadPack(Runtime.getRuntime().availableProcessors()*2); //TODO: decide if this n threads is ok --ashis
    }

    public void kickOffThreadPack(int numThreads){
        if(null == BigDataProcessor.trackerThreadPool
                ||  BigDataProcessor.trackerThreadPool.isTerminated()){
            BigDataProcessor.trackerThreadPool = Executors.newFixedThreadPool(numThreads);
        }
    }

    public void shutdownThreadPack(){
        Utils.shutdownThreadPack(BigDataProcessor.trackerThreadPool,5);
    }

    public void trackObject(TrackingSettings trackingSettings, ImageViewer imageViewer)
    {   this.trackingSettings = trackingSettings;
        Point3D minInit = trackingSettings.pMin;
        Point3D maXinit = trackingSettings.pMax;
        this.objectTracker = new ObjectTracker(trackingSettings);
        this.trackResults = objectTracker.getTrackingPoints();
        if(!this.objectTracker.interruptTrackingThreads) {

            ImageViewer newTrackedView =  imageViewer.newImageViewer();
            newTrackedView.show(
                    trackingSettings.imageRAI,
                    FileInfoConstants.TRACKED_IMAGE_NAME,
                    imageViewer.getVoxelSize(),
                    imageViewer.getCalibrationUnit(),
                    false);
            imageViewer.replicateViewerContrast(newTrackedView);

            if(newTrackedView instanceof BdvImageViewer) {
                TrackedAreaBoxOverlay tabo = new TrackedAreaBoxOverlay(this.trackResults,
                        ((BdvHandleFrame) ((BdvImageViewer) newTrackedView).getBdvSS().getBdvHandle()).getBigDataViewer().getViewer(),
                        ((BdvHandleFrame) ((BdvImageViewer) newTrackedView).getBdvSS().getBdvHandle()).getBigDataViewer().getSetupAssignments(), 9991,
                        Intervals.createMinMax((long) minInit.getX(), (long) minInit.getY(), (long) minInit.getZ(), (long) maXinit.getX(), (long) maXinit.getY(), (long) maXinit.getZ()));
            }
        }
    }

    public< T extends RealType< T > & NativeType< T >> void showTrackedObjects(
            ImageViewer imageViewer)
    {
        if(trackResults!=null) {
            List<RandomAccessibleInterval<T>> tracks = new ArrayList<>();
            int nChannels = (int) trackingSettings.imageRAI.dimension( DimensionOrder.C );
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
                RandomAccessibleInterval trackedRegion = Views.interval((RandomAccessible) Views.extendZero(trackingSettings.imageRAI), trackedInterval); // Views.extendZero in case range is beyond the original image.
                RandomAccessibleInterval timeRemovedRAI = Views.zeroMin(Views.hyperSlice(trackedRegion,4, entry.getKey()));
                tracks.add(timeRemovedRAI);
            }
            RandomAccessibleInterval stackedRAI = Views.stack(tracks);
            ImageViewer newTrackedView = imageViewer.newImageViewer();
            newTrackedView.show(
                    stackedRAI,
                    FileInfoConstants.TRACKED_IMAGE_NAME,
                    imageViewer.getVoxelSize(),
                    imageViewer.getCalibrationUnit(),
                    false);
            newTrackedView.addMenus(new BdvMenus());
            for (int channel=0; channel<nChannels; ++channel){ // TODO: change to method replicateViewerContrast --ashis
                DisplaySettings setting = imageViewer.getDisplaySettings(channel);
                newTrackedView.setDisplayRange(setting.getMinValue(),setting.getMaxValue(),channel);
            }
        }
    }

    public void cancelTracking(){
        Optional<ObjectTracker> obj =  Optional.ofNullable(objectTracker);
        if(obj.isPresent()){
            trackResults = null;
            logger.info("Stopping all tracking...");
            this.objectTracker.interruptTrackingThreads = true;
        }else{
            logger.info("Cannot stop an unbegun process.");
        }
    }

}