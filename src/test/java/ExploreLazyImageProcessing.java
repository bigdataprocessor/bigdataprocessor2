import bdv.util.BdvFunctions;
import de.embl.cba.bdp2.neighborhood.RectangleShape2;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.converter.Converters;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.ops.parse.token.Real;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.UnsignedIntType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

import java.util.Random;

public class ExploreLazyImageProcessing
{
	public static  < R extends RealType< R > >
	void main( String[] args )
	{
		final RandomAccessibleInterval< R > rai = getRandomImage();

		BdvFunctions.show( rai, "input" );

		final RandomAccessibleInterval< R > averageView =
				createAverageView( rai, new long[]{5,5,5} );

		BdvFunctions.show( averageView, "average" );
	}


	private static < R extends RealType< R > >
	RandomAccessibleInterval< R >
	createAverageView( RandomAccessibleInterval< R > rai, long[] spans )
	{
		Shape shape = new RectangleShape2( spans, false );

		final RandomAccessible< Neighborhood< R > > nra =
				shape.neighborhoodsRandomAccessible( Views.extendBorder( rai ) );

		final RandomAccessibleInterval< Neighborhood< R > > nrai =
				Views.interval( nra, rai );

		final RandomAccessibleInterval< R > averageView =
				Converters.convert( nrai,
						( neighborhood, output ) ->
						{
							// here one has the neighborhood of the central pixel
							// that means one can in principle do any kind
							// of neighborhood filter.
							computeLocalAverage( neighborhood, output );
						},
						Util.getTypeFromInterval( rai ) );

		return averageView;
	}

	private static < R extends RealType< R > >
	void computeLocalAverage( Neighborhood< R > neighborhood, R output )
	{
		double sum = 0;

		final Cursor< R > cursor = neighborhood.cursor();

		// one can figure out the position of the central pixel
		// neighborhood.localize(  ); // I assume this is the neighborhoods center location?

		while( cursor.hasNext() )
		{
			final R value = cursor.next();
			sum += value.getRealDouble();

			// one can also figure out the position of the other pixels
			// cursor.localize(  ); // This can be used to know the current position
		}

		// ...thus one can implement any kind of local neighborhood filter
		// one just needs to compute where the cursor is relative to the central pixel
		// e.g., median would be trivial, because one does not even need the positions

		// ...I assume this is not very efficient, because the neighborhoods are not
		// reused when going to the next pixel, one has to start the computation from scratch.
		// But for using it in the BDV it seems fast enough...I guess there is also multi-threading
		// during the computations, due to the way Bdv fetches the pixels

		output.setReal( sum / neighborhood.size() );
	}

	private static < R extends RealType< R > >
	RandomAccessibleInterval< R > getRandomImage()
	{
		final RandomAccessibleInterval< IntType > input = ArrayImgs.ints( 300, 300, 300 );

		final Random random = new Random( 10 );
		final Cursor< IntType > cursor = Views.iterable( input ).cursor();
		while ( cursor.hasNext() )
		{
			cursor.next().set( random.nextInt( 65535 )  );
		}

		return (RandomAccessibleInterval) input;
	}
}
