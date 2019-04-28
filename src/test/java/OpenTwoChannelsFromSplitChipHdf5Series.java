import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.loading.CachedCellImgReader;
import de.embl.cba.bdp2.files.FileInfoConstants;
import de.embl.cba.bdp2.files.FileInfos;
import de.embl.cba.bdp2.viewers.ImageViewer;
import de.embl.cba.bdp2.viewers.ViewerUtils;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import java.util.ArrayList;

/**
 * IMPORTANT NOTE: Adjust Max value to 255 in the Big Data Viewer. (Settings>Brightness and Color>Max)
 */

public class OpenTwoChannelsFromSplitChipHdf5Series
{
    public static < R extends RealType< R > & NativeType< R > > void main( String[] args)
    {
        // BigDataProcessor2 bigDataProcessor2 = new BigDataProcessor2();
        final ImageViewer viewer = ViewerUtils
                .getImageViewer( ViewerUtils.BIG_DATA_VIEWER );

        String imageDirectory = "/Users/tischer/Desktop/stack_0_channel_0";

        final FileInfos fileInfos = new FileInfos( imageDirectory,
                FileInfoConstants.SINGLE_CHANNEL_TIMELAPSE,
                ".*.h5", "Data" );

        fileInfos.voxelSpacing = new double[]{ 1.0, 1.0, 10.0};

        final Image< R > image = CachedCellImgReader.asImage( fileInfos );

        final ArrayList< long[] > centres = new ArrayList<>();
        centres.add( new long[]{ 522, 1143 } );
        centres.add( new long[]{ 1407, 546 } );
        final long span = 950;


        // TODO: make work

//        final RandomAccessibleInterval< R > colorRAI
//                = SplitViewMerging.merge( image.getRai(), centres, span );
//
//        viewer.show(
//                colorRAI,
//                image.getName(),
//                image.getVoxelSpacing(),
//                image.getVoxelUnit(),
//                true );

    }

}
