package de.embl.cba.bdp2.process;

import de.embl.cba.bdp2.logging.Logger;
import de.embl.cba.bdp2.utils.DimensionOrder;
import de.embl.cba.bdp2.utils.Utils;
import ij.ImagePlus;
import net.imglib2.*;
import net.imglib2.algorithm.util.Grids;
import net.imglib2.img.AbstractImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;
import net.imglib2.view.ExtendedRandomAccessibleInterval;
import net.imglib2.view.Views;

import java.util.List;

import static de.embl.cba.bdp2.utils.DimensionOrder.*;

public class VolumeExtractions  // TODO: better name?!
{

	public static < R extends RealType< R > & NativeType< R > >
	RandomAccessibleInterval< R > getVolumeView(
			RandomAccessibleInterval< R > image,
			long c,
			long t )
	{
		long start = System.currentTimeMillis();

		long[] minInterval = new long[]{
				image.min( X ),
				image.min( Y ),
				image.min( Z ),
				c,
				t };

		long[] maxInterval = new long[]{
				image.max( X ),
				image.max( Y ),
				image.max( Z ),
				c,
				t };

		RandomAccessibleInterval raiXYZ =
				Views.dropSingletonDimensions(
						Views.interval( image, minInterval, maxInterval ) );

		return raiXYZ;
	}

	public static < R extends RealType< R > & NativeType< R > >
	RandomAccessibleInterval< R > getNonVolatileVolumeCopy(
			RandomAccessibleInterval< R > image,
			long c,
			long t,
			int numThreads )
	{
		long start = System.currentTimeMillis();

		long[] minInterval = new long[]{
				image.min( X ),
				image.min( Y ),
				image.min( Z ),
				c,
				t };

		long[] maxInterval = new long[]{
				image.max( X ),
				image.max( Y ),
				image.max( Z ),
				c,
				t };

		RandomAccessibleInterval raiXYZ =
				Views.dropSingletonDimensions(
						Views.interval( image, minInterval, maxInterval ) );

		raiXYZ = copyVolumeRAI( raiXYZ, numThreads );

		Logger.debug( "Produce processed data cube [ s ]: "
				+ ( System.currentTimeMillis() - start ) / 1000);

		return raiXYZ;
	}

	public static < R extends RealType< R > & NativeType< R > >
	RandomAccessibleInterval< R > getNonVolatileVolumeCopy(
			RandomAccessibleInterval< R > rai,
			FinalInterval volume,
			long c,
			long t,
			int numThreads )
	{
		long start = System.currentTimeMillis();

		long[] minInterval = new long[]{
				volume.min( X ),
				volume.min( Y ),
				volume.min( Z ),
				c,
				t };

		long[] maxInterval = new long[]{
				volume.max( X ),
				volume.max( Y ),
				volume.max( Z ),
				c,
				t };

		// Accommodate cases where the asked-for volume is out-of-bounds
		RandomAccessible< R > extended = Views.extendBorder( rai );

		RandomAccessibleInterval raiXYZ =
				Views.zeroMin(
						Views.dropSingletonDimensions(
								Views.interval( extended, minInterval, maxInterval ) ) );

		raiXYZ = copyVolumeRAI( raiXYZ, numThreads );

		Logger.debug( "Produce processed data cube [ s ]: "
				+ ( System.currentTimeMillis() - start ) / 1000);

		return raiXYZ;
	}

	/**
	 *
	 * Create a copyVolumeRAI, thereby forcing computation of a potential
	 * cascade of views.
	 *
	 * @param volume
	 * @param numThreads
	 * @param <R>
	 * @return
	 */
	private static < R extends RealType< R > & NativeType< R > >
	RandomAccessibleInterval< R > copyVolumeRAI( RandomAccessibleInterval< R > volume,
												 int numThreads )
	{
		final int dimensionX = ( int ) volume.dimension( 0 );
		final int dimensionY = ( int ) volume.dimension( 1 );
		final int dimensionZ = ( int ) volume.dimension( 2 );

		final long numElements =
				AbstractImg.numElements( Intervals.dimensionsAsLongArray( volume ) );

		RandomAccessibleInterval< R > copy;

		R type = getType( volume );

		if ( numElements < Integer.MAX_VALUE - 1 )
		{
			copy = new ArrayImgFactory( type ).create( volume );
		}
		else
		{
			int nz = (int) ( numElements / ( volume.dimension( 0  ) * volume.dimension( 1 ) ) );

			final int[] cellSize = {
					dimensionX,
					dimensionY,
					nz };

			copy = new CellImgFactory( type, cellSize ).create( volume );
		}

		// LoopBuilder.setImages( copy, volume ).forEachPixel( Type::set );

		final int[] blockSize = {
				dimensionX,
				dimensionY,
				( int ) Math.ceil( dimensionZ / numThreads ) };

		final List< Interval > intervals = Grids.collectAllContainedIntervals(
				Intervals.dimensionsAsLongArray( volume ), blockSize );

		intervals.parallelStream().forEach(
				interval -> copy( volume, Views.interval( copy, interval ) ) );

		return copy;
	}

	private static < R extends RealType< R > & NativeType< R > > R getType( RandomAccessibleInterval< R > volume )
	{
		R type = null;
		try
		{
			type = Util.getTypeFromInterval( volume );
		}
		catch ( Exception e )
		{
			System.err.println( e );
		}
		return type;
	}


	private static < T extends Type< T > > void copy(
			final RandomAccessible< T > source,
			final IterableInterval< T > target )
	{
		// create a cursor that automatically localizes itself on every move
		Cursor< T > targetCursor = target.localizingCursor();
		RandomAccess< T > sourceRandomAccess = source.randomAccess();

		// iterate over the input cursor
		while ( targetCursor.hasNext() )
		{
			// move input cursor forward
			targetCursor.fwd();

			// set the output cursor to the position of the input cursor
			sourceRandomAccess.setPosition( targetCursor );

			// set the value of this pixel of the output image, every Type supports T.set( T type )
			targetCursor.get().set( sourceRandomAccess.get() );
		}


	}

}
