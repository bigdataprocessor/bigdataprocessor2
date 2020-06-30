package test;

import de.embl.cba.bdp2.drift.devel.StaticVolumePhaseCorrelationTracker;
import de.embl.cba.bdp2.drift.devel.ThresholdFloodFillOverlapTracker;
import de.embl.cba.bdp2.drift.track.TrackApplier;
import de.embl.cba.bdp2.drift.track.TrackDisplayBehaviour;
import de.embl.cba.bdp2.drift.track.TrackIO;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.open.core.NamingSchemes;
import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import de.embl.cba.bdv.utils.BdvUtils;
import ij.IJ;
import loci.common.DebugTools;
import net.imagej.ImageJ;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class TestMicrogliaTracking
{

    public static boolean showImages = false;

    ////@Test
    public < R extends RealType< R > & NativeType< R > > void thresholdFloodFillTracking( ) throws IOException
    {

        DebugTools.setRootLevel("OFF"); // Bio-Formats

        String imageDirectory = "/Users/tischer/Documents/fiji-plugin-bigDataProcessor2/" +
                "src/test/resources/test-data/microglia-drift-nt123/volumes";

        final Image< R > image = BigDataProcessor2.openImage(
                imageDirectory,
                NamingSchemes.SINGLE_CHANNEL_TIMELAPSE,
                ".*" );

        image.setVoxelUnit( "pixel" );
        image.setVoxelSize( 1.0, 1.0, 1.0 );

        BdvImageViewer viewer = null;
        if ( showImages )
        {
            viewer = BigDataProcessor2.showImage( image);
            viewer.setDisplaySettings( 0, 150,  0 );
        }

        ThresholdFloodFillOverlapTracker.Settings settings = new ThresholdFloodFillOverlapTracker.Settings();

        settings.initialPositionCalibrated = new double[]{ 168, 62, 42 };
        settings.channel = 0;
        settings.timeInterval = new int[]{ 0, (int) image.getRai().dimension( 4 ) - 1 };
        settings.threshold = 20;
        settings.trackId = "Track001";

        final ThresholdFloodFillOverlapTracker tracker =
                new ThresholdFloodFillOverlapTracker< R >( image, settings );

        tracker.track();

        if ( showImages )
            new TrackDisplayBehaviour( viewer.getBdvHandle(), tracker.getTrack() );

        while( ! tracker.isFinished() )
            IJ.wait(100 );

        assertArrayEquals( new double[]{ 111.31,73.90,24.29 }, tracker.getTrack().getPosition( 53 ), 1.0 );
        assertArrayEquals( new double[]{ 38.27,36.69,20.81 }, tracker.getTrack().getPosition( 120 ), 1.0 );

        TrackIO.saveTrack( new File( "/Users/tischer/Documents/fiji-plugin-bigDataProcessor2/" +
                "src/test/resources/test-data/microglia-drift-nt123/drift-thresholdFloodFillTracking.csv" ),
                tracker.getTrack() );

        if ( showImages )
        {
            final Image< R > trackViewImage = new TrackApplier<>( image ).applyTrack( tracker.getTrack() );
            new BdvImageViewer<>( trackViewImage );
        }
    }


    //@Test
    public < R extends RealType< R > & NativeType< R > > void trackView( )
    {

        DebugTools.setRootLevel("OFF"); // Bio-Formats

        String imageDirectory = "/Users/tischer/Documents/fiji-plugin-bigDataProcessor2/" +
                "src/test/resources/test-data/microglia-drift-nt123/volumes";

        final Image< R > image = BigDataProcessor2.openImage(
                imageDirectory,
                NamingSchemes.SINGLE_CHANNEL_TIMELAPSE,
                ".*" );

        image.setVoxelUnit( "pixel" );
        image.setVoxelSize( 1.0, 1.0, 1.0 );

        BdvImageViewer viewer = null;
        if ( showImages )
        {
            viewer = BigDataProcessor2.showImage( image);
            viewer.setDisplaySettings( 0, 150, 0 );
        }

        ThresholdFloodFillOverlapTracker.Settings settings = new ThresholdFloodFillOverlapTracker.Settings();

        settings.initialPositionCalibrated = new double[]{ 168, 62, 42 };
        settings.channel = 0;
        settings.timeInterval = new int[]{ 0, 10 };
        settings.threshold = 20;
        settings.trackId = "Track001";

        final ThresholdFloodFillOverlapTracker tracker =
                new ThresholdFloodFillOverlapTracker< R >( image, settings );

        tracker.track();

        if ( showImages )
        {
            new TrackDisplayBehaviour( viewer.getBdvHandle(), tracker.getTrack() );
            final Image< R > trackViewImage = new TrackApplier<>( image ).applyTrack( tracker.getTrack() );

            final BdvImageViewer< R > newViewer = new BdvImageViewer<>( trackViewImage );
            newViewer.setDisplaySettings( viewer.getDisplaySettings() );
            BdvUtils.moveToPosition( newViewer.getBdvHandle(), new double[]{ 0, 0, 0 }, 0, 100 );
        }
    }


    //@Test
    public < R extends RealType< R > & NativeType< R > > void phaseCorrelationTracking( )
    {
        DebugTools.setRootLevel("OFF"); // Bio-Formats

        String imageDirectory = "/Users/tischer/Documents/fiji-plugin-bigDataProcessor2/" +
                "src/test/resources/test-data/microglia-drift-nt3/";

        final Image< R > image = BigDataProcessor2.openImage(
                imageDirectory,
                NamingSchemes.SINGLE_CHANNEL_TIMELAPSE,
                ".*" );

        image.setVoxelUnit( "pixel" );
        image.setVoxelSize(
                1.0,
                1.0,
                1.0 );

        BdvImageViewer viewer = null;
        if ( showImages )
        {
            viewer = BigDataProcessor2.showImage( image);
            viewer.setDisplaySettings( 0, 150, 0 );
        }

        StaticVolumePhaseCorrelationTracker.Settings settings = new StaticVolumePhaseCorrelationTracker.Settings();

//        settings.centerStartingPosition = new long[]{ 65, 44, 25 };
        settings.initialPosition = new double[]{ 168, 62, 42 };
        settings.channel = 0;
        settings.timeInterval = new int[]{ 0, 2 };
        settings.volumeDimensions = new long[]{ 30, 30, 15 };

        final StaticVolumePhaseCorrelationTracker tracker =
                new StaticVolumePhaseCorrelationTracker( image, settings, "Track01" );

        tracker.track();

        if ( showImages )
            new TrackDisplayBehaviour( viewer.getBdvHandle(), tracker.getTrack() );

    }

    public static void main( String[] args ) throws IOException
    {
        showImages = true;
        final ImageJ imageJ = new ImageJ();
        imageJ.ui().showUI();
//        new TestMicrogliaTracking().trackUsingPhaseCorrelation();
//        new TestMicrogliaTracking().thresholdFloodFillTracking();
        new TestMicrogliaTracking().trackView();
    }
}
