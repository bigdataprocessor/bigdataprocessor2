import de.embl.cba.bdp2.loading.CachedCellImgReader;
import de.embl.cba.bdp2.ui.BigDataProcessor2;
import de.embl.cba.bdp2.files.FileInfoConstants;
import de.embl.cba.bdp2.files.FileInfos;
import de.embl.cba.bdp2.saving.SavingSettings;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import de.embl.cba.bdp2.viewers.ImageViewer;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.type.numeric.integer.UnsignedShortType;

public class TestTHdf5StackSaving
{

    public static void main(String[] args)
    {
        String imageDirectory = "src/test/resources/shear_transform_test";
        final FileInfos fileInfos = new FileInfos( imageDirectory, FileInfoConstants.SINGLE_CHANNEL_TIMELAPSE,
                ".*", "");
        CachedCellImg cachedCellImg = CachedCellImgReader.asCachedCellImg( fileInfos );

        ImageViewer imageViewer = new BdvImageViewer<UnsignedShortType>(
                cachedCellImg,
                "input",
                new double[]{1.0, 1.0, 1.0},
                "pixel");
        imageViewer.show();
        imageViewer.setDisplayRange( 0, 800, 0 );

        /**
		 * Save as HDF5_STACKS Stacks
         */
        final SavingSettings defaults = SavingSettings.getDefaults();
        defaults.fileType = SavingSettings.FileType.HDF5_STACKS;
        defaults.nThreads = 3;
        defaults.voxelSpacing =imageViewer.getImage().getVoxelSpacing();
        defaults.unit = imageViewer.getImage().getVoxelUnit();
        new BigDataProcessor2().saveImage( imageViewer.getImage(), defaults );

    }

}
