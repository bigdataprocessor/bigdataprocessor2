import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.loading.CachedCellImgReader;
import de.embl.cba.bdp2.progress.ProgressListener;
import de.embl.cba.bdp2.ui.BigDataProcessor2;
import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.saving.SavingSettings;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import de.embl.cba.bdp2.viewers.ImageViewer;
import net.imglib2.type.numeric.integer.UnsignedShortType;

public class TestTiffPlaneSaving
{

    public static void main(String[] args)
    {
        String imageDirectory = "src/test/resources/shear_transform_test";
        final FileInfos fileInfos = new FileInfos( imageDirectory, FileInfos.SINGLE_CHANNEL_TIMELAPSE,
                ".*", "");
        final Image image = CachedCellImgReader.loadImage( fileInfos );

        ImageViewer imageViewer = new BdvImageViewer<UnsignedShortType>( image );
        imageViewer.show();
        imageViewer.setDisplayRange( 0, 800, 0 );

        /**
		 * Save as Tiff Planes
         */
        final SavingSettings defaults = SavingSettings.getDefaults();
        defaults.fileType = SavingSettings.FileType.TIFF_PLANES;
        defaults.nThreads = 3;
        defaults.voxelSpacing =imageViewer.getImage().getVoxelSpacing();
        defaults.voxelUnit = imageViewer.getImage().getVoxelUnit();
        new BigDataProcessor2().saveImage( image, defaults, new ProgressListener()
        {
            @Override
            public void progress( long current, long total )
            {

            }
        } );
    }

}
