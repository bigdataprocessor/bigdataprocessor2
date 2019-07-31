package tests;

import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.tracking.StaticVolumePhaseCorrelationTracker;
import de.embl.cba.bdp2.tracking.TrackingSettings;
import de.embl.cba.bdp2.ui.BigDataProcessor2;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.bdp2.utils.Point3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.junit.Test;

public class TestMicrogliaTracking
{

    @Test
    public < R extends RealType< R > & NativeType< R > > void trackUsingPhaseCorrelation( )
    {


        final BigDataProcessor2< R > bdp = new BigDataProcessor2<>();

        String imageDirectory = "/Users/tischer/Documents/fiji-plugin-bigDataTools2/" +
                "src/test/resources/test-data/microglia-tracking/volumes";

        final Image< R > image = bdp.openImage(
                imageDirectory,
                FileInfos.SINGLE_CHANNEL_TIMELAPSE,
                ".*" );

        image.setVoxelUnit( "micrometer" );
        image.setVoxelSpacing(
                0.2166,
                0.2166,
                1.0000 );

        bdp.showImage( image );

        StaticVolumePhaseCorrelationTracker.Settings settings = new StaticVolumePhaseCorrelationTracker.Settings();

        settings.centerStartingPosition = new long[]{ 168, 60, 43 };
        settings.channel = 0;
        settings.timeInterval = new long[]{ 0, 5};
        settings.volumeDimensions = new long[]{ 50, 50, 25 };

        final StaticVolumePhaseCorrelationTracker tracker = new StaticVolumePhaseCorrelationTracker( image, settings );


    }

}
