package develop;

import de.embl.cba.bdp2.open.core.CachedCellImgReader;
import de.embl.cba.bdp2.open.core.FileInfos;
import de.embl.cba.bdp2.open.core.NamingSchemes;
import net.imglib2.cache.img.CachedCellImg;

public class TestTHdf5StackSaving
{

    public static void main(String[] args)
    {
        String imageDirectory = "src/test/resources/shear_transform_test";
        final FileInfos fileInfos = new FileInfos( imageDirectory, NamingSchemes.SINGLE_CHANNEL_TIMELAPSE,
                ".*", "");
        CachedCellImg cachedCellImg = CachedCellImgReader.createCachedCellImg( fileInfos );

//        new Image< UnsignedShortType >
//        BdvImageViewer imageViewer = new BdvImageViewer<UnsignedShortType>(
//                cachedCellImg,
//                "input",
//                new double[]{1.0, 1.0, 1.0},
//                "pixel");
//        imageViewer.show( true );
//        imageViewer.setDisplayRange( 0, 800, 0 );
//
//        /**
//		 * Save as HDF5_STACKS Stacks
//         */
//        final SavingSettings defaults = SavingSettings.getDefaults();
//        defaults.fileType = SavingSettings.FileType.HDF5_VOLUMES;
//        defaults.numIOThreads = 3;
//        defaults.voxelSpacing =imageViewer.getImage().getVoxelSpacing();
//        defaults.voxelUnit = imageViewer.getImage().getVoxelUnit();
//        new BigDataProcessor2().saveImage( imageViewer.getImage(), defaults, new LoggingProgressListener( "Files saved" ) );

    }

}
