package de.embl.cba.bigDataTools2.bigDataTrackerGUI;

import bdv.util.*;
import de.embl.cba.bigDataTools2.dataStreamingGUI.BdvMenus;
import de.embl.cba.bigDataTools2.dataStreamingGUI.BigDataConverter;
import de.embl.cba.bigDataTools2.dataStreamingGUI.DisplaySettings;
import de.embl.cba.bigDataTools2.fileInfoSource.FileInfoConstants;
import de.embl.cba.bigDataTools2.logging.IJLazySwingLogger;
import de.embl.cba.bigDataTools2.logging.Logger;
import de.embl.cba.bigDataTools2.utils.Utils;
import de.embl.cba.bigDataTools2.viewers.BdvImageViewer;
import de.embl.cba.bigDataTools2.viewers.ImageViewer;
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

    private Logger logger = new IJLazySwingLogger();
    public ArrayList<Track> tracks = new ArrayList<>();
    public Track trackResults;
    public TrackingSettings trackingSettings;
    private ObjectTracker objectTracker;
    public BigDataTracker(){
        System.out.println("BigDataTracker constructor");
        kickOffThreadPack(Runtime.getRuntime().availableProcessors()*2); //TODO: decide if this n threads is ok --ashis
    }

    public void kickOffThreadPack(int nIOthreads){
        if(null == BigDataConverter.trackerThreadPool ||  BigDataConverter.trackerThreadPool.isTerminated()){
            BigDataConverter.trackerThreadPool = Executors.newFixedThreadPool(nIOthreads);
        }
    }

    public void shutdownThreadPack(){
        Utils.shutdownThreadPack(BigDataConverter.trackerThreadPool,5);
    }

    public void trackObject(TrackingSettings trackingSettings, ImageViewer imageViewer)
    {   this.trackingSettings = trackingSettings;
        Point3D minInit = trackingSettings.pMin;
        Point3D maXinit = trackingSettings.pMax;
        this.objectTracker = new ObjectTracker(trackingSettings);
        this.trackResults = objectTracker.getTrackingPoints();
        if(!this.objectTracker.interruptTrackingThreads) {

            ImageViewer newTrackedView =  imageViewer.newImageViewer();
            newTrackedView.show( trackingSettings.imageRAI, imageViewer.getVoxelSize(), FileInfoConstants.TRACKED_STREAM_NAME,false);//No need to add Menus.
            imageViewer.replicateViewerContrast(newTrackedView);

            if(newTrackedView instanceof BdvImageViewer) {
                TrackedAreaBoxOverlay tabo = new TrackedAreaBoxOverlay(this.trackResults,
                        ((BdvHandleFrame) ((BdvImageViewer) newTrackedView).getBdvSS().getBdvHandle()).getBigDataViewer().getViewer(),
                        ((BdvHandleFrame) ((BdvImageViewer) newTrackedView).getBdvSS().getBdvHandle()).getBigDataViewer().getSetupAssignments(), 9991,
                        Intervals.createMinMax((long) minInit.getX(), (long) minInit.getY(), (long) minInit.getZ(), (long) maXinit.getX(), (long) maXinit.getY(), (long) maXinit.getZ()));
            }
        }
    }

    public< T extends RealType< T > & NativeType< T >> void showTrackedObjects(ImageViewer imageViewer){
        if(trackResults!=null) {
            List<RandomAccessibleInterval<T>> tracks = new ArrayList<>();
            int nChannels = (int) trackingSettings.imageRAI.dimension(FileInfoConstants.C );
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
            newTrackedView.show( stackedRAI, trackingSettings.voxelSize, FileInfoConstants.TRACKED_STREAM_NAME,false);
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