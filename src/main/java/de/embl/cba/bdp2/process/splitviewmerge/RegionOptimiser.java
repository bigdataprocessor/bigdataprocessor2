package de.embl.cba.bdp2.process.splitviewmerge;

import bdv.util.ModifiableInterval;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.track.PhaseCorrelationTranslationComputer;
import net.imglib2.*;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import java.util.ArrayList;
import java.util.concurrent.Executors;

import static de.embl.cba.bdp2.process.splitviewmerge.SplitViewMerger.intervalXYZasXYZCT;
import static de.embl.cba.bdp2.utils.DimensionOrder.*;

public class RegionOptimiser
{


	public static < R extends RealType< R > & NativeType< R > >
	double[] optimiseIntervals(
			Image< R > image,
			ArrayList< ModifiableInterval > intervals )
	{

		final double[] shift = optimiseRegions2D( image.getRai(), intervals );

		Logger.info( "Region Centre Optimiser: Shift [Pixel]: "
				+ shift[ 0 ] + ", "
				+ shift[ 1 ] );

		return shift;
	}


	public static
	void adjustModifiableInterval( double[] shift, ModifiableInterval interval )
	{

		final long[] min = Intervals.minAsLongArray( interval );
		final long[] max = Intervals.maxAsLongArray( interval );

		for ( int d = 0; d < 2; d++ )
		{
			min[ d ] = ( long ) ( min[ d ] - shift[ d ] );
			max[ d ] = ( long) ( max[ d ] - shift[ d ] );
		}

		interval.set( new FinalInterval( min, max ) );
	}

	private static < R extends RealType< R > & NativeType< R > >
	double[] optimiseRegions2D(
			RandomAccessibleInterval< R > rai5D,
			ArrayList< ? extends Interval > intervals )
	{
		final ArrayList< RandomAccessibleInterval< R > > planes
				= new ArrayList<>();

		for ( Interval interval : intervals )
		{
			final FinalInterval interval5D = intervalXYZasXYZCT( rai5D, interval );

			final IntervalView< R > crop =
					Views.zeroMin(
							Views.interval(
									rai5D,
									interval5D ) );

			final IntervalView< R > time = Views.hyperSlice( crop, T, 0 );
			final IntervalView< R > channel = Views.hyperSlice( time, C, 0 );
			final IntervalView< R > plane =
					Views.hyperSlice(
							channel,
							Z,
							channel.dimension( Z ) / 2 );

			planes.add( plane );
		}

		final double[] shift = PhaseCorrelationTranslationComputer.computeShift(
				planes.get( 0 ), planes.get( 1 ),
				Executors.newFixedThreadPool( 2 ) );

		return shift;
	}
}
