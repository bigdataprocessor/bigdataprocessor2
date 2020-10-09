package develop;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.open.fileseries.FileSeriesCachedCellImgCreator;
import de.embl.cba.bdp2.log.progress.LoggingProgressListener;
import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.open.fileseries.FileInfos;
import de.embl.cba.bdp2.open.NamingSchemes;
import de.embl.cba.bdp2.save.SavingSettings;
import de.embl.cba.bdp2.viewers.ImageViewer;
import net.imglib2.type.numeric.integer.UnsignedShortType;

public class TestTiffPlaneSaving
{
    public static void main(String[] args)
    {
        String imageDirectory = "src/test/resources/shear_transform_test";
        final FileInfos fileInfos = new FileInfos( imageDirectory, NamingSchemes.SINGLE_CHANNEL_TIMELAPSE,
                ".*", "");
        final Image image = FileSeriesCachedCellImgCreator.createImage( fileInfos );

        ImageViewer imageViewer = new ImageViewer<UnsignedShortType>( image );

        imageViewer.setDisplaySettings( 0, 800, 0 );

        /**
		 * Save as Tiff Planes
         */
        final SavingSettings defaults = SavingSettings.getDefaults();
        defaults.saveFileType = SavingSettings.SaveFileType.TIFF_PLANES;
        defaults.numIOThreads = 3;
        defaults.voxelSize =imageViewer.getImage().getVoxelSize();
        defaults.voxelUnit = imageViewer.getImage().getVoxelUnit();
        new BigDataProcessor2().saveImage( image, defaults, new LoggingProgressListener( "Files saved" ) );
    }

}
