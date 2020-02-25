import de.embl.cba.bdp2.loading.CachedCellImgReader;
import de.embl.cba.bdp2.ui.BigDataProcessor2;
import de.embl.cba.bdp2.shear.ShearMenuDialog;
import de.embl.cba.bdp2.shear.ShearingSettings;
import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.type.numeric.integer.UnsignedShortType;

public class TestShearTransform {

    public static void main(String[] args)
    {
        String imageDirectory = "src/test/resources/shear_transform_test";
        final FileInfos fileInfos = new FileInfos( imageDirectory, FileInfos.SINGLE_CHANNEL_TIMELAPSE,
                ".*", "");
        CachedCellImg cachedCellImg = CachedCellImgReader.getCachedCellImg( fileInfos );

        BdvImageViewer imageViewer = new BdvImageViewer<UnsignedShortType>(
                cachedCellImg,
                "input",
                new double[]{1.0, 1.0, 1.0},
                "pixel");
        imageViewer.show();
        imageViewer.setDisplayRange( 0, 800, 0 );


        /**
         * Get sheared image and replaceImage it in same viewer
         * (replacing the input image);
         */
        RandomAccessibleInterval sheared = getShearedImage( cachedCellImg, imageViewer );

        imageViewer.replaceImage( imageViewer.getImage().newImage( sheared ) );

        /**
         * Compute shearing using a 5D Affine Transform.
         * This is much simpler, but also much  slower to compute
         */
    //        RandomAccessibleInterval sheared5D = getShearedImage5D( cachedCellImg, imageViewer );
    //       BdvImageViewer imageViewer2 = new BdvImageViewer<UnsignedShortType>(
    //                sheared5D,
    //                "sheared5D",
    //                new double[]{1.0, 1.0, 1.0});
    //        imageViewer2.replaceImage();
    //        imageViewer2.setDisplayRange( 0, 800, 0 );


    }

    private static RandomAccessibleInterval
    getShearedImage( CachedCellImg cachedCellImg,BdvImageViewer imageViewer )
    {
        ShearingSettings shearingSettings = new ShearingSettings();
        ShearMenuDialog dialog = new ShearMenuDialog(imageViewer);
        dialog.getShearingSettings( shearingSettings ); // sets default values.
        return BigDataProcessor2.shearRaiXYZCT( cachedCellImg, shearingSettings );
    }

    private static RandomAccessibleInterval
    getShearedImage5D( CachedCellImg cachedCellImg,BdvImageViewer imageViewer )
    {
        ShearingSettings shearingSettings = new ShearingSettings();
        ShearMenuDialog dialog = new ShearMenuDialog(imageViewer);
        dialog.getShearingSettings( shearingSettings ); // sets default values.
        return BigDataProcessor2.shearImage5D( cachedCellImg, shearingSettings );
    }
}
