import de.embl.cba.bdp2.read.CachedCellImgReader;
import de.embl.cba.bdp2.log.progress.LoggingProgressListener;
import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.read.FileInfos;
import de.embl.cba.bdp2.read.NamingScheme;
import de.embl.cba.bdp2.save.SavingSettings;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.type.numeric.integer.UnsignedShortType;

public class TestTHdf5ImarisSaving {

    public static void main(String[] args) {
        String imageDirectory = "src/test/resources/shear_transform_test";
        final FileInfos fileInfos = new FileInfos(imageDirectory, NamingScheme.SINGLE_CHANNEL_TIMELAPSE,
                ".*", "");
        CachedCellImg cachedCellImg = CachedCellImgReader.createCachedCellImg( fileInfos );

       BdvImageViewer imageViewer = new BdvImageViewer<UnsignedShortType>(
                cachedCellImg,
                "input",
                new double[]{1.0, 1.0, 1.0},
                "pixel");
        imageViewer.show( true );
        imageViewer.setDisplayRange(0, 800, 0);

        /**
         * Save as IMARIS_STACKS Stacks
         */
        final SavingSettings defaults = SavingSettings.getDefaults();
        defaults.fileType = SavingSettings.FileType.IMARIS_VOLUMES;

        defaults.voxelSpacing = imageViewer.getImage().getVoxelSpacing();
        defaults.voxelUnit = imageViewer.getImage().getVoxelUnit();

        new BigDataProcessor2().saveImage( imageViewer.getImage(), defaults, new LoggingProgressListener( "Files saved" ) );
    }

}
