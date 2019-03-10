package de.embl.cba.bdp2.process;

import de.embl.cba.bdp2.neighborhood.RectangleShape2;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.converter.Converters;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

import java.util.Arrays;

public class LazyDownsampler < R extends RealType< R > & NativeType< R > >
{

	final RandomAccessibleInterval< R > rai;
	final long[] span;

	/**
	 *
	 * The regions which are averaged are span * 2 + 1, for each dimension.
	 * This ensures that the rectangle is symmetric around the central pixel...
	 */
	public LazyDownsampler( RandomAccessibleInterval< R > rai, long[] span )
	{
		this.rai = rai;
		this.span = span;
	}

	public  RandomAccessibleInterval< R > get()
	{
		final long[] neighborhoodCenterDistance =
				Arrays.stream( span ).map( x -> 2 * x + 1 ).toArray();
		return Views.subsample( averageView( rai, span ), neighborhoodCenterDistance );
	}

	private RandomAccessibleInterval< R > averageView(
			RandomAccessibleInterval< R > rai,
			long[] span )
	{
		Shape shape = new RectangleShape2( span, false );

		final RandomAccessible< Neighborhood< R > > nra =
				shape.neighborhoodsRandomAccessible( Views.extendBorder( rai ) );

		final RandomAccessibleInterval< Neighborhood< R > > nrai = Views.interval( nra, rai );

		final RandomAccessibleInterval< R > averageView =
				Converters.convert( nrai,
						( neighborhood, output ) ->
						{
							setNeighborhoodAverage( neighborhood, output );
						},
						Util.getTypeFromInterval( rai ) );

		return averageView;
	}

	private void setNeighborhoodAverage( Neighborhood< R > neighborhood, R output )
	{
		double sum = 0;

		for ( R value : neighborhood )
			sum += value.getRealDouble();

		output.setReal( sum / neighborhood.size() );
	}

}
