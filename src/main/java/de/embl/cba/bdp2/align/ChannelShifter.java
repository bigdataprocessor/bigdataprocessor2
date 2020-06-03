package de.embl.cba.bdp2.align;

import de.embl.cba.bdp2.utils.DimensionOrder;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import java.util.ArrayList;
import java.util.List;

public class ChannelShifter < R extends RealType< R > & NativeType< R > >
{
	private final RandomAccessibleInterval< R > rai;
	private final long numChannels;
	private final ArrayList< RandomAccessibleInterval< R > > channelRAIs;

	public ChannelShifter( RandomAccessibleInterval< R > rai )
	{
		this.rai = rai;
		numChannels = rai.dimension( DimensionOrder.C );
		channelRAIs = getChannelRAIs();
	}

	public RandomAccessibleInterval< R > getShiftedRai( List< long[] > translationsXYZT )
	{
		ArrayList< RandomAccessibleInterval< R > > shiftedChannelRAIs =
				getShiftedRAIs( translationsXYZT );

		Interval intersect = getIntersectionInterval( shiftedChannelRAIs );

		final ArrayList< RandomAccessibleInterval< R > > croppedRAIs
				= getCroppedRAIs( shiftedChannelRAIs, intersect );

		final IntervalView< R > shiftedView = Views.permute(
				Views.stack( croppedRAIs ),
				DimensionOrder.C,
				DimensionOrder.T );

		return shiftedView;
	}

	private ArrayList< RandomAccessibleInterval< R > > getCroppedRAIs(
			ArrayList< RandomAccessibleInterval< R > > rais,
			Interval intersect )
	{
		final ArrayList< RandomAccessibleInterval< R > > cropped = new ArrayList<>();
		for ( int c = 0; c < numChannels; c++ )
		{
			final IntervalView< R > crop = Views.interval( rais.get( c ), intersect );
			cropped.add( Views.zeroMin( crop ) );
		}
		return cropped;
	}

	private Interval getIntersectionInterval( ArrayList< RandomAccessibleInterval< R > > shiftedChannelRAIs )
	{
		Interval intersect = shiftedChannelRAIs.get( 0 );
		for ( int c = 1; c < numChannels; c++ )
			intersect = Intervals.intersect( intersect, shiftedChannelRAIs.get( c ) );
		return intersect;
	}

	private ArrayList< RandomAccessibleInterval< R > > getShiftedRAIs( List< long[] > translationsXYZT )
	{
		ArrayList< RandomAccessibleInterval< R > > shiftedChannelRAIs = new ArrayList<>();

		for ( int c = 0; c < numChannels; c++ )
			shiftedChannelRAIs.add(
					Views.translate( channelRAIs.get( c ), translationsXYZT.get( c ) ) );

		return shiftedChannelRAIs;
	}

	private ArrayList< RandomAccessibleInterval< R > > getChannelRAIs()
	{
		ArrayList< RandomAccessibleInterval< R > > channelRais = new ArrayList<>();

		for ( int c = 0; c < numChannels; c++ )
			channelRais.add( Views.hyperSlice( rai, DimensionOrder.C, c ) );

		return channelRais;
	}

	private long getNumChannels()
	{
		return numChannels;
	}
}
