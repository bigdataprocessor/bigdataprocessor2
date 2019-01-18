import bdv.util.BdvFunctions;
import net.imglib2.*;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.converter.Converters;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.UnsignedIntType;
import net.imglib2.view.SubsampleIntervalView;
import net.imglib2.view.Views;

import java.util.Random;

public class ExploreLazyDownsampling
{
	public static void main( String[] args )
	{
		final RandomAccessibleInterval< IntType > rai = getRandomImage();

		BdvFunctions.show( rai, "input" );

		final RandomAccessibleInterval< UnsignedIntType > averageView = createAverageView( rai, 3 );

		BdvFunctions.show( averageView, "average" );

		final SubsampleIntervalView< UnsignedIntType > downsampleView = Views.subsample( averageView, 3 );

		BdvFunctions.show( downsampleView, "downsample" );
	}

	private static RandomAccessibleInterval< UnsignedIntType > createAverageView( RandomAccessibleInterval< IntType > rai, int span )
	{
		Shape shape = new RectangleShape( span, false );
		final RandomAccessible< Neighborhood< IntType > > nra =
				shape.neighborhoodsRandomAccessible( Views.extendBorder( rai ) );
		final RandomAccessibleInterval< Neighborhood< IntType > > nrai = Views.interval( nra, rai );

		final RandomAccessibleInterval< UnsignedIntType > averageView =
				Converters.convert( nrai,
					( neighborhood, output ) ->
					{
						setNeighborhoodAverage( neighborhood, output );
					},
				new UnsignedIntType() );

		return averageView;
	}

	private static void setNeighborhoodAverage( Neighborhood< IntType > n, UnsignedIntType o )
	{
		double sum = 0;

		for ( IntType i : n ) sum += i.getInteger();

		o.set( ( int ) sum / n.size() );
	}

	private static RandomAccessibleInterval< IntType > getRandomImage()
	{
		final RandomAccessibleInterval< IntType > input =
				ArrayImgs.ints( 300, 300, 300 );

		final Random random = new Random( 10 );
		final Cursor< IntType > cursor = Views.iterable( input ).cursor();
		while ( cursor.hasNext() )
		{
			cursor.next().set( random.nextInt( 65535 )  );
		}
		return input;
	}
}
