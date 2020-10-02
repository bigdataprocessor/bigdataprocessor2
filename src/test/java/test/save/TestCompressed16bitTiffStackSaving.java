package test.save;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.log.progress.LoggingProgressListener;
import de.embl.cba.bdp2.save.SavingSettings;

import static de.embl.cba.bdp2.open.core.NamingSchemes.*;

public class TestCompressed16bitTiffStackSaving
{
    public static void main(String[] args)
    {
        new TestCompressed16bitTiffStackSaving().run();
    }

    public void run()
    {
        final String directory = "/Users/tischer/Downloads/tmp-luxendo";

        String regExp = LUXENDO_REGEXP.replace( "STACK", "" + 0 );

        final Image image = BigDataProcessor2.openImageFromHdf5(
                directory,
                regExp,
                ".*",
                "Data"
        );

        image.setVoxelSize( 1.0, 1.0, 1.0 );

        final SavingSettings settings = SavingSettings.getDefaults();
        settings.saveFileType = SavingSettings.SaveFileType.TIFF_VOLUMES;
        settings.numProcessingThreads = 4;
        settings.numIOThreads = 1;
        settings.voxelSize = image.getVoxelSize();
        settings.voxelUnit = image.getVoxelUnit();
        settings.compression = SavingSettings.COMPRESSION_LZW;
        settings.tStart = 0;
        settings.tEnd = image.getNumTimePoints() - 1;

        Logger.setLevel( Logger.Level.Debug );
        BigDataProcessor2.saveImage( image, settings, new LoggingProgressListener( "Files saved" ) );
    }
}
