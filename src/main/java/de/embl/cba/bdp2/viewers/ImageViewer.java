package de.embl.cba.bdp2.viewers;

import de.embl.cba.bdp2.RaiPlus;
import de.embl.cba.bdp2.ui.BdvMenus;
import de.embl.cba.bdp2.ui.DisplaySettings;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public interface ImageViewer < R extends RealType< R > & NativeType< R > >  {

    void show();

    void show(RaiPlus< R > raiPlus, boolean autoContrast);

    RaiPlus< R > getRaiPlus();

    ImageViewer newImageViewer();

    FinalInterval get5DIntervalFromUser();

    void addMenus(BdvMenus menus);

    void setDisplayRange(double min, double max, int channel);

    DisplaySettings getDisplaySettings(int channel);

    void replicateViewerContrast(ImageViewer newImageView);

    int getCurrentTimePoint();

    void repaint(AffineTransform3D viewerTransform);

    void repaint();

    void shiftImageToCenter(double[] centerCoordinates);

    void doAutoContrastPerChannel();

    AffineTransform3D getViewerTransform();

    void setViewerTransform( AffineTransform3D viewerTransform );

}
