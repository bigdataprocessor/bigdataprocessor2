package de.embl.cba.bdp2.viewers;

import bdv.tools.brightness.ConverterSetup;
import bdv.tools.brightness.MinMaxGroup;
import bdv.util.*;
import bdv.viewer.SourceAndConverter;
import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.boundingbox.BoundingBoxDialog;
import de.embl.cba.bdp2.logging.Logger;
import de.embl.cba.bdp2.tracking.ThresholdFloodFillOverlapTracker;
import de.embl.cba.bdp2.tracking.Track;
import de.embl.cba.bdp2.ui.BdvMenus;
import de.embl.cba.bdp2.ui.DisplaySettings;
import de.embl.cba.bdp2.utils.DimensionOrder;
import de.embl.cba.bdp2.volatiles.VolatileCachedCellImg;
import de.embl.cba.bdp2.volatiles.VolatileViews;
import net.imglib2.*;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.util.Util;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BdvImageViewer < R extends RealType< R > & NativeType< R > >
{
    private Image< R > image;
    private BdvStackSource< ? > bdvStackSource;
    private BdvGrayValuesOverlay overlay;
    private BdvHandle bdvHandle;
    private Map< String, Track > tracks;
    private int numRenderingThreads = Runtime.getRuntime().availableProcessors(); // TODO

    public BdvImageViewer( Image< R > image )
    {
        this( image, true );
    }

    public BdvImageViewer( Image< R > image, boolean autoContrast )
    {
        this.image = image;
        show();

        if ( autoContrast ) autoContrastPerChannel();

        // TODO: not logical that this is part of the "Viewer" rather than the "Processor"....
        this.addMenus( new BdvMenus() );
        this.installBehaviours( );
    }

    private void installBehaviours()
    {
        Behaviours behaviours = new Behaviours( new InputTriggerConfig() );
        behaviours.install( bdvHandle.getTriggerbindings(), "behaviours" );

        installTrackingBehaviour( behaviours );
        installStoppingBehaviour( behaviours );
    }

    private void installTrackingBehaviour( Behaviours behaviours )
    {
        tracks = new HashMap<>(  );

        behaviours.behaviour( ( ClickBehaviour ) ( x, y ) ->
                ( new Thread( () -> ThresholdFloodFillOverlapTracker.trackObjectDialog( this ) )).start(),
                "Track object", "ctrl T"  ) ;
    }

    private void installStoppingBehaviour( Behaviours behaviours )
    {
        behaviours.behaviour( ( ClickBehaviour ) ( x, y ) ->
                        ( new Thread( () ->
                        {
                            image.stopStopableProcesses();
                            //bdvHandle.close();
                        } )).start(),
                "Stop image processing", "ctrl S"  ) ;
    }


    public BdvImageViewer( RandomAccessibleInterval< R > rai,
                           String name,
                           double[] voxelSpacing,
                           String voxelUnit )
    {
        this.image = new Image<>( rai, name, voxelSpacing, voxelUnit  );
    }

    public void close()
    {
        bdvHandle.close();
    }

    public FinalInterval get5DIntervalFromUser( boolean calibratedSelection )
    {
        BoundingBoxDialog boundingBoxDialog = new BoundingBoxDialog( bdvHandle, image );

        if ( calibratedSelection )
            boundingBoxDialog.showCalibratedUnitsBox( );
        else
            boundingBoxDialog.showVoxelUnitsBox( );

        FinalInterval interval = boundingBoxDialog.getVoxelUnitsSelectionInterval();

        return interval;
    }

    public Image< R > getImage() {
        return image;
    }

    public void repaint(AffineTransform3D viewerTransform) {
        this.bdvStackSource.getBdvHandle().getViewerPanel().setCurrentViewerTransform(viewerTransform);
    }

    
    public void repaint() {
        this.bdvStackSource.getBdvHandle().getViewerPanel().requestRepaint();
    }

    
    public void show() {
        showImage( image );
    }

    public void replaceImage( Image image )
    {
        final AffineTransform3D viewerTransform = getViewerTransform();
        final List< DisplaySettings > displaySettings = getDisplaySettings();

        if ( bdvStackSource != null )
            removeAllSourcesFromBdv();

        showImage( image );

        bdvHandle.getViewerPanel().setCurrentViewerTransform( viewerTransform );
        setDisplaySettings( displaySettings );
    }

    public BdvImageViewer< R > showImageInNewWindow( Image< R > image )
    {
        final AffineTransform3D viewerTransform = getViewerTransform();
        final List< DisplaySettings > displaySettings = getDisplaySettings();

        final BdvImageViewer< R > bdvImageViewer = new BdvImageViewer<>( image );

        bdvImageViewer.getBdvHandle().getViewerPanel().setCurrentViewerTransform( viewerTransform );
        bdvImageViewer.setDisplaySettings( displaySettings );

        return bdvImageViewer;
    }

    public void setDisplaySettings( List< DisplaySettings > displaySettings )
    {
        final int numChannels = displaySettings.size();
        for ( int c = 0; c < numChannels; c++ )
            setDisplayRange(
                    displaySettings.get( c ).getDisplayRangeMin(),
                    displaySettings.get( c ).getDisplayRangeMax(),
                    c );
    }

    private void removeAllSourcesFromBdv() {
        int nSources = bdvHandle.getViewerPanel().getState().getSources().size();
        for (int source = 0; source < nSources; ++source) {
            SourceAndConverter scnv = bdvHandle.getViewerPanel().getState().getSources().get(0);
            bdvHandle.getViewerPanel().removeSource(scnv.getSpimSource());
            //source is always 0 (zero) because SourceAndConverter object gets removed from bdvSS.
            //Hence source is always at position 0 of the bdvSS.
        }

        int nChannels = bdvHandle.getSetupAssignments().getConverterSetups().size();
        for (int channel = 0; channel < nChannels; ++channel) {
            ConverterSetup converterSetup =
                    bdvHandle.getSetupAssignments().getConverterSetups().get(0);
            bdvHandle.getSetupAssignments().removeSetup(converterSetup);
            //channel is always 0 (zero) because converterSetup object gets removed from bdvSS.
            //Hence current channel is always at position 0 of the bdvSS.
        }
    }

    public void addMenus( BdvMenus menus ) {

        menus.setImageViewer(this);
        for ( JMenu menu : menus.getMenus() )
        {
            ((BdvHandleFrame) this.bdvStackSource.getBdvHandle())
                    .getBigDataViewer().getViewerFrame().getJMenuBar().add((menu));
        }

        ((BdvHandleFrame) this.bdvStackSource.getBdvHandle())
                .getBigDataViewer().getViewerFrame().getJMenuBar().updateUI();
    }

    public void setDisplayRange( double min, double max, int channel )
    {
        final List< ConverterSetup > converterSetups =
                this.bdvStackSource.getBdvHandle().getSetupAssignments().getConverterSetups();

        final ConverterSetup converterSetup = converterSetups.get( channel );
        converterSetup.setDisplayRange( min, max );

        final MinMaxGroup minMaxGroup =
                getBdvHandle().getSetupAssignments().getMinMaxGroup( converterSetup );

        minMaxGroup.getMinBoundedValue().setCurrentValue( min );
        minMaxGroup.getMaxBoundedValue().setCurrentValue( max );
    }

    public List< DisplaySettings > getDisplaySettings()
    {
        final List< ConverterSetup > converterSetups = bdvStackSource.getBdvHandle()
                .getSetupAssignments()
                .getConverterSetups();

        final ArrayList< DisplaySettings > displaySettings = new ArrayList<>();
        for ( int c = 0; c < converterSetups.size(); c++ )
            displaySettings.add(
                    new DisplaySettings(
                            converterSetups.get( c ).getDisplayRangeMin(),
                            converterSetups.get( c ).getDisplayRangeMax(),
                            converterSetups.get( c ).getColor()) );


        return displaySettings;
    }

    /**
     * Returns min and max pixel values of the
     * center slice of the first time point for the RandomAccessibleInterval
     * as a DisplaySettings object of the requested channel.
     */
    
    public DisplaySettings getAutoContrastDisplaySettings( int channel ) {
        double min, max;

        if ( image != null)
        {
            RandomAccessibleInterval raiXYZ = Views.hyperSlice(
                    Views.hyperSlice( image.getRai(), DimensionOrder.T, 0),
                    DimensionOrder.C,
                    channel);

            final long stackCenter =
                    (long) Math.ceil( ( raiXYZ.max( DimensionOrder.Z ) - raiXYZ.min( DimensionOrder.Z ) ) / 2.0 )
                            + raiXYZ.min( DimensionOrder.Z ) + 1;

            IntervalView< R > raiXY = Views.hyperSlice(
                    raiXYZ,
                    DimensionOrder.Z,
                    stackCenter );

            Cursor< R > cursor = Views.iterable( raiXY ).cursor();
            min = Double.MAX_VALUE;
            max = -Double.MAX_VALUE;
            double value;
            while ( cursor.hasNext() )
            {
                value = cursor.next().getRealDouble();
                if (value < min) min = value;
                if (value > max) max = value;
            }
        }
        else
        {
            max = 0;
            min = 0;
        }

        return new DisplaySettings( min, max, null );
    }

    public void autoContrastPerChannel()
    {
        int nChannels = (int) image.getRai().dimension( DimensionOrder.C);

        for (int channel = 0; channel < nChannels; ++channel)
        {
            DisplaySettings setting = getAutoContrastDisplaySettings( channel );

            setDisplayRange(
                    setting.getDisplayRangeMin(),
                    setting.getDisplayRangeMax(),
                    channel );
        }
    }

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

    public void setViewerTransform( AffineTransform3D viewerTransform )
    {
        bdvStackSource.getBdvHandle().getViewerPanel()
                .setCurrentViewerTransform( viewerTransform );

        bdvStackSource.getBdvHandle().getViewerPanel()
                .requestRepaint();
    }

    public void replicateViewerContrast( BdvImageViewer newImageViewer ) {

        int nChannels = bdvHandle.getSetupAssignments().getConverterSetups().size();

        for (int channel = 0; channel < nChannels; ++channel)
        {
            ConverterSetup converterSetup = bdvHandle
                    .getSetupAssignments().getConverterSetups().get(channel);

            if (! ( converterSetup instanceof PlaceHolderConverterSetup))
            { // PlaceHolderConverterSetup is the Overlay.
                newImageViewer.setDisplayRange(
                        converterSetup.getDisplayRangeMin(),
                        converterSetup.getDisplayRangeMax(), channel);
            }
        }
    }

    public int getCurrentTimePoint() {
        return this.bdvStackSource.getBdvHandle().getViewerPanel().getState().getCurrentTimepoint();
    }

    
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

    public BdvStackSource< ? > getBdvStackSource() {
        return bdvStackSource;
    }

    public BdvHandle getBdvHandle()
    {
        return bdvHandle;
    }

    private void showImage( Image< R > image )
    {
        this.image = image;
        bdvStackSource = addToBdv( image );
        bdvHandle = bdvStackSource.getBdvHandle();
        //bdvHandle.getViewerPanel().setInterpolation( Interpolation.NLINEAR );
        //setTransform();
        setAutoColors();
    }

    private BdvStackSource< ? > addToBdv( Image< R > image )
    {
        final AffineTransform3D scaling = getScalingTransform( image.getVoxelSpacing() );

//        final RandomAccessibleInterval< Volatile< R > > volatileRai =
//                asVolatile( image.getRai() );

        final CachedCellImg cachedCellImg = VolatileCachedCellImg.asVolatileCachedCellImg( ( RandomAccessibleInterval ) image.getRai() );

        bdvStackSource = BdvFunctions.show(
                cachedCellImg,
                image.getName(),
                BdvOptions.options().axisOrder( AxisOrder.XYZCT )
                        .addTo( bdvHandle )
                        .sourceTransform( scaling )
                        .numRenderingThreads( numRenderingThreads ) );


//        if ( volatileRai == null )
//        {
//            Logger.error( "Could not convert to volatile!\n" +
//                    "Image viewing might not be very smooth..." );
//
//            bdvStackSource = BdvFunctions.show(
//                    image.getRai(),
//                    image.getName(),
//                    BdvOptions.options().axisOrder( AxisOrder.XYZCT )
//                            .addTo( bdvHandle )
//                            .sourceTransform( scaling )
//                            .numRenderingThreads( numRenderingThreads ) );
//        }
//        else
//        {
//            bdvStackSource = BdvFunctions.show(
//                    volatileRai,
//                    image.getName(),
//                    BdvOptions.options().axisOrder( AxisOrder.XYZCT )
//                            .addTo( bdvHandle )
//                            .sourceTransform( scaling )
//                            .numRenderingThreads( numRenderingThreads ) );
//        }

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

    private void setAutoColors()
    {
        final int numSources = bdvStackSource.getSources().size();
        if ( numSources > 1 )
        {
            for ( int sourceIndex = 0; sourceIndex < numSources; sourceIndex++ )
            {
                final ConverterSetup converterSetup =
                        bdvStackSource.getBdvHandle().
                                getSetupAssignments().getConverterSetups().get( sourceIndex );

                converterSetup.setColor( getAutoColor( sourceIndex, numSources ) );
            }

        }
    }

    private ARGBType getAutoColor( int sourceIndex, int numSources )
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

    private RandomAccessibleInterval< Volatile< R > > asVolatile( RandomAccessibleInterval< R > rai ) {

        try {
            final RandomAccessibleInterval< Volatile< R > > volatileRai
                    = VolatileViews.wrapAsVolatile( rai );
            final Volatile< R > typeFromInterval = Util.getTypeFromInterval( volatileRai );
            return volatileRai;
        }
        catch (IllegalArgumentException e)
		{
			System.err.println( "Wrap as volatile failed!");
            return null;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
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

    public void addTrack( Track track )
    {
        tracks.put( track.getId(), track );
    }

    public Map< String, Track > getTracks()
    {
        return tracks;
    }
}