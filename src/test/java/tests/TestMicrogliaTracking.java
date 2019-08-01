package tests;

import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.tracking.StaticVolumePhaseCorrelationTracker;
import de.embl.cba.bdp2.tracking.ThresholdFloodFillOverlapTracker;
import de.embl.cba.bdp2.tracking.TrackDisplayBehaviour;
import de.embl.cba.bdp2.tracking.TrackingIO;
import de.embl.cba.bdp2.ui.BigDataProcessor2;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import ij.IJ;
import net.imagej.ImageJ;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class TestMicrogliaTracking
{

    @Test
    public < R extends RealType< R > & NativeType< R > > void thresholdFloodFillTracking( ) throws IOException
    {

        final BigDataProcessor2< R > bdp = new BigDataProcessor2<>();

        String imageDirectory = "/Users/tischer/Documents/fiji-plugin-bigDataTools2/" +
                "src/test/resources/test-data/microglia-tracking-nt123/volumes";

        final Image< R > image = bdp.openImage(
                imageDirectory,
                FileInfos.SINGLE_CHANNEL_TIMELAPSE,
                ".*" );

        image.setVoxelUnit( "pixel" );
        image.setVoxelSpacing( 1.0, 1.0, 1.0 );

        final BdvImageViewer viewer = bdp.showImage( image );
        viewer.setDisplayRange( 0, 150, 0 );

        ThresholdFloodFillOverlapTracker.Settings settings = new ThresholdFloodFillOverlapTracker.Settings();

        settings.initialPositionCalibrated = new double[]{ 168, 62, 42 };
        settings.channel = 0;
        settings.timeInterval = new long[]{ 0, image.getRai().dimension( 4 ) - 1 };
        settings.threshold = 20;
        settings.trackId = "Track001";

        final ThresholdFloodFillOverlapTracker tracker =
                new ThresholdFloodFillOverlapTracker< R >( image, settings );

        new Thread( new Runnable()
        {
            @Override
            public void run()
            {
                tracker.track();
            }
        } ).start();

        new TrackDisplayBehaviour( viewer.getBdvHandle(), tracker.getTrack() );

        while( ! tracker.isFinished() )
            IJ.wait(100 );

        assertArrayEquals( new double[]{ 111.31,73.90,24.29 }, tracker.getTrack().getCalibratedPosition( 53 ), 1.0 );
        assertArrayEquals( new double[]{ 38.27,36.69,20.81 }, tracker.getTrack().getCalibratedPosition( 120 ), 1.0 );

        TrackingIO.saveTrack( new File( "/Users/tischer/Documents/fiji-plugin-bigDataTools2/" +
                "src/test/resources/test-data/microglia-tracking-nt123/track-thresholdFloodFillTracking.csv" ),
                tracker.getTrack() );
    }


    @Test
    public < R extends RealType< R > & NativeType< R > > void phaseCorrelationTracking( )
    {

        final BigDataProcessor2< R > bdp = new BigDataProcessor2<>();

        String imageDirectory = "/Users/tischer/Documents/fiji-plugin-bigDataTools2/" +
                "src/test/resources/test-data/microglia-tracking-nt3/";

        final Image< R > image = bdp.openImage(
                imageDirectory,
                FileInfos.SINGLE_CHANNEL_TIMELAPSE,
                ".*" );

        image.setVoxelUnit( "pixel" );
        image.setVoxelSpacing(
                1.0,
                1.0,
                1.0 );

        final BdvImageViewer viewer = bdp.showImage( image );
        viewer.setDisplayRange( 0, 150, 0 );

        StaticVolumePhaseCorrelationTracker.Settings settings = new StaticVolumePhaseCorrelationTracker.Settings();

//        settings.centerStartingPosition = new long[]{ 65, 44, 25 };
        settings.initialPosition = new double[]{ 168, 62, 42 };
        settings.channel = 0;
        settings.timeInterval = new long[]{ 0, 2 };
        settings.volumeDimensions = new long[]{ 30, 30, 15 };

        final StaticVolumePhaseCorrelationTracker tracker =
                new StaticVolumePhaseCorrelationTracker( image, settings, "Track01" );

        new Thread( new Runnable()
        {
            @Override
            public void run()
            {
                tracker.track();
            }
        } ).start();

        new TrackDisplayBehaviour( viewer.getBdvHandle(), tracker.getTrack() );

    }


    public static void main( String[] args ) throws IOException
    {
        final ImageJ imageJ = new ImageJ();
        imageJ.ui().showUI();
//        new TestMicrogliaTracking().trackUsingPhaseCorrelation();
        new TestMicrogliaTracking().thresholdFloodFillTracking();
    }

}
