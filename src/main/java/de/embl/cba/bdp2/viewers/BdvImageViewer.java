package de.embl.cba.bdp2.viewers;

import bdv.tools.brightness.ConverterSetup;
import bdv.util.AxisOrder;
import bdv.util.BdvFunctions;
import bdv.util.BdvHandleFrame;
import bdv.util.BdvOptions;
import bdv.util.BdvStackSource;
import bdv.util.PlaceHolderConverterSetup;
import bdv.viewer.SourceAndConverter;
import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.boundingbox.BoundingBoxDialog;
import de.embl.cba.bdp2.logging.Logger;
import de.embl.cba.bdp2.ui.BdvMenus;
import de.embl.cba.bdp2.ui.DisplaySettings;
import de.embl.cba.bdp2.utils.DimensionOrder;
import de.embl.cba.bdp2.volatiles.VolatileViews;
import de.embl.cba.bdv.utils.BdvUtils;
import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.Volatile;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import javax.swing.*;

public class BdvImageViewer< R extends RealType< R > & NativeType< R >>
        implements ImageViewer
{

    private Image< R > image;
    private BdvStackSource< Volatile< R > > bdvStackSource;
    private BdvGrayValuesOverlay overlay;

    public BdvImageViewer() {
    }

    public BdvImageViewer( Image< R > image ) {
        this.image = image;
    }

    public BdvImageViewer( RandomAccessibleInterval< R > rai,
                           String name,
                           double[] voxelSpacing,
                           String voxelUnit )
    {
        this.image = new Image<>( rai, name, voxelSpacing, voxelUnit  );
    }

    @Override
    public FinalInterval get5DIntervalFromUser() {
        BoundingBoxDialog showBB = new BoundingBoxDialog(this.bdvStackSource.getBdvHandle());
        final double[] voxelSpacing = image.getVoxelSpacing();
        final RandomAccessibleInterval< R > rai = image.getRai();
        showBB.show( rai, voxelSpacing );

        // TODO: refactor this into a BoundingBox class
        FinalInterval interval;
        if (showBB.selectedMax != null && showBB.selectedMin != null) {
            long[] minMax = {
                    (long) (showBB.selectedMin[BoundingBoxDialog.X] / voxelSpacing[ DimensionOrder.X]),
                    (long) (showBB.selectedMin[BoundingBoxDialog.Y] / voxelSpacing[ DimensionOrder.Y]),
                    (long) (showBB.selectedMin[BoundingBoxDialog.Z] / voxelSpacing[ DimensionOrder.Z]),
                    rai.min( DimensionOrder.C),
                    showBB.selectedMin[BoundingBoxDialog.T],
                    (long) (showBB.selectedMax[BoundingBoxDialog.X] / voxelSpacing[ DimensionOrder.X]),
                    (long) (showBB.selectedMax[BoundingBoxDialog.Y] / voxelSpacing[ DimensionOrder.Y]),
                    (long) (showBB.selectedMax[BoundingBoxDialog.Z] / voxelSpacing[ DimensionOrder.Z]),
                    rai.max( DimensionOrder.C),
                    showBB.selectedMax[BoundingBoxDialog.T]};
            interval= Intervals.createMinMax(minMax);
        }else{
            interval =  null;
        }
        return interval;
    }


    @Override
    public ImageViewer newImageViewer() {
        return new BdvImageViewer< R >();
    }

    @Override
    public Image< R > getImage() {
        return image;
    }


    @Override // TODO: remove this...
    public void repaint(AffineTransform3D viewerTransform) {
        this.bdvStackSource.getBdvHandle().getViewerPanel().setCurrentViewerTransform(viewerTransform);
    }

    @Override
    public void repaint() {
        this.bdvStackSource.getBdvHandle().getViewerPanel().requestRepaint();
    }

    @Override
    public void show() {
        showImage( image );
    }

    @Override
    public void show( Image image, boolean autoContrast )
    {
        if ( bdvStackSource != null )
            removeAllSourcesFromBdv();

        showImage( image );

        if (autoContrast)
            doAutoContrastPerChannel();
    }

    @Override
    public void show(
            RandomAccessibleInterval rai,
            String name,
            double[] voxelSpacing,
            String voxelUnit,
            boolean autoContrast )
    {
        show( new Image<>( rai, name, voxelSpacing, voxelUnit  ), autoContrast );
    }

    private void removeAllSourcesFromBdv() {
        int nSources = this.bdvStackSource.getBdvHandle().getViewerPanel().getState().getSources().size();
        for (int source = 0; source < nSources; ++source) {
            SourceAndConverter scnv = this.bdvStackSource.getBdvHandle().getViewerPanel().getState().getSources().get(0);
            this.bdvStackSource.getBdvHandle().getViewerPanel().removeSource(scnv.getSpimSource());
            //source is always 0 (zero) because SourceAndConverter object gets removed from bdvSS.
            //Hence source is always at position 0 of the bdvSS.
        }

        int nChannels = this.getBdvStackSource()
                .getBdvHandle().getSetupAssignments().getConverterSetups().size();
        for (int channel = 0; channel < nChannels; ++channel) {
            ConverterSetup converterSetup = this.getBdvStackSource()
                    .getBdvHandle().getSetupAssignments().getConverterSetups().get(0);
            this.bdvStackSource.getBdvHandle().getSetupAssignments().removeSetup(converterSetup);
            //channel is always 0 (zero) because converterSetup object gets removed from bdvSS.
            //Hence current channel is always at position 0 of the bdvSS.
        }
    }

    public void addMenus( BdvMenus menus ) {
        menus.setImageViewer(this);
        for (JMenu menu : menus.getMenus()) {
            ((BdvHandleFrame) this.bdvStackSource.getBdvHandle())
                    .getBigDataViewer().getViewerFrame().getJMenuBar().add((menu));
        }
        ((BdvHandleFrame) this.bdvStackSource.getBdvHandle())
                .getBigDataViewer().getViewerFrame().getJMenuBar().updateUI();
    }

    @Override
    public void setDisplayRange(double min, double max, int channel) {
        final ConverterSetup converterSetup = this.bdvStackSource.getBdvHandle().getSetupAssignments().getConverterSetups().get(channel);
        converterSetup.setDisplayRange( min, max );
        converterSetup.getSetupId();

    }


    public DisplaySettings getDisplaySettings( int channel ) {

        final ConverterSetup converterSetup = this.bdvStackSource.getBdvHandle()
                .getSetupAssignments()
                .getConverterSetups()
                .get(channel);

        return new DisplaySettings( converterSetup.getDisplayRangeMin(), converterSetup.getDisplayRangeMax() );
    }

    /**
     * Returns min and max pixel values of the
     * center slice of the first time point for the RandomAccessibleInterval
     * as a DisplaySettings object of the requested channel.
     */
    @Override
    public DisplaySettings getAutoContrastDisplaySettings( int channel) {
        double min, max;
        if ( image != null) {
            RandomAccessibleInterval raiStack = Views.hyperSlice(
                    Views.hyperSlice( image.getRai(), DimensionOrder.T, 0),
                    DimensionOrder.C,
                    channel);
            final long stackCenter = (raiStack.max( DimensionOrder.Z) - raiStack.min( DimensionOrder.Z)) / 2 + raiStack.min( DimensionOrder.Z);
            IntervalView< R > ts = Views.hyperSlice(
                    raiStack,
                    DimensionOrder.Z,
                    stackCenter);
            Cursor< R > cursor = Views.iterable(ts).cursor();
            min = Double.MAX_VALUE;
            max = -Double.MAX_VALUE;
            double value;
            while (cursor.hasNext()) {
                value = cursor.next().getRealDouble();
                if (value < min) min = value;
                if (value > max) max = value;
            }
        } else {
            max = 0;
            min = 0;
        }
        return new DisplaySettings(min, max);
    }

    @Override
    public void doAutoContrastPerChannel() {
        int nChannels = (int) image.getRai().dimension( DimensionOrder.C);
        for (int channel = 0; channel < nChannels; ++channel) {
            DisplaySettings setting = getAutoContrastDisplaySettings(channel);
            setDisplayRange( setting.getMinValue(), setting.getMaxValue(), 0);
            //channel is always 0 (zero) because converterSetup object gets removed and added at the end of bdvSS in setDisplayRange method.
            //Hence current channel is always at position 0 of the bdvSS.
        }
    }

    @Override
    public AffineTransform3D getViewerTransform()
    {
        if ( bdvStackSource != null )
        {
            final AffineTransform3D transform3D = new AffineTransform3D();
            bdvStackSource.getBdvHandle().getViewerPanel()
                    .getState().getViewerTransform( transform3D );
            return transform3D.copy();
        }
        else
           return null;
    }

    @Override
    public void setViewerTransform( AffineTransform3D viewerTransform )
    {
        bdvStackSource.getBdvHandle().getViewerPanel().setCurrentViewerTransform( viewerTransform );
        bdvStackSource.getBdvHandle().getViewerPanel().requestRepaint();
    }

    public void replicateViewerContrast(ImageViewer newImageView) {
        int nChannels =this.getBdvStackSource().getBdvHandle().getSetupAssignments().getConverterSetups().size();
        for (int channel = 0; channel < nChannels; ++channel) {
            ConverterSetup converterSetup = this.getBdvStackSource().getBdvHandle().getSetupAssignments().getConverterSetups().get(channel);
            if (!(converterSetup instanceof PlaceHolderConverterSetup)) { // PlaceHolderConverterSetup is the Overlay.
                newImageView.setDisplayRange(converterSetup.getDisplayRangeMin(), converterSetup.getDisplayRangeMax(), 0);
            }
            //channel is always 0 (zero) because converterSetup object gets removed and added at the end of bdvSS in setDisplayRange method.
            //Hence current channel is always at position 0 of the bdvSS.
        }
    }

    public int getCurrentTimePoint() {
        return this.bdvStackSource.getBdvHandle().getViewerPanel().getState().getCurrentTimepoint();
    }

    @Override
    public void shiftImageToCenter(double[] centerCoordinates) {
        AffineTransform3D sourceTransform = new AffineTransform3D();
        int width = this.bdvStackSource.getBdvHandle().getViewerPanel().getWidth();
        int height = this.bdvStackSource.getBdvHandle().getViewerPanel().getHeight();
        centerCoordinates[0] = (width / 2.0 + centerCoordinates[0]);
        centerCoordinates[1] = (height / 2.0 - centerCoordinates[1]);
        centerCoordinates[2] = -centerCoordinates[2];
        sourceTransform.translate(centerCoordinates);
        repaint(sourceTransform);
    }

    public BdvStackSource< Volatile< R > > getBdvStackSource() {
        return bdvStackSource;
    }

    private void showImage( Image< R > image )
    {
        this.image = image;
        bdvStackSource = addToBdv( image );
        setTransform();
        setColors();
        addGrayValueOverlay();
    }

    private BdvStackSource< Volatile< R > > addToBdv( Image< R > image )
    {
        final AffineTransform3D scaling = getScalingTransform( image.getVoxelSpacing() );

        final RandomAccessibleInterval< Volatile< R > > volatileRai =
                asVolatile( image.getRai() );

        if ( volatileRai == null )
            Logger.error( "Could not wrap as volatile!" );
        else
            bdvStackSource = BdvFunctions.show(
                                    volatileRai,
                                    image.getName(),
                                    BdvOptions.options().axisOrder( AxisOrder.XYZCT )
                                            .addTo( bdvStackSource )
                                            .sourceTransform( scaling ) );
        return bdvStackSource;
    }

    private void setTransform()
    {
        AffineTransform3D transform3D = getViewerTransform();
        if ( transform3D != null ) setViewerTransform( transform3D );
    }

    private AffineTransform3D getScalingTransform( double[] voxelSpacing )
    {
        final AffineTransform3D scaling = new AffineTransform3D();

        for (int d = 0; d < 3; d++)
            scaling.set(voxelSpacing[d], d, d);
        return scaling;
    }

    private void setColors()
    {
        final int numSources = bdvStackSource.getSources().size();
        if ( numSources > 1 )
        {
            for ( int sourceIndex = 0; sourceIndex < numSources; sourceIndex++ )
            {
                final ConverterSetup converterSetup =
                        bdvStackSource.getBdvHandle().getSetupAssignments().getConverterSetups().get( sourceIndex );

                converterSetup.setColor( getColor( sourceIndex, numSources ) );
            }

        }
    }

    private ARGBType getColor( int sourceIndex, int numSources )
    {
        switch ( sourceIndex )
        {
            case 0:
                return new ARGBType( ARGBType.rgba( 0, 255, 0, 255 / numSources ) );
            case 1:
                return new ARGBType( ARGBType.rgba( 255, 0, 255, 255 / numSources ) );
            case 2:
                return new ARGBType( ARGBType.rgba( 0, 255, 255, 255 / numSources ) );
            case 3:
                return new ARGBType( ARGBType.rgba( 255, 0, 0, 255 / numSources ) );
            default:
                return new ARGBType( ARGBType.rgba( 255, 255, 255, 255 / numSources ) );
        }
    }

    private RandomAccessibleInterval< Volatile< R > >
    asVolatile( RandomAccessibleInterval< R > rai ) {

        try {
            final RandomAccessibleInterval< Volatile< R > > volatileRai
                    = VolatileViews.wrapAsVolatile( rai );
            final Volatile< R > typeFromInterval = Util.getTypeFromInterval( volatileRai );
            return volatileRai;
        } catch (IllegalArgumentException e)
		{
			System.out.println( "Wrap as volatile failed!");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void addGrayValueOverlay() {

        // TODO: this seems to sometimes clash with other overlays
        // e.g., the selectionBox
//        if (overlay == null) {
//            overlay = new BdvGrayValuesOverlay(this.bdvSS, 20, "Courier New");
//        }
//        BdvFunctions.showOverlay(overlay,
//                "GrayOverlay",
//                BdvOptions.options().addTo(bdvSS));

    }
}