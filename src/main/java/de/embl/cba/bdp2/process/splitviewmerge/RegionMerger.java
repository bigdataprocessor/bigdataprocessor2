package de.embl.cba.bdp2.process.splitviewmerge;

import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import java.util.ArrayList;

import static de.embl.cba.bdp2.utils.DimensionOrder.C;

public class RegionMerger
{
	public static < R extends RealType< R > >
	RandomAccessibleInterval< R > merge(
			RandomAccessibleInterval< R > rai5D,
			ArrayList< double[] > calibratedCentres2D,
			double[] calibratedSpan2D,
			double[] voxelSpacing )
	{
		ArrayList< FinalInterval > voxelIntervals =
				SplitViewMergingHelpers.getVoxelntervals5D(
						rai5D, calibratedCentres2D, calibratedSpan2D, voxelSpacing );

		final RandomAccessibleInterval< R > merge = merge( rai5D, voxelIntervals );

		return merge;
	}

	public static < R extends RealType< R > >
	RandomAccessibleInterval< R > merge(
			RandomAccessibleInterval< R > rai5D,
			ArrayList< ? extends RealInterval > calibratedIntervals3D,
			double[] voxelSpacing )
	{

		ArrayList< FinalInterval > voxelIntervals =
				SplitViewMergingHelpers.getVoxelntervals5D(
						rai5D, calibratedIntervals3D, voxelSpacing );

		final RandomAccessibleInterval< R > merge = merge( rai5D, voxelIntervals );

		return merge;
	}

	public static < R extends RealType< R > >
	RandomAccessibleInterval< R > merge(
			RandomAccessibleInterval< R > rai5D,
			ArrayList< FinalInterval > voxelIntervals5D )
	{
		final ArrayList< RandomAccessibleInterval< R > > crops
				= new ArrayList<>();

		for ( FinalInterval interval : voxelIntervals5D )
		{
			final IntervalView crop =
					Views.zeroMin(
							Views.interval(
									rai5D,
									interval ) );

			crops.add( Views.hyperSlice( crop, C, 0 ) );
		}

		final RandomAccessibleInterval< R > multiChannel5D =
				Views.stack( crops );

		return Views.permute( multiChannel5D, 3, 4 );
	}
}
