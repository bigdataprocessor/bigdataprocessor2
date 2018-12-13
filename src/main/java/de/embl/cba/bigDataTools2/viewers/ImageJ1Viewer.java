package de.embl.cba.bigDataTools2.viewers;

import de.embl.cba.bigDataTools2.dataStreamingGUI.BdvMenus;
import de.embl.cba.bigDataTools2.dataStreamingGUI.DisplaySettings;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.realtransform.AffineTransform3D;

public class ImageJ1Viewer implements ImageViewer {
    @Override
    public void show() {

    }

    @Override
    public RandomAccessibleInterval getRai() {
        return null;
    }

    @Override
    public String getStreamName() {
        return null;
    }

    @Override
    public FinalInterval get5DIntervalFromUser() {
        return null;
    }

    @Override
    public void setRai(RandomAccessibleInterval rai) {

    }

    @Override
    public void setStreamName(String streamName) {

    }

    @Override
    public ImageViewer newImageViewer(RandomAccessibleInterval rai, String streamName) {
        return null;
    }

    @Override
    public void addMenus(BdvMenus menus) {

    }

    @Override
    public void setDisplayRange(double min, double max, int channel) {

    }

    @Override
    public DisplaySettings getDisplaySettings(int channel) {
        return null;
    }

    @Override
    public void replicateViewerContrast(ImageViewer newImageView) {

    }

    @Override
    public int getCurrentTimePoint() {
        return 0;
    }

    @Override
    public void repaint(AffineTransform3D viewerTransform) {

    }

}
