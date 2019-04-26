import de.embl.cba.bdp2.CachedCellImageCreator;
import de.embl.cba.bdp2.files.FileInfoConstants;
import de.embl.cba.bdp2.files.FileInfos;
import de.embl.cba.bdp2.viewers.ImageViewer;
import de.embl.cba.bdp2.viewers.ViewerUtils;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import java.util.ArrayList;
import java.util.concurrent.Executors;

/**
 * IMPORTANT NOTE: Adjust Max value to 255 in the Big Data Viewer. (Settings>Brightness and Color>Max)
 */

public class OpenTwoChannelsFromSplitChipHdf5Series
{
    public static < R extends RealType< R > > void main( String[] args)
    {
        // BigDataProcessor2 bigDataProcessor2 = new BigDataProcessor2();
        final ImageViewer viewer = ViewerUtils
                .getImageViewer( ViewerUtils.BIG_DATA_VIEWER );

        String imageDirectory = "/Users/tischer/Desktop/stack_0_channel_0";

        final FileInfos fileInfos = new FileInfos( imageDirectory,
                FileInfoConstants.SINGLE_CHANNEL_TIMELAPSE,
                ".*.h5", "Data" );
        fileInfos.voxelSize = new double[]{ 1.0, 1.0, 10.0};

        CachedCellImg img =
                CachedCellImageCreator.create(
                        fileInfos,
                        Executors.newFixedThreadPool(2));

        final ArrayList< long[] > centres = new ArrayList<>();
        centres.add( new long[]{ 522, 1143 } );
        centres.add( new long[]{ 1407, 546 } );
        final long span = 950;

        final IntervalView< R > colorRAI = getMultiColorRai( img, centres, span );


        viewer.show(
                colorRAI,
                FileInfoConstants.IMAGE_NAME,
                fileInfos.voxelSize,
                fileInfos.unit,
                true );

    }

    private static < R extends RealType< R > > IntervalView< R > getMultiColorRai( CachedCellImg img, ArrayList< long[] > centres, long span )
    {
        final long radius = span / 2;

        final long[] min = Intervals.minAsLongArray( img );
        final long[] max = Intervals.maxAsLongArray( img );

        final ArrayList< RandomAccessibleInterval< R > > crops = new ArrayList<>();

        for ( long[] centre : centres )
        {
            for ( int d = 0; d < 2; d++ )
            {
                min[ d ] = centre[ d ] - radius;
                max[ d ] = centre[ d ] + radius;
            }

            final IntervalView crop =
                    Views.zeroMin(
                            Views.interval( img, new FinalInterval( min, max ) ) );

            crops.add( crop );
        }

        final RandomAccessibleInterval< R > stack = Views.stack( crops );
        return Views.permute( stack, 3, 4 );
    }

}
