package de.embl.cba.bigDataTools2.viewers;

import de.embl.cba.bigDataTools2.dataStreamingGUI.BdvMenus;
import de.embl.cba.bigDataTools2.dataStreamingGUI.DisplaySettings;
import ij.ImagePlus;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class IJ1ImageViewer<T extends RealType<T> & NativeType<T> > implements ImageViewer {

    private ImagePlus imp;
    private RandomAccessibleInterval rai;

    public IJ1ImageViewer()
    {
    }

    @Override
    public void show() {
        imp.show();
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
    public void setRai( RandomAccessibleInterval rai ) {
        this.rai = rai;
        imp = ImageJFunctions.wrap( rai, "" );
    }

    @Override
    public void setImageName( String streamName) {
        imp.setTitle( streamName );
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

        imp.setDisplayRange( min, max, channel );

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

    @Override
    public void repaint(RandomAccessibleInterval rai, String newStreamName) {

    }

    @Override
    public void shiftImageToCenter(double[] centerCoordinates) {

    }

}
