package test.opensave;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.log.progress.LoggingProgressListener;
import de.embl.cba.bdp2.save.Projector;
import de.embl.cba.bdp2.save.SaveFileType;
import de.embl.cba.bdp2.save.SavingSettings;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.junit.Test;
import test.Utils;

import static de.embl.cba.bdp2.utils.DimensionOrder.X;
import static de.embl.cba.bdp2.utils.DimensionOrder.Y;
import static de.embl.cba.bdp2.utils.DimensionOrder.Z;

public class TestOpenSingleChannelFloatTiffVolumeWithBioFormatsAndSaveAsTiffVolume
{
    private static Image image;

    public static void main( String[] args)
    {
        Utils.prepareInteractiveMode();

        new TestOpenSingleChannelFloatTiffVolumeWithBioFormatsAndSaveAsTiffVolume().run();
        BigDataProcessor2.showImage( image );
    }

    @Test
    public void run()
    {
        String file = "src/test/resources/test/tiff-nc1-nt1-ImageJFloat/mri-stack-ij-float.tif";
        //file = "/Volumes/cba/exchange/Shuting/Ecad_Sqh_100x_20210511_01_decon.ics";

        image = BigDataProcessor2.openBioFormats( file, 0 );
        image.setVoxelDimensions( 1, 1, 20 );

        final SavingSettings settings = SavingSettings.getDefaults();
        settings.volumesFilePathStump = "src/test/resources/test-output/" + image.getName() + "/volumes/" + image.getName();
        settings.saveVolumes = true;
        settings.projectionsFilePathStump = "src/test/resources/test-output/" + image.getName() + "/projections/" + image.getName();
        settings.saveProjections = true;
        settings.projectionMode = Projector.SUM;
        settings.fileType = SaveFileType.TIFFVolumes;
        settings.numProcessingThreads = 4;
        settings.numIOThreads = 1;
        settings.compression = SavingSettings.COMPRESSION_NONE;
        settings.tStart = 0;
        settings.tEnd = image.getNumTimePoints() - 1;

        BigDataProcessor2.saveImage( image, settings, new LoggingProgressListener( "Progress" ) );
    }
}
