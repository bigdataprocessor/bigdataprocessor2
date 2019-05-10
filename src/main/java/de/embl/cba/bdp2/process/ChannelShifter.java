package de.embl.cba.bdp2.process;

import de.embl.cba.bdp2.utils.DimensionOrder;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import java.util.ArrayList;

public class ChannelShifter < T extends RealType< T > & NativeType< T > >
{
	private final RandomAccessibleInterval< T > rai;
	private final long numChannels;
	private final ArrayList< RandomAccessibleInterval< T > > channelRAIs;

	public ChannelShifter( RandomAccessibleInterval< T > rai )
	{
		this.rai = rai;
		numChannels = rai.dimension( DimensionOrder.C );
		channelRAIs = getChannelRAIs();
	}

	public RandomAccessibleInterval< T > getChannelShiftedRAI( ArrayList< long[] > translations )
	{
		ArrayList< RandomAccessibleInterval< T > > shiftedChannelRAIs =
				getShiftedRAIs( translations );

		Interval intersect = getIntersectionInterval( shiftedChannelRAIs );

		final ArrayList< RandomAccessibleInterval< T > > croppedRAIs
				= getCroppedRAIs( shiftedChannelRAIs, intersect );

		final IntervalView< T > shiftedView = Views.permute(
				Views.stack( croppedRAIs ),
				DimensionOrder.C,
				DimensionOrder.T );

		return shiftedView;
	}

	public ArrayList< RandomAccessibleInterval< T > > getCroppedRAIs(
			ArrayList< RandomAccessibleInterval< T > > rais,
			Interval intersect )
	{
		final ArrayList< RandomAccessibleInterval< T > > cropped = new ArrayList<>();
		for ( int c = 0; c < numChannels; c++ )
		{
			final IntervalView< T > crop =
					Views.interval( rais.get( c ), intersect );
			cropped.add(  crop );
		}

		return cropped;
	}

	public Interval getIntersectionInterval( ArrayList< RandomAccessibleInterval< T > > shiftedChannelRAIs )
	{
		Interval intersect = shiftedChannelRAIs.get( 0 );
		for ( int c = 1; c < numChannels; c++ )
			intersect = Intervals.intersect( intersect, shiftedChannelRAIs.get( c ) );
		return intersect;
	}

	public ArrayList< RandomAccessibleInterval< T > > getShiftedRAIs(
			ArrayList< long[] > translations )
	{
		ArrayList< RandomAccessibleInterval< T > > shiftedChannelRAIs = new ArrayList<>();

		for ( int c = 0; c < numChannels; c++ )
			shiftedChannelRAIs.add(
					Views.translate( channelRAIs.get( c ), translations.get( c ) ) );

		return shiftedChannelRAIs;
	}

	private ArrayList< RandomAccessibleInterval< T > > getChannelRAIs()
	{
		ArrayList< RandomAccessibleInterval< T > > channelRais = new ArrayList<>();

		for ( int c = 0; c < numChannels; c++ )
			channelRais.add( Views.hyperSlice( rai, DimensionOrder.C, c ) );

		return channelRais;
	}

	public long getNumChannels()
	{
		return numChannels;
	}
}
