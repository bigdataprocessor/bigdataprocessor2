package test.save;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.log.progress.LoggingProgressListener;
import de.embl.cba.bdp2.read.CachedCellImgReader;
import de.embl.cba.bdp2.read.FileInfos;
import de.embl.cba.bdp2.read.NamingScheme;
import de.embl.cba.bdp2.save.SavingSettings;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import net.imglib2.type.numeric.integer.UnsignedShortType;

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
        settings.compression = SavingSettings.COMPRESSION_ZLIB;

        BigDataProcessor2.saveImage( image, settings, new LoggingProgressListener( "Files saved" ) );
    }

}
