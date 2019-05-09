import de.embl.cba.bdp2.loading.CachedCellImgReader;
import de.embl.cba.bdp2.progress.ProgressListener;
import de.embl.cba.bdp2.ui.BigDataProcessor2;
import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.saving.SavingSettings;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import de.embl.cba.bdp2.viewers.ImageViewer;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.type.numeric.integer.UnsignedShortType;

public class TestTHdf5ImarisSaving {

    public static void main(String[] args) {
        String imageDirectory = "src/test/resources/shear_transform_test";
        final FileInfos fileInfos = new FileInfos(imageDirectory, FileInfos.SINGLE_CHANNEL_TIMELAPSE,
                ".*", "");
        CachedCellImg cachedCellImg = CachedCellImgReader.asCachedCellImg( fileInfos );

        ImageViewer imageViewer = new BdvImageViewer<UnsignedShortType>(
                cachedCellImg,
                "input",
                new double[]{1.0, 1.0, 1.0},
                "pixel");
        imageViewer.show();
        imageViewer.setDisplayRange(0, 800, 0);

        /**
         * Save as IMARIS_STACKS Stacks
         */
        final SavingSettings defaults = SavingSettings.getDefaults();
        defaults.fileType = SavingSettings.FileType.IMARIS_STACKS;
        defaults.fileBaseNameIMARIS = "file";
        defaults.parentDirectory = "src/test/resources/";
        defaults.voxelSpacing = imageViewer.getImage().getVoxelSpacing();
        defaults.voxelUnit = imageViewer.getImage().getVoxelUnit();
        new BigDataProcessor2().saveImage( imageViewer.getImage(), defaults, new ProgressListener()
        {
            @Override
            public void progress( long current, long total )
            {

            }
        } );

    }

}
