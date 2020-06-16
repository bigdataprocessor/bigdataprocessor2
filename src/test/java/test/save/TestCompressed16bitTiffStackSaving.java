package test.save;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.log.progress.LoggingProgressListener;
import de.embl.cba.bdp2.open.core.CachedCellImgReader;
import de.embl.cba.bdp2.open.core.FileInfos;
import de.embl.cba.bdp2.open.core.NamingScheme;
import de.embl.cba.bdp2.save.SavingSettings;

public class TestCompressed16bitTiffStackSaving
{
    public static void main(String[] args)
    {
        String imageDirectory = "src/test/resources/test-data/nc1-nt3-calibrated-16bit-tiff";
        final FileInfos fileInfos = new FileInfos(
                imageDirectory,
                NamingScheme.SINGLE_CHANNEL_TIMELAPSE,
                ".*",
                 "");

        final Image image = CachedCellImgReader.loadImage( fileInfos );

        final SavingSettings settings = SavingSettings.getDefaults();
        settings.fileType = SavingSettings.FileType.TIFF_VOLUMES;
        settings.numIOThreads = 3;
        settings.voxelSpacing = image.getVoxelSpacing();
        settings.voxelUnit = image.getVoxelUnit();
        settings.compression = SavingSettings.COMPRESSION_NONE;

        BigDataProcessor2.saveImage( image, settings, new LoggingProgressListener( "Files saved" ) );
    }

}
