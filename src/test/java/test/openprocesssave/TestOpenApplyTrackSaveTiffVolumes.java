package test.openprocesssave;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.log.progress.LoggingProgressListener;
import de.embl.cba.bdp2.save.SavingSettings;
import org.junit.Test;
import test.Utils;

import java.io.File;

import static de.embl.cba.bdp2.open.NamingSchemes.MULTI_CHANNEL_VOLUMES;
import static de.embl.cba.bdp2.open.NamingSchemes.TIF;

public class TestOpenApplyTrackSaveTiffVolumes
{
    public static Image image;
    public static Image trackedImage;

    public static void main(String[] args)
    {
        Utils.prepareInteractiveMode();

        new TestOpenApplyTrackSaveTiffVolumes().run();
        BigDataProcessor2.showImage( image, true );
        BigDataProcessor2.showImage( trackedImage, true );
    }

    @Test
    public void run()
    {
        final String directory = "src/test/resources/test/tiff-nc2-nt6";

        // open
        image = BigDataProcessor2.openTIFFSeries( directory, MULTI_CHANNEL_VOLUMES + TIF );
        image.setVoxelDimensions( new double[]{1.0, 1.0, 1.0} );

        // track
        trackedImage = BigDataProcessor2.applyTrack( new File( "src/test/resources/test/tracks/tiff-nc2-nt6-with-z-drift.json" ), TestOpenApplyTrackSaveTiffVolumes.image, false );

        // save
        final SavingSettings settings = SavingSettings.getDefaults();
        settings.volumesFilePathStump = "src/test/resources/test/output/tracked-tiff/" + trackedImage.getName();
        settings.tStart = 0;
        settings.tEnd = trackedImage.getNumTimePoints() - 1;
        BigDataProcessor2.saveImage( trackedImage, settings, new LoggingProgressListener( "Files saved" ) );
    }
}
