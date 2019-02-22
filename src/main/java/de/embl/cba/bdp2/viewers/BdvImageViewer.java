package de.embl.cba.bdp2.viewers;

import bdv.tools.brightness.ConverterSetup;
import bdv.util.AxisOrder;
import bdv.util.BdvFunctions;
import bdv.util.BdvHandleFrame;
import bdv.util.BdvOptions;
import bdv.util.BdvStackSource;
import bdv.util.volatiles.VolatileViews;
import bdv.viewer.SourceAndConverter;
import de.embl.cba.bdp2.boundingbox.BoundingBoxDialog;
import de.embl.cba.bdp2.ui.BdvMenus;
import de.embl.cba.bdp2.ui.DisplaySettings;
import de.embl.cba.bdp2.fileinfosource.FileInfoConstants;
import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import javax.swing.*;
import static de.embl.cba.bdp2.fileinfosource.FileInfoConstants.*;

public class BdvImageViewer<T extends RealType<T> & NativeType<T>> implements ImageViewer {

    private RandomAccessibleInterval<T> rai;
    private double[] voxelSize;
    private String imageName;

    private BdvStackSource< T > bdvSS;
    private String calibrationUnit;

    public BdvImageViewer()
    {

    }

    // TODO: wrap RAI into a "PhysicalImg" with voxelSize and Calibration
    public BdvImageViewer(
            RandomAccessibleInterval<T> rai,
            String imageName,
            double[] voxelSize,
            String calibrationUnit )
    {
        this.imageName = imageName;
        this.rai = rai;
        this.voxelSize = voxelSize;
        this.calibrationUnit = calibrationUnit;
    }


    @Override
    public FinalInterval get5DIntervalFromUser() {
        BoundingBoxDialog showBB = new BoundingBoxDialog(this.bdvSS.getBdvHandle());
        //showBB.show( rai, voxelSize, BB_TRACK_BUTTON_LABEL,true);
        showBB.show( rai, voxelSize);
        long[] minMax = {
                (long) ( showBB.selectedMin[ BoundingBoxDialog.X ] / voxelSize[ X ] ),
                (long) ( showBB.selectedMin[ BoundingBoxDialog.Y ] / voxelSize[ Y ] ),
                (long) ( showBB.selectedMin[ BoundingBoxDialog.Z ] / voxelSize[ Z ] ),
                rai.min( C ),
                showBB.selectedMin[ BoundingBoxDialog.T ],
                (long) ( showBB.selectedMax[ BoundingBoxDialog.X ] / voxelSize[ X ] ),
                (long) ( showBB.selectedMax[ BoundingBoxDialog.Y ] / voxelSize[ Y ] ),
                (long) ( showBB.selectedMax[ BoundingBoxDialog.Z ] / voxelSize[ Z ] ),
                rai.max( C ),
                showBB.selectedMax[ BoundingBoxDialog.T ]};

        return Intervals.createMinMax( minMax );

    }

    @Override
    public ImageViewer newImageViewer( ) {
        return new BdvImageViewer<T>( );
    }

    @Override
    public RandomAccessibleInterval<T> getRai() {
        return rai;
    }

    @Override
    public double[] getVoxelSize()
    {
        return voxelSize;
    }

    @Override
    public String getImageName() {
        return imageName;
    }


    @Override
    public void repaint( AffineTransform3D viewerTransform )
    {
        this.bdvSS.getBdvHandle().getViewerPanel().setCurrentViewerTransform( viewerTransform );
    }

    @Override
    public void show( )
    {
        showImageInViewer( rai, imageName, voxelSize, calibrationUnit );
    }

    @Override
    public void show(
            RandomAccessibleInterval rai,
            String imageName,
            double[] voxelSize,
            String calibrationUnit,
            boolean autoContrast )
    {
        if ( this.bdvSS != null ){
            removeAllSourcesFromBdv();
        }
        showImageInViewer( rai, imageName, voxelSize, calibrationUnit );
        if(autoContrast) {
            doAutoContrastPerChannel();
        }
    }


    private void removeAllSourcesFromBdv()
    {
        SourceAndConverter scnv = this.bdvSS.getBdvHandle().getViewerPanel().getState().getSources().get(0);
        this.bdvSS.getBdvHandle().getViewerPanel().removeSource( scnv.getSpimSource() );

        int nChannels = (int) this.getRai().dimension( C );
        for (int channel = 0; channel < nChannels; ++channel)
        {
			ConverterSetup converterSetup = this.getBdvSS().getBdvHandle().getSetupAssignments().getConverterSetups().get( channel );
			this.bdvSS.getBdvHandle().getSetupAssignments().removeSetup( converterSetup );
		}
    }

    public void addMenus(BdvMenus menus) {
        menus.setImageViewer(this);
        for (JMenu menu : menus.getMenus()) {
            ((BdvHandleFrame) this.bdvSS.getBdvHandle()).getBigDataViewer().getViewerFrame().getJMenuBar().add((menu));
        }
        ((BdvHandleFrame) this.bdvSS.getBdvHandle()).getBigDataViewer().getViewerFrame().getJMenuBar().updateUI();
    }

    @Override
    public void setDisplayRange(double min, double max, int channel) {
        final ConverterSetup converterSetup = this.bdvSS.getBdvHandle().getSetupAssignments().getConverterSetups().get(channel);
        this.bdvSS.getBdvHandle().getSetupAssignments().removeSetup(converterSetup);
        converterSetup.setDisplayRange(min, max);
        this.bdvSS.getBdvHandle().getSetupAssignments().addSetup(converterSetup);

    }

    /**
    Returns min and max pixel values of the center slice of the first time point for the RandomAccessibleInterval
     as a DisplaySettings object of the requested channel.
     */
    @Override
    public DisplaySettings getDisplaySettings(int channel) {
        double min,max;
        if(this.rai!= null) {
            RandomAccessibleInterval raiStack = Views.hyperSlice(
                    Views.hyperSlice(this.rai, T, 0),
                    C,
                    channel);
            final long stackCenter = (raiStack.max(Z) - raiStack.min(Z)) / 2 + raiStack.min(Z);
            IntervalView<T> ts = Views.hyperSlice(
                    raiStack,
                    Z,
                    stackCenter);
            Cursor<T> cursor = Views.iterable(ts).cursor();
            min = Double.MAX_VALUE;
            max = -Double.MAX_VALUE;
            double value;
            while (cursor.hasNext()) {
                value = cursor.next().getRealDouble();
                if (value < min) min = value;
                if (value > max) max = value;
            }
        }else{
            max=0;min=0;
        }
        return new DisplaySettings(min, max);
    }

    @Override
    public void doAutoContrastPerChannel() {
        int nChannels = (int) this.getRai().dimension(FileInfoConstants.C);
        for (int channel = 0; channel < nChannels; ++channel) {
            DisplaySettings setting = getDisplaySettings(channel);
            setDisplayRange(setting.getMinValue(), setting.getMaxValue(), 0);
            //channel is always 0 (zero) because converterSetup object gets removed and added at the end of bdvSS in setDisplayRange method.
            //Hence current channel is always at position 0 of the bdvSS.
        }
    }

    @Override
    public String getCalibrationUnit()
    {
        return calibrationUnit;
    }

    public void replicateViewerContrast(ImageViewer newImageView) {
        int nChannels = (int) this.getRai().dimension( C );
        for (int channel = 0; channel < nChannels; ++channel) {
            ConverterSetup converterSetup = this.getBdvSS().getBdvHandle().getSetupAssignments().getConverterSetups().get(channel);
            newImageView.setDisplayRange(converterSetup.getDisplayRangeMin(), converterSetup.getDisplayRangeMax(), 0);
            //channel is always 0 (zero) because converterSetup object gets removed and added at the end of bdvSS in setDisplayRange method.
            //Hence current channel is always at position 0 of the bdvSS.
        }
    }

    public int getCurrentTimePoint() {
        System.out.println("Time is" + this.bdvSS.getBdvHandle().getViewerPanel().getState().getCurrentTimepoint());
        return this.bdvSS.getBdvHandle().getViewerPanel().getState().getCurrentTimepoint();
    }

    @Override
    public void shiftImageToCenter(double[] centerCoordinates) {
        AffineTransform3D sourceTransform = new AffineTransform3D();
        int width = this.bdvSS.getBdvHandle().getViewerPanel().getWidth();
        int height = this.bdvSS.getBdvHandle().getViewerPanel().getHeight();
        centerCoordinates[0] = (width / 2.0 + centerCoordinates[0]);
        centerCoordinates[1] = (height / 2.0 - centerCoordinates[1]);
        centerCoordinates[2] = -centerCoordinates[2];
        sourceTransform.translate(centerCoordinates);
        repaint(sourceTransform);
    }

    public BdvStackSource getBdvSS() {
        return bdvSS;
    }

    private void showImageInViewer(
            RandomAccessibleInterval rai,
            String imageName,
            double[] voxelSize,
            String calibrationUnit )
    {
        final AffineTransform3D scaling = new AffineTransform3D();

        for ( int d = 0; d < 3; d++ )
        {
            scaling.set( voxelSize[ d ], d, d );
        }

        bdvSS = BdvFunctions.show(
                asVolatile( rai ),
                imageName,
                BdvOptions.options().axisOrder( AxisOrder.XYZCT )
                        .addTo( bdvSS ).sourceTransform( scaling ) );

        this.imageName = imageName;
        this.calibrationUnit = calibrationUnit;
        this.rai = rai;
        this.voxelSize = voxelSize;
    }

    private RandomAccessibleInterval asVolatile( RandomAccessibleInterval rai )
    {
        RandomAccessibleInterval volatileRai;
        try
        {
            volatileRai = VolatileViews.wrapAsVolatile( rai );
        }
        catch ( Exception e )
        {
            volatileRai = rai;
        }
        return volatileRai;
    }

}
