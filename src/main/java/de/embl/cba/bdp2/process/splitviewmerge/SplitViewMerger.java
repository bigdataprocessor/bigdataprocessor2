package de.embl.cba.bdp2.process.splitviewmerge;

import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.logging.Logger;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import java.util.ArrayList;

import static de.embl.cba.bdp2.utils.DimensionOrder.C;

public class SplitViewMerger
{

	private long[] upperLeftCornerRegionA;
	private long[] upperLeftCornerRegionB;
	private long[] regionSpan;

	public SplitViewMerger()
	{

	}

	public void setUpperLeftCornerRegionA( long... upperLeftCornerRegionA )
	{
		this.upperLeftCornerRegionA = upperLeftCornerRegionA;
	}

	public void setUpperLeftCornerRegionB( long... upperLeftCornerRegionB )
	{
		this.upperLeftCornerRegionB = upperLeftCornerRegionB;
	}

	public void setRegionSpan( long... regionSpan )
	{
		this.regionSpan = regionSpan;
	}

	public < R extends RealType< R > & NativeType< R > >
	Image< R > mergeRegionsAandB( Image< R > image )
	{

		final ArrayList< long[] > mins = new ArrayList<>();
		mins.add( upperLeftCornerRegionA );
		mins.add( upperLeftCornerRegionB );


		ArrayList< FinalInterval > intervals =
				SplitViewMergingHelpers.asIntervals( mins, regionSpan );

		final RandomAccessibleInterval< R > merge = merge( image.getRai(), intervals );

		final Image< R > mergeImage = new Image<>(
				merge,
				image.getName(),
				image.getVoxelSpacing(),
				image.getVoxelUnit() );

		return mergeImage;
	}


	public static < R extends RealType< R > & NativeType< R > >
	Image< R > merge(
			Image< R > image,
			ArrayList< long[] > mins,
			long[] spans )
	{

		ArrayList< FinalInterval > intervals =
				SplitViewMergingHelpers.asIntervals( mins, spans );

		final RandomAccessibleInterval< R > merge = merge( image.getRai(), intervals );

		final Image< R > mergeImage = new Image<>(
				merge,
				image.getName(),
				image.getVoxelSpacing(),
				image.getVoxelUnit() );

		return mergeImage;
	}


	public static < R extends RealType< R > & NativeType< R > >
	Image< R > merge(
			Image< R > image,
			ArrayList< ? extends Interval > intervals )
	{
		final RandomAccessibleInterval< R > merge = merge( image.getRai(), intervals );

		final Image< R > mergeImage = new Image<>( merge, image.getName(), image.getVoxelSpacing(), image.getVoxelUnit() );

		return mergeImage;
	}



	public static < R extends RealType< R > & NativeType< R > >
	RandomAccessibleInterval< R > merge(
			RandomAccessibleInterval< R > rai,
			ArrayList< ? extends RealInterval > realIntervals,
			double[] voxelSpacing )
	{
		ArrayList< FinalInterval > intervals =
				SplitViewMergingHelpers.asIntervals( realIntervals, voxelSpacing );

		final RandomAccessibleInterval< R > merge = merge( rai, intervals );

		return merge;
	}

	public static < R extends RealType< R > & NativeType< R > >
	RandomAccessibleInterval< R > merge(
			RandomAccessibleInterval< R > raiXYZCT,
			ArrayList< ? extends Interval > intervals )
	{
		final ArrayList< RandomAccessibleInterval< R > > crops
				= new ArrayList<>();

		for ( Interval interval : intervals )
		{
		 	Logger.log( "Split Image Merging Interval [Pixel]: " + interval );

			final FinalInterval interval5D = getInterval5D( raiXYZCT, interval );

			final IntervalView crop =
					Views.zeroMin(
							Views.interval(
									raiXYZCT,
									interval5D ) );

			crops.add( Views.hyperSlice( crop, C, 0 ) );
		}

		final RandomAccessibleInterval< R > merged = Views.stack( crops );

		final IntervalView< R > permute = Views.permute( merged, 3, 4 );

		return permute;
	}

	public static < R extends RealType< R > & NativeType< R > >
	FinalInterval getInterval5D( RandomAccessibleInterval< R > raiXYZCT,
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
}
