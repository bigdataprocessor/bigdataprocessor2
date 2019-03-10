package de.embl.cba.bdp2.viewers;

import de.embl.cba.bdp2.ui.BdvMenus;
import de.embl.cba.bdp2.ui.DisplaySettings;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public interface ImageViewer < R extends RealType< R > & NativeType< R > >  {

    void show();

    RandomAccessibleInterval< R > getRai();

    double[] getVoxelSize();

    String getImageName();

    ImageViewer newImageViewer();

    FinalInterval get5DIntervalFromUser();

    void show(
            RandomAccessibleInterval rai,
            String name,
            double[] voxelSize,
            String calibrationUnit,
            boolean autoContrast );

    void addMenus(BdvMenus menus);

    void setDisplayRange(double min, double max, int channel);

    DisplaySettings getDisplaySettings(int channel);

    void replicateViewerContrast(ImageViewer newImageView);

    int getCurrentTimePoint();

    void repaint(AffineTransform3D viewerTransform);

    void repaint();

    void shiftImageToCenter(double[] centerCoordinates);

    void doAutoContrastPerChannel();

    String getCalibrationUnit();
}
