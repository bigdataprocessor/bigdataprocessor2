package de.embl.cba.bigDataTools2.viewers;

import de.embl.cba.bigDataTools2.dataStreamingGUI.BdvMenus;
import de.embl.cba.bigDataTools2.dataStreamingGUI.DisplaySettings;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.realtransform.AffineTransform3D;

public interface ImageViewer {

    void show();

    RandomAccessibleInterval getRai();

    double[] getVoxelSize();

    String getImageName();

    ImageViewer newImageViewer();

    FinalInterval get5DIntervalFromUser();

    void show( RandomAccessibleInterval rai, String name, double[] voxelSize, String calibrationUnit, boolean autoContrast );

    void addMenus(BdvMenus menus);

    void setDisplayRange(double min, double max, int channel);

    DisplaySettings getDisplaySettings(int channel);

    void replicateViewerContrast(ImageViewer newImageView);

    int getCurrentTimePoint();

    void repaint(AffineTransform3D viewerTransform);

    void shiftImageToCenter(double[] centerCoordinates);

    void doAutoContrastPerChannel();

    String getCalibrationUnit();
}
