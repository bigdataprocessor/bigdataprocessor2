package benchmark;

import de.embl.cba.bdp2.imglib2.LazyStackView;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.view.StackView;

import java.util.ArrayList;

public class BenchmarkLazyStackView
{
	public static void main( String[] args )
	{
		final int numSlices = 10000;
		final int nx = 100;
		final int ny = 100;
		long nanoTime;
		long duration;
		final ArrayList< Long > oldTimes = new ArrayList<>();
		final ArrayList< Long > newTimes = new ArrayList<>();

		for ( int repetition = 0; repetition < 10; repetition++ )
		{
			// Old way
			nanoTime = System.nanoTime();
			final StackView< ByteType > stackView = new StackView<>( getRandomAccessibleIntervals( numSlices, nx, ny ) );
			final RandomAccess< ByteType > access = stackView.randomAccess();
			for ( int i = 0; i < numSlices; i++ )
				access.setPositionAndGet( 0, 0, i );
			duration = System.nanoTime() - nanoTime;
			System.out.println( "Old DefaultRA [ns]: " + duration );
			oldTimes.add( duration );

			// New way
			nanoTime = System.nanoTime();
			final LazyStackView< ByteType > lazyStackView = new LazyStackView<>( getRandomAccessibleIntervals( numSlices, nx, ny ) );
			final RandomAccess< ByteType > lazyAccess = lazyStackView.randomAccess();
			for ( int i = 0; i < numSlices; i++ )
				lazyAccess.setPositionAndGet( 0, 0, i );
			duration = System.nanoTime() - nanoTime;
			System.out.println( "New DefaultRA [ns]: " + duration );
			newTimes.add( duration );
		}

		System.out.println( "Old average = " + oldTimes.stream().mapToDouble( x -> x ).summaryStatistics().getAverage() );
		System.out.println( "New average = " + newTimes.stream().mapToDouble( x -> x ).summaryStatistics().getAverage() );
	}

	private static ArrayList< RandomAccessibleInterval< ByteType > > getRandomAccessibleIntervals( int numSlices, int nx, int ny )
	{
		final ArrayList< RandomAccessibleInterval< ByteType> > rais = new ArrayList<>();
		for ( int i = 0; i < numSlices; i++ )
		{
			rais.add( ArrayImgs.bytes( nx, ny ) );
		}
		return rais;
	}
}
