package de.embl.cba.bdp2.loading;

import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import java.util.ArrayList;

import static de.embl.cba.bdp2.utils.DimensionOrder.C;

public class MultiFromSingleChannelImageCreator
{
	/**
	 * Crops a single channel 5D image into multiple and stacks them as
	 * a multi-channel. The crops are specified as square columns.
	 *
	 * @param singleChannelRai
	 * @param channelCentres
	 * @param span
	 * @param <R>
	 * @return
	 */
	public static < R extends RealType< R > > RandomAccessibleInterval< R >
	create(
			RandomAccessibleInterval< R > singleChannelRai,
			ArrayList< long[] > channelCentres,
			long span )
	{
		final long radius = span / 2;

		final long[] min = Intervals.minAsLongArray( singleChannelRai );
		final long[] max = Intervals.maxAsLongArray( singleChannelRai );

		final ArrayList< RandomAccessibleInterval< R > > singleChannel4Ds = new ArrayList<>();

		for ( long[] centre : channelCentres )
		{
			for ( int d = 0; d < 2; d++ )
			{
				min[ d ] = centre[ d ] - radius;
				max[ d ] = centre[ d ] + radius;
			}

			final IntervalView crop =
					Views.zeroMin(
							Views.interval(
									singleChannelRai,
									new FinalInterval( min, max ) ) );

			final IntervalView singleChannel4D = Views.hyperSlice( crop, C, 0 );

			singleChannel4Ds.add( singleChannel4D );
		}

		final RandomAccessibleInterval< R > multiChannel5D =
				Views.stack( singleChannel4Ds );

		return Views.permute( multiChannel5D, 3, 4 );
	}
}
