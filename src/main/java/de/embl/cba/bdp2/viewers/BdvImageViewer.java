package de.embl.cba.bdp2.viewers;

import bdv.tools.brightness.ConverterSetup;
import bdv.tools.brightness.MinMaxGroup;
import bdv.tools.brightness.SetupAssignments;
import bdv.util.*;
import bdv.util.volatiles.VolatileViews;
import bdv.viewer.DisplayMode;
import bdv.viewer.SourceAndConverter;
import de.embl.cba.bdp2.dialog.DisplaySettings;
import de.embl.cba.bdp2.dialog.Utils;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.boundingbox.BoundingBoxDialog;
import de.embl.cba.bdp2.service.BdvService;
import de.embl.cba.bdp2.service.ImageService;
import de.embl.cba.bdp2.drift.devel.ThresholdFloodFillOverlapTracker;
import de.embl.cba.bdp2.drift.track.Track;
import de.embl.cba.bdp2.ui.MenuActions;
import de.embl.cba.bdp2.utils.DimensionOrder;
import de.embl.cba.bdp2.volatiles.VolatileCachedCellImgs;
import de.embl.cba.bdv.utils.BdvUtils;
import net.imglib2.*;
import net.imglib2.Cursor;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BdvImageViewer < R extends RealType< R > & NativeType< R > >
{
    public static final String VIEWER_TITLE_STUMP = "BigDataViewer - ";
    public static boolean enableArbitraryPlaneSlicing = false;

    private Image< R > image;
    private ArrayList< BdvStackSource< R > > channelSources;
    private BdvHandle bdvHandle;
    private Map< String, Track > tracks;
    private int numRenderingThreads = Runtime.getRuntime().availableProcessors(); // TODO

    public BdvImageViewer( final Image< R > image )
    {
        this( image, true, false );
    }

    public BdvImageViewer( final Image< R > image, final boolean autoContrast, final boolean enableArbitraryPlaneSlicing )
    {
        this.image = image;
        this.enableArbitraryPlaneSlicing = enableArbitraryPlaneSlicing;
        this.channelSources = new ArrayList<>(  );

        showImage( image, autoContrast );


        Utils.centerWindowToPosition( bdvHandle.getViewerPanel()  );

        // this.addMenus( new MenuActions() );
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

    public void close()
    {
        bdvHandle.close();
    }

    public FinalInterval getVoxelIntervalXYZCTViaDialog( )
    {
        BoundingBoxDialog boundingBoxDialog = new BoundingBoxDialog( bdvHandle, image );
        boundingBoxDialog.showVoxelBoxAndWaitForResult();
        return boundingBoxDialog.getVoxelSelectionInterval();
    }

    public FinalRealInterval getRealIntervalXYZCTViaDialog( )
    {
        BoundingBoxDialog boundingBoxDialog = new BoundingBoxDialog( bdvHandle, image );
        boundingBoxDialog.showRealBoxAndWaitForResult();
        return boundingBoxDialog.getRealSelectionInterval();
    }

    public Image< R > getImage() {
        return image;
    }

    public void repaint( AffineTransform3D viewerTransform) {
        this.bdvHandle.getViewerPanel().setCurrentViewerTransform(viewerTransform);
    }

    public void repaint() {
        this.bdvHandle.getViewerPanel().requestRepaint();
    }

    public void replaceImage( Image image, boolean autoContrast, boolean keepViewerTransform )
    {
        final AffineTransform3D viewerTransform = getViewerTransform();
        final List< DisplaySettings > displaySettings = getDisplaySettings();

        if ( channelSources.size() > 0 ) removeAllSourcesFromBdv();

        showImage( image, autoContrast );

        if ( keepViewerTransform )
            bdvHandle.getViewerPanel().setCurrentViewerTransform( viewerTransform );

        if ( ! autoContrast )
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
        {
            setDisplayRange(
                    displaySettings.get( c ).getDisplayRangeMin(),
                    displaySettings.get( c ).getDisplayRangeMax(),
                    c );
        }
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

    public void addMenus( MenuActions menuActions )
    {
        menuActions.setViewer(this);

        final BdvHandleFrame bdvHandleFrame = ( BdvHandleFrame ) this.bdvHandle;
        final JMenuBar bdvMenuBar = bdvHandleFrame.getBigDataViewer().getViewerFrame().getJMenuBar();

        for ( JMenu menu : menuActions.getMainMenus() )
        {
            bdvMenuBar.add( menu );
        }

        bdvMenuBar.updateUI();
    }

    public void setDisplayRange( double min, double max, int channel )
    {
        final boolean groupingEnabled = bdvHandle.getViewerPanel().getVisibilityAndGrouping().isGroupingEnabled();
        final DisplayMode displayMode = bdvHandle.getViewerPanel().getVisibilityAndGrouping().getDisplayMode();

        final SetupAssignments setupAssignments = bdvHandle.getSetupAssignments();

        final List< ConverterSetup > converterSetups = setupAssignments.getConverterSetups();
        final ConverterSetup converterSetup = converterSetups.get( channel );

        setupAssignments.removeSetupFromGroup( converterSetup,  setupAssignments.getMinMaxGroup( converterSetup ) );

        converterSetup.setDisplayRange( min, max );
        final MinMaxGroup minMaxGroup = setupAssignments.getMinMaxGroup( converterSetup );
        minMaxGroup.getMinBoundedValue().setCurrentValue( min );
        minMaxGroup.getMaxBoundedValue().setCurrentValue( max );
    }

    public List< DisplaySettings > getDisplaySettings()
    {
        final List< ConverterSetup > converterSetups = bdvHandle.getSetupAssignments().getConverterSetups();

        final ArrayList< DisplaySettings > displaySettings = new ArrayList<>();
        for ( ConverterSetup converterSetup : converterSetups )
        {
            if ( converterSetup instanceof PlaceHolderConverterSetup ) continue;

            displaySettings.add(
                    new DisplaySettings(
                            converterSetup.getDisplayRangeMin(),
                            converterSetup.getDisplayRangeMax(),
                            converterSetup.getColor() ) );
        }

        return displaySettings;
    }

    /**
     * Returns min and max pixel values of the
     * center slice of the first time point for the RandomAccessibleInterval
     * as a DisplaySettings object of the requested channel.
     */
    
    public DisplaySettings getAutoContrastDisplaySettings( int channel )
    {
        double min, max;

        final long sliceIndex = getCurrentlyShownSliceIndex( channel );

        if ( image != null)
        {
            RandomAccessibleInterval raiXYZ = Views.hyperSlice(
                    Views.hyperSlice( image.getRai(), DimensionOrder.T, 0),
                    DimensionOrder.C,
                    channel);

            IntervalView< R > raiXY = Views.hyperSlice(
                    raiXYZ,
                    DimensionOrder.Z,
                    sliceIndex );

            final long nx = raiXY.dimension( 0 );
            final long ny = raiXY.dimension( 1 );

            // take only a central part to speed it up a bit
            final FinalInterval crop = Intervals.expand( raiXY, 0, DimensionOrder.Y );

            final IntervalView< R > cropped = Views.interval( raiXY, crop );

            Cursor< R > cursor = Views.iterable( cropped ).cursor();
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

    public long getCurrentlyShownSliceIndex( int channel )
    {
        final AffineTransform3D viewerTransform = getViewerTransform();
        final RealPoint globalPosition = new RealPoint( 0, 0, 0 );
        viewerTransform.inverse().apply( new RealPoint( 0, 0, 0 ), globalPosition );
        final long[] positionInSource = BdvUtils.getPositionInSource( channelSources.get( channel ).getSources().get( 0 ).getSpimSource(), globalPosition, 0, 0 );
        return positionInSource[ DimensionOrder.Z ];
    }

    public void autoContrast()
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
        final AffineTransform3D transform3D = new AffineTransform3D();
        bdvHandle.getViewerPanel().getState().getViewerTransform( transform3D );
        return transform3D; //  transform3D.copy();
    }

    public void setViewerTransform( AffineTransform3D viewerTransform )
    {
        bdvHandle.getViewerPanel()
                .setCurrentViewerTransform( viewerTransform );
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
        return this.bdvHandle.getViewerPanel().getState().getCurrentTimepoint();
    }

    
    public void shiftImageToCenter(double[] centerCoordinates) {
        AffineTransform3D sourceTransform = new AffineTransform3D();
        int width = this.bdvHandle.getViewerPanel().getWidth();
        int height = this.bdvHandle.getViewerPanel().getHeight();
        centerCoordinates[0] = (width / 2.0 + centerCoordinates[0]);
        centerCoordinates[1] = (height / 2.0 - centerCoordinates[1]);
        centerCoordinates[2] = -centerCoordinates[2];
        sourceTransform.translate(centerCoordinates);
        repaint(sourceTransform);
    }

    public BdvHandle getBdvHandle()
    {
        return bdvHandle;
    }

    private void showImage( Image< R > image, boolean autoContrast )
    {
        this.image = image;

        BdvService.imageNameToBdvImageViewer.put( image.getName(), this );
        ImageService.imageNameToImage.put( image.getName(), image );

        addToBdv( image );

        //bdvHandle.getViewerPanel().setInterpolation( Interpolation.NLINEAR );

        setAutoColors();

        if ( autoContrast )
            new Thread( () -> autoContrast() ).start();

        JFrame topFrame = setWindowTitle( image );
        addFocusListener( topFrame );
    }

    private JFrame setWindowTitle( Image< R > image )
    {
        JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor( bdvHandle.getViewerPanel() );
        topFrame.setTitle( VIEWER_TITLE_STUMP + image.getName() );
        return topFrame;
    }

    private void addFocusListener( JFrame topFrame )
    {
        final BdvImageViewer viewer = this;
        topFrame.addWindowListener( new WindowAdapter()
        {
            @Override
            public void windowActivated( WindowEvent e )
            {
                super.windowActivated( e );
                BdvService.setFocusedViewer( viewer );
            }
        } );
    }

    private void addToBdv( Image< R > image )
    {
        final AffineTransform3D scaling = getScalingTransform( image.getVoxelSize() );
        RandomAccessibleInterval cachedCellImg = VolatileCachedCellImgs.asVolatileCachedCellImg( image );
        BdvOptions options = getBdvOptions( image, scaling );

        final long numChannels = cachedCellImg.dimension( DimensionOrder.C );
        final String[] channelNames = image.getChannelNames();

        this.channelSources = new ArrayList<>(  );

        for ( int channelIndex = 0; channelIndex < numChannels; channelIndex++ )
        {
            final IntervalView channelView = Views.hyperSlice( cachedCellImg, DimensionOrder.C, channelIndex );

            final BdvStackSource stackSource = BdvFunctions.show(
                    VolatileViews.wrapAsVolatile( channelView ),
                    channelNames[ channelIndex ],
                    options );

            this.bdvHandle = stackSource.getBdvHandle();
            options = options.addTo( bdvHandle );
            this.channelSources.add( stackSource );
        }
    }

    private BdvOptions getBdvOptions( Image< R > image, AffineTransform3D scaling )
    {
        BdvOptions options = BdvOptions.options().axisOrder( AxisOrder.XYZT )
                .addTo( bdvHandle )
                .sourceTransform( scaling )
                .numRenderingThreads( numRenderingThreads )
                .frameTitle( VIEWER_TITLE_STUMP + image.getName() );

        if ( ! enableArbitraryPlaneSlicing )
        {
            options = options.transformEventHandlerFactory( new BehaviourTransformEventHandler3DWithoutRotation.BehaviourTransformEventHandler3DFactory() );
        }
        return options;
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
        final int numSources = channelSources.size();

        if ( numSources > 0 )
        {
            for ( int sourceIndex = 0; sourceIndex < numSources; sourceIndex++ )
            {
                final ConverterSetup converterSetup =
                        bdvHandle.getSetupAssignments().getConverterSetups().get( sourceIndex );

                converterSetup.setColor( getAutoColor( sourceIndex, numSources ) );
            }
        }
    }

    private ARGBType getAutoColor( int sourceIndex, int numSources )
    {
        if ( numSources == 1 )
            return new ARGBType( ARGBType.rgba( 255, 255, 255, 255 / numSources ) );

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
        tracks.put( track.getName(), track );
    }

    public Map< String, Track > getTracks()
    {
        return tracks;
    }

    public void getSourceTransform( AffineTransform3D transform )
    {
        channelSources.get( 0 ).getSources().get( 0 ).getSpimSource().getSourceTransform( 0,0, transform );
    }
}