package de.embl.cba.bigDataTools2.viewers;

import de.embl.cba.bigDataTools2.dataStreamingGUI.BdvMenus;
import de.embl.cba.bigDataTools2.dataStreamingGUI.DisplaySettings;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.realtransform.AffineTransform3D;

public interface ImageViewer {

    void show();

    RandomAccessibleInterval getRai();

    String getImageName();

    FinalInterval get5DIntervalFromUser();

    void setRai(RandomAccessibleInterval rai);

    void setImageName( String streamName);

    ImageViewer newImageViewer(RandomAccessibleInterval rai, String streamName);

    void addMenus(BdvMenus menus);

    void setDisplayRange(double min, double max, int channel);

    DisplaySettings getDisplaySettings(int channel);

    void replicateViewerContrast(ImageViewer newImageView);

    int getCurrentTimePoint();

    void repaint(AffineTransform3D viewerTransform);

    void replace(RandomAccessibleInterval rai, String newStreamName);

    void shiftImageToCenter(double[] centerCoordinates);

}
