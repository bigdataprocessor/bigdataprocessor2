/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2023 EMBL
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package de.embl.cba.bdp2.viewer;

import bdv.TransformEventHandler3D;
import bdv.tools.brightness.ConverterSetup;
import bdv.tools.transformation.TransformedSource;
import bdv.util.*;
import bdv.util.volatiles.VolatileViews;
import bdv.viewer.Source;
import bdv.viewer.SourceAndConverter;
import de.embl.cba.bdp2.boundingbox.BoundingBoxDialog;
import de.embl.cba.bdp2.dialog.DisplaySettings;
import de.embl.cba.bdp2.dialog.DialogUtils;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.process.calibrate.CalibrationChecker;
import de.embl.cba.bdp2.track.Track;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.service.ImageViewerService;
import de.embl.cba.bdp2.service.ImageService;
import de.embl.cba.bdp2.utils.DimensionOrder;
import de.embl.cba.bdp2.utils.RAISlicer;
import de.embl.cba.bdp2.volatiles.VolatileCachedCellImgs;
import de.embl.cba.bdv.utils.BdvUtils;
import net.imglib2.*;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.scijava.ui.behaviour.*;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;

public class ImageViewer< R extends RealType< R > & NativeType< R > >
{
    public static final String VIEWER_TITLE_STUMP = "";
    public static boolean enableArbitraryPlaneSlicing = false;

    private Image< R > image;
    private ArrayList< BdvStackSource< R > > channelSources;
    private BdvHandle bdvHandle;
    private Map< String, Track > tracks;
    private int numRenderingThreads = Runtime.getRuntime().availableProcessors();

    public ImageViewer( final Image< R > image )
    {
        this( image, true, false );
    }

    public ImageViewer( final Image< R > image, final boolean autoContrast )
    {
        this( image, autoContrast, enableArbitraryPlaneSlicing );
    }

    public ImageViewer( final Image< R > image, final boolean autoContrast, final boolean enableArbitraryPlaneSlicing )
    {
        if ( ! CalibrationChecker.checkImage( image ) )
        {
            throw new RuntimeException( "The voxel dimensions or voxel unit of image " + image.getName() + " were not set properly and the image could thus not be visualised." );
        }

        this.image = image;
        this.enableArbitraryPlaneSlicing = enableArbitraryPlaneSlicing;
        this.channelSources = new ArrayList<>(  );

        showImage( image, autoContrast );

        DialogUtils.centerWindowToPosition( bdvHandle.getViewerPanel()  );

        // this.addMenus( new MenuActions() );
        this.installBehaviours( );

        installBenchmarking();
    }

    private void installBenchmarking()
    {
        bdvHandle.getViewerPanel().addTimePointListener( timePointIndex ->
        {
            if ( Logger.getLevel().equals( Logger.Level.Benchmark  ) )
            {
                long[] dimensionsXYZCT = image.getDimensionsXYZCT();
                long nZ = dimensionsXYZCT[ DimensionOrder.Z ];
                Random random = new Random();
                int randomZ = random.nextInt( ( int ) nZ );
                long start = System.currentTimeMillis();
                RAISlicer.createPlaneCopy( image.getRai(), image.getRai(), image.getType(), randomZ, 0, timePointIndex );
                long durationMillis = System.currentTimeMillis() - start;
                Logger.benchmark( "Loading and processing of random z-plane #" + randomZ + " [ms]: " + durationMillis );
            }
        } );
    }

    private void installBehaviours()
    {
        Behaviours behaviours = new Behaviours( new InputTriggerConfig() );
        behaviours.install( bdvHandle.getTriggerbindings(), "behaviours" );

        installStoppingBehaviour( behaviours );
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

    public Image< R > getImage()
    {
        return image;
    }

    public AffineTransform3D getSourceTransform()
    {
        for ( BdvStackSource< R > channelSource : channelSources )
        {
            final TransformedSource< R > transformedSource = ( TransformedSource< R > ) channelSource.getSources().get( 0 ).getSpimSource();
            final AffineTransform3D affineTransform3D = new AffineTransform3D();
            transformedSource.getFixedTransform( affineTransform3D );
            if ( ! affineTransform3D.isIdentity() )
            {
                // TODO: one may support transforming different channels differently
                Logger.log( "Image " + image.getName() + " has been transformed: " + affineTransform3D );
                return affineTransform3D;
            }
        }

        return new AffineTransform3D();
    }

    public void repaint( AffineTransform3D viewerTransform) {
        this.bdvHandle.getViewerPanel().setCurrentViewerTransform(viewerTransform);
    }

    public void repaint() {
        this.bdvHandle.getViewerPanel().requestRepaint();
    }

    /*
     * TODO: This fails in Macro mode during setDisplaySettings
     *
     * @param image
     * @param autoContrast
     * @param keepViewerTransform
     */
    public void replaceImage( Image image, boolean autoContrast, boolean keepViewerTransform )
    {
        final AffineTransform3D viewerTransform = getViewerTransform();
        final List< DisplaySettings > displaySettings = getDisplaySettings();

        if ( channelSources.size() > 0 ) removeAllSourcesFromBdv();

        showImage( image, autoContrast );

        if ( keepViewerTransform )
            bdvHandle.getViewerPanel().state().setViewerTransform( viewerTransform );

        if ( ! autoContrast )
            setDisplaySettings( displaySettings );
    }

    public ImageViewer< R > showImageInNewWindow( Image< R > image )
    {
        final AffineTransform3D viewerTransform = getViewerTransform();
        final List< DisplaySettings > displaySettings = getDisplaySettings();

        final ImageViewer< R > imageViewer = new ImageViewer<>( image );

        imageViewer.getBdvHandle().getViewerPanel().state().setViewerTransform( viewerTransform );
        imageViewer.setDisplaySettings( displaySettings );

        return imageViewer;
    }

    public void setDisplaySettings( List< DisplaySettings > displaySettings )
    {
        final int numChannels = displaySettings.size();
        for ( int channel = 0; channel < numChannels; channel++ )
        {
            final DisplaySettings displaySettingsChannel = displaySettings.get( channel );

            setDisplaySettings(
                    displaySettingsChannel.getDisplayRangeMin(),
                    displaySettingsChannel.getDisplayRangeMax(),
                    displaySettingsChannel.getColor(),
                    channel );
        }
    }

    private void removeAllSourcesFromBdv()
    {
        // Get a thread safe copy of the sources
        final List< SourceAndConverter< ? > > sources = new ArrayList<>( bdvHandle.getViewerPanel().state().getSources() );

        for ( SourceAndConverter< ? > source : sources )
        {
            bdvHandle.getViewerPanel().state().removeSource( source );
        }
    }

    public void setDisplaySettings( double min, double max, int channel )
    {
        setDisplaySettings( min, max, null, channel );
    }

    public void setDisplaySettings( double min, double max, ARGBType color, int channel )
    {
        final ConverterSetup converterSetup = channelSources.get( channel ).getConverterSetups().get( 0 );
        converterSetup.setDisplayRange( min, max );

        if ( color != null )
            converterSetup.setColor( color );
    }

    public List< DisplaySettings > getDisplaySettings()
    {
        final ArrayList< DisplaySettings > displaySettings = new ArrayList<>();

        for ( BdvStackSource< R > channelSource : channelSources )
        {
            final ConverterSetup converterSetup = channelSource.getConverterSetups().get( 0 );
            if ( converterSetup instanceof PlaceHolderConverterSetup ) continue;

            displaySettings.add(
                    new DisplaySettings(
                            converterSetup.getDisplayRangeMin(),
                            converterSetup.getDisplayRangeMax(),
                            converterSetup.getColor() ) );
        }

        return displaySettings;
    }

    /*
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

            setDisplaySettings(
                    setting.getDisplayRangeMin(),
                    setting.getDisplayRangeMax(),
                    null,
                    channel );
        }
    }

    public AffineTransform3D getViewerTransform()
    {
        final AffineTransform3D transform3D = new AffineTransform3D();
        bdvHandle.getViewerPanel().state().getViewerTransform( transform3D );
        return transform3D; //  transform3D.copy();
    }

    public int getCurrentTimePoint() {
        return this.bdvHandle.getViewerPanel().state().getCurrentTimepoint();
    }

    public BdvHandle getBdvHandle()
    {
        return bdvHandle;
    }

    private void showImage( Image< R > image, boolean autoContrast )
    {
        this.image = image;
        image.setViewer( this );
        ImageViewerService.imageNameToBdvImageViewer.put( image.getName(), this );
        ImageService.imageNameToImage.put( image.getName(), image );

        addToBdv( image );

        setAutoColors();

        if ( autoContrast )
            new Thread( () -> autoContrast() ).start();

        JFrame topFrame = setWindowTitle( image );
        ImageViewerService.setFocusedViewer( this );
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
        final ImageViewer viewer = this;
        topFrame.addWindowListener( new WindowAdapter()
        {
            @Override
            public void windowClosed( WindowEvent e )
            {
                super.windowClosed( e );
                ImageViewerService.setFocusedViewer( null );
            }

            @Override
            public void windowActivated( WindowEvent e )
            {
                super.windowActivated( e );
                ImageViewerService.setFocusedViewer( viewer );
            }
        } );
    }

    private void addToBdv( Image< R > image )
    {
        final AffineTransform3D scaling = getScalingTransform( image.getVoxelDimensions() );
        RandomAccessibleInterval< R > cachedCellImg = VolatileCachedCellImgs.asVolatileCachedCellImg( image );
        BdvOptions options = getBdvOptions( image, scaling );

        final long numChannels = cachedCellImg.dimension( DimensionOrder.C );
        final String[] channelNames = image.getChannelNames();

        channelSources = new ArrayList<>(  );

        for ( int channelIndex = 0; channelIndex < numChannels; channelIndex++ )
        {
            final IntervalView< R > channelView = Views.hyperSlice( cachedCellImg, DimensionOrder.C, channelIndex );

            final RandomAccessibleInterval< Volatile< R > > volatileRandomAccessibleInterval = VolatileViews.wrapAsVolatile( channelView );

            final BdvStackSource stackSource = BdvFunctions.show(
                    volatileRandomAccessibleInterval,
                    channelNames[ channelIndex ],
                    options );

            bdvHandle = stackSource.getBdvHandle();
            options = options.addTo( bdvHandle );
            channelSources.add( stackSource );
        }

        if ( ! enableArbitraryPlaneSlicing )
        {
            final BehaviourMap behaviourMap = new BehaviourMap();
            final Behaviour behaviour = new Behaviour() {};
            behaviourMap.put( TransformEventHandler3D.DRAG_ROTATE, behaviour );
            behaviourMap.put( TransformEventHandler3D.DRAG_ROTATE_FAST, behaviour );
            behaviourMap.put( TransformEventHandler3D.DRAG_ROTATE_SLOW, behaviour );
            bdvHandle.getTriggerbindings().addBehaviourMap( "BLOCKMAP", behaviourMap );
        }
    }

    private BdvOptions getBdvOptions( Image< R > image, AffineTransform3D scaling )
    {
        BdvOptions options = BdvOptions.options().axisOrder( AxisOrder.XYZT )
                .addTo( bdvHandle )
                .sourceTransform( scaling )
                .numRenderingThreads( numRenderingThreads )
                .frameTitle( VIEWER_TITLE_STUMP + image.getName() );

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

                converterSetup.setColor( de.embl.cba.bdp2.utils.Utils.getAutoColor( sourceIndex, numSources ) );
            }
        }
    }

    @Deprecated
    private RandomAccessibleInterval< Volatile< R > > asVolatile( RandomAccessibleInterval< R > rai ) {

        try {
            final RandomAccessibleInterval< Volatile< R > > volatileRai = VolatileViews.wrapAsVolatile( rai );
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
        final ArrayList< AffineTransform3D > transforms = new ArrayList<>();
        for ( BdvStackSource< R > channelSource : channelSources )
        {
            final AffineTransform3D transform3D = new AffineTransform3D();
            final Source< R > source = channelSource.getSources().get( 0 ).getSpimSource();
            source.getSourceTransform( 0, 0, transform3D );
            transforms.add( transform3D );
        }

        transform.set( transforms.get( 0 ) );
    }
}
