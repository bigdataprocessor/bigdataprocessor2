import de.embl.cba.bdp2.load.CachedCellImgReader;
import de.embl.cba.bdp2.progress.LoggingProgressListener;
import de.embl.cba.bdp2.ui.BigDataProcessor2;
import de.embl.cba.bdp2.load.files.FileInfos;
import de.embl.cba.bdp2.save.SavingSettings;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.type.numeric.integer.UnsignedShortType;

public class TestTHdf5StackSaving
{

    public static void main(String[] args)
    {
        String imageDirectory = "src/test/resources/shear_transform_test";
        final FileInfos fileInfos = new FileInfos( imageDirectory, FileInfos.SINGLE_CHANNEL_TIMELAPSE,
                ".*", "");
        CachedCellImg cachedCellImg = CachedCellImgReader.createCachedCellImg( fileInfos );

       BdvImageViewer imageViewer = new BdvImageViewer<UnsignedShortType>(
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
        defaults.numIOThreads = 3;
        defaults.voxelSpacing =imageViewer.getImage().getVoxelSpacing();
        defaults.voxelUnit = imageViewer.getImage().getVoxelUnit();
        new BigDataProcessor2().saveImage( imageViewer.getImage(), defaults, new LoggingProgressListener( "Files saved" ) );

    }

}
