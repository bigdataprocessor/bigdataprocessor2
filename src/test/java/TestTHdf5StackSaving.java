import de.embl.cba.bigDataTools2.CachedCellImageCreator;
import de.embl.cba.bigDataTools2.dataStreamingGUI.BigDataConverter;
import de.embl.cba.bigDataTools2.fileInfoSource.FileInfoConstants;
import de.embl.cba.bigDataTools2.fileInfoSource.FileInfoSource;
import de.embl.cba.bigDataTools2.saving.SavingSettings;
import de.embl.cba.bigDataTools2.viewers.BdvImageViewer;
import de.embl.cba.bigDataTools2.viewers.ImageViewer;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.type.numeric.integer.UnsignedShortType;

public class TestTHdf5StackSaving
{

    public static void main(String[] args)
    {
        String imageDirectory = "src/test/resources/shear_transform_test";
        final FileInfoSource fileInfoSource = new FileInfoSource( imageDirectory, FileInfoConstants.SINGLE_CHANNEL_TIMELAPSE,
                ".*", "", true);
        CachedCellImg cachedCellImg = CachedCellImageCreator.create(fileInfoSource, null);

        ImageViewer imageViewer = new BdvImageViewer<UnsignedShortType>(
                cachedCellImg,
                "input",
                new double[]{1.0, 1.0, 1.0});
        imageViewer.show();
        imageViewer.setDisplayRange( 0, 800, 0 );

        /**
		 * Save as HDF5_STACKS Stacks
         */
        final SavingSettings defaults = SavingSettings.getDefaults();
        defaults.fileType = SavingSettings.FileType.HDF5_STACKS;
        defaults.nThreads = 3;
        new BigDataConverter().saveImage( defaults, imageViewer );


    }

}
