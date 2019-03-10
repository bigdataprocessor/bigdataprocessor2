import bdv.util.BdvFunctions;
import de.embl.cba.bdp2.process.LazyDownsampler;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.view.Views;

import java.util.Random;

public class TestLazyDownsampler
{
	public static void main( String[] args )
	{
		final RandomAccessibleInterval< IntType > randomImage = createRandomImage();
		BdvFunctions.show( randomImage, "input" );

		final RandomAccessibleInterval downsampledView =
				new LazyDownsampler( randomImage, new long[]{3,0,0} ).get();

		BdvFunctions.show( downsampledView, "down-sampled" );
	}

	private static RandomAccessibleInterval< IntType > createRandomImage()
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
