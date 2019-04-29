package de.embl.cba.bdp2.process.splitviewmerge;

import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.logging.Logger;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import java.util.ArrayList;

import static de.embl.cba.bdp2.utils.DimensionOrder.C;

public class SplitImageMerger
{

	public static < R extends RealType< R > & NativeType< R > >
	Image< R > merge(
			Image< R > image,
			ArrayList< double[] > centres,
			double[] spans )
	{

		ArrayList< FinalInterval > intervals =
				SplitViewMergingHelpers.asIntervals(
						centres, spans, image.getVoxelSpacing() );

		final RandomAccessibleInterval< R > merge = merge( image.getRai(), intervals );

		final Image< R > mergeImage = new Image<>( merge, image.getName(), image.getVoxelSpacing(), image.getVoxelUnit() );

		return mergeImage;
	}

	public static < R extends RealType< R > & NativeType< R > >
	Image< R > merge(
			Image< R > image,
			ArrayList< long[] > centres,
			long[] spans )
	{

		ArrayList< FinalInterval > intervals =
				SplitViewMergingHelpers.asIntervals( centres, spans );

		final RandomAccessibleInterval< R > merge = merge( image.getRai(), intervals );

		final Image< R > mergeImage = new Image<>( merge, image.getName(), image.getVoxelSpacing(), image.getVoxelUnit() );

		return mergeImage;
	}


	public static < R extends RealType< R > & NativeType< R > >
	Image< R > merge(
			Image< R > image,
			ArrayList< FinalInterval > intervals )
	{
		final RandomAccessibleInterval< R > merge = merge( image.getRai(), intervals );

		final Image< R > mergeImage = ( Image< R > ) new Image<>( merge, image.getName(), image.getVoxelSpacing(), image.getVoxelUnit() );

		return mergeImage;
	}


	public static < R extends RealType< R > & NativeType< R > >
	RandomAccessibleInterval< R > merge(
			RandomAccessibleInterval< R > rai,
			ArrayList< double[] > centres,
			double[] spans,
			double[] voxelSpacing )
	{

		ArrayList< FinalInterval > intervals =
				SplitViewMergingHelpers.asIntervals(
						centres, spans, voxelSpacing );

		final RandomAccessibleInterval< R > merge = merge( rai, intervals );

		return merge;
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
			ArrayList< FinalInterval > intervals )
	{
		final ArrayList< RandomAccessibleInterval< R > > crops
				= new ArrayList<>();

		for ( FinalInterval interval : intervals )
		{
		 	Logger.log( "Split Image Merging Interval [Voxels]: " + interval );

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
	FinalInterval getInterval5D( RandomAccessibleInterval< R > raiXYZCT, FinalInterval interval )
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
