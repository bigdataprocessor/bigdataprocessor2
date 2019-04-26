import de.embl.cba.bdp2.CachedCellImageCreator;
import de.embl.cba.bdp2.fileinfosource.FileInfoConstants;
import de.embl.cba.bdp2.fileinfosource.FileInfoSource;
import de.embl.cba.bdp2.ui.BigDataProcessor2;
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

import static de.embl.cba.bdp2.utils.DimensionOrder.C;

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

        final FileInfoSource fileInfoSource = new FileInfoSource( imageDirectory,
                FileInfoConstants.SINGLE_CHANNEL_TIMELAPSE,
                ".*.h5", "Data" );
        fileInfoSource.voxelSize = new double[]{ 1.0, 1.0, 10.0};

        CachedCellImg ch0 =
                CachedCellImageCreator.create(
                        fileInfoSource,
                        Executors.newFixedThreadPool(2));

        CachedCellImg ch1 =
                CachedCellImageCreator.create(
                        fileInfoSource,
                        Executors.newFixedThreadPool(2));

        final long[] centre0 = { 522, 1143 };
        final long[] centre1 = { 1407, 546 };
        final long span = 950;
        final long radius = span / 2;


        final long[] min = Intervals.minAsLongArray( ch0 );
        final long[] max = Intervals.maxAsLongArray( ch0 );

        for ( int d = 0; d < 2; d++ )
        {
            min[ d ] = centre0[ d ] - radius;
            max[ d ] = centre0[ d ] + radius;
        }
        final IntervalView ch0crop =
                Views.zeroMin(
                    Views.interval( ch0, new FinalInterval( min, max ) ) );


        for ( int d = 0; d < 2; d++ )
        {
            min[ d ] = centre1[ d ] - radius;
            max[ d ] = centre1[ d ] + radius;
        }
        final IntervalView ch1crop =
                Views.zeroMin(
                    Views.interval( ch1, new FinalInterval( min, max ) ) );

        final ArrayList< RandomAccessibleInterval< R > > rais = new ArrayList<>();
        rais.add( Views.hyperSlice( ch0crop, C,0 ) );
        rais.add( Views.hyperSlice( ch1crop, C,0 ) );

        final RandomAccessibleInterval< R > stack = Views.stack( rais );

        final IntervalView< R > colorRAI = Views.permute( stack, 3, 4 );

        viewer.show(
                colorRAI,
                FileInfoConstants.IMAGE_NAME,
                fileInfoSource.voxelSize,
                fileInfoSource.unit,
                true );

    }

}
