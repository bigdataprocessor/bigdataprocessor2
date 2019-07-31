package de.embl.cba.bdp2.process.splitviewmerge;

import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.logging.Logger;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import java.util.ArrayList;

import static de.embl.cba.bdp2.utils.DimensionOrder.C;

public class SplitViewMerger
{
	private ArrayList< FinalInterval > intervalsXYC;

	public SplitViewMerger()
	{
		intervalsXYC = new ArrayList<>(  );
	}


	public void addIntervalXYC( int minX, int minY, int sizeX, int sizeY, int channel )
	{
		intervalsXYC.add( SplitViewMergingHelpers.asIntervalXYC(
				new long[]{ minX, minY },
				new long[]{ sizeX, sizeY },
				channel ) );
	}

	public < R extends RealType< R > & NativeType< R > >
	Image< R > mergeIntervalsXYC( Image< R > image )
	{
		final RandomAccessibleInterval< R > merge = mergeIntervalsXYC( image.getRai(), intervalsXYC );

		final Image< R > mergeImage = image.newImage( merge );

		return mergeImage;
	}


	public static < R extends RealType< R > & NativeType< R > >
	RandomAccessibleInterval< R > mergeIntervalsXYZ(
			RandomAccessibleInterval< R > raiXYZCT,
			ArrayList< ? extends Interval > intervalsXYZ,
			int channel )
	{
		final ArrayList< RandomAccessibleInterval< R > > crops
				= new ArrayList<>();

		for ( Interval intervalXYZ : intervalsXYZ )
		{
		 	Logger.log( "Split Image Merging Interval [X, Y, Z]: " + intervalXYZ );

			final FinalInterval interval5D = intervalXYZasXYZCT( raiXYZCT, intervalXYZ );

			final IntervalView crop =
					Views.zeroMin(
							Views.interval(
									raiXYZCT,
									interval5D ) );

			crops.add( Views.hyperSlice( crop, C, channel ) );
		}

		final RandomAccessibleInterval< R > merged = Views.stack( crops );

		final IntervalView< R > permute = Views.permute( merged, 3, 4 );

		return permute;
	}


	public static < R extends RealType< R > & NativeType< R > >
	RandomAccessibleInterval< R > mergeIntervalsXYC(
			RandomAccessibleInterval< R > raiXYZCT,
			ArrayList< ? extends Interval > intervalsXYC )
	{
		final ArrayList< RandomAccessibleInterval< R > > crops
				= new ArrayList<>();

		for ( Interval interval : intervalsXYC )
		{
			final FinalInterval intervalXYZCT = intervalXYCasXYZCT( raiXYZCT, interval );

			Logger.log( "Split Image Merging Interval [X, Y, Z, C, T]: " + intervalXYZCT );

			final IntervalView crop =
					Views.zeroMin(
							Views.interval(
									raiXYZCT,
									intervalXYZCT ) );

			// NOTE: below it is always channel 0, because of above Views.zeroMin
			crops.add( Views.hyperSlice( crop, C, 0 ) );
		}

		final RandomAccessibleInterval< R > merged = Views.stack( crops );

		final IntervalView< R > permute = Views.permute( merged, 3, 4 );

		return permute;
	}

	public static < R extends RealType< R > & NativeType< R > >
	FinalInterval intervalXYZasXYZCT( RandomAccessibleInterval< R > raiXYZCT,
									  Interval interval )
	{
		final long[] min = Intervals.minAsLongArray( raiXYZCT );
		final long[] max = Intervals.maxAsLongArray( raiXYZCT );

		for ( int d = 0; d < interval.numDimensions(); d++ )
		{
			min[ d ] = interval.min( d );
			max[ d ] = interval.max( d );
		}

		return new FinalInterval( min, max );
	}

	public static < R extends RealType< R > & NativeType< R > >
	FinalInterval intervalXYCasXYZCT( RandomAccessibleInterval< R > raiXYZCT,
									  Interval interval )
	{
		final long[] min = Intervals.minAsLongArray( raiXYZCT );
		final long[] max = Intervals.maxAsLongArray( raiXYZCT );

		// XY
		for ( int d = 0; d < 2; d++ )
		{
			min[ d ] = interval.min( d );
			max[ d ] = interval.max( d );
		}

		// C
		min[ 3 ] = interval.min( 2 );
		max[ 3 ] = interval.max( 2 );

		return new FinalInterval( min, max );
	}
}
