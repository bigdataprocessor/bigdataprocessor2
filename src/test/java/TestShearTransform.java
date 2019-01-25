import de.embl.cba.bigDataTools2.CachedCellImageCreator;
import de.embl.cba.bigDataTools2.dataStreamingGUI.BigDataConverter;
import de.embl.cba.bigDataTools2.dataStreamingGUI.ObliqueMenuDialog;
import de.embl.cba.bigDataTools2.dataStreamingGUI.ShearingSettings;
import de.embl.cba.bigDataTools2.fileInfoSource.FileInfoConstants;
import de.embl.cba.bigDataTools2.fileInfoSource.FileInfoSource;
import de.embl.cba.bigDataTools2.viewers.BdvImageViewer;
import de.embl.cba.bigDataTools2.viewers.ImageViewer;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.type.numeric.integer.UnsignedShortType;

public class TestShearTransform {

    public static void main(String[] args) {
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
         * Get sheared image and show it in same viewer
         * (replacing the input image);
         */
        RandomAccessibleInterval sheared = getShearedImage( cachedCellImg, imageViewer );
        imageViewer.show( sheared, imageViewer.getVoxelSize(), "sheared" );
        imageViewer.setDisplayRange( 0, 800, 0 );

    }

    private static RandomAccessibleInterval getShearedImage( CachedCellImg cachedCellImg, ImageViewer imageViewer )
    {
        ShearingSettings shearingSettings = new ShearingSettings();
        ObliqueMenuDialog dialog = new ObliqueMenuDialog(imageViewer);
        dialog.getShearingSettings( shearingSettings ); // sets default values.
        return BigDataConverter.shearImage( cachedCellImg, shearingSettings );
    }
}
