package de.embl.cba.bdp2.process.splitviewmerge;

import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;

import java.util.ArrayList;

public class SplitViewMergingHelpers
{

	public static < R extends RealType< R > >
	ArrayList< FinalInterval > getVoxelntervals5D(
			RandomAccessibleInterval< R > rai5D,
			ArrayList< ? extends RealInterval > calibratedIntervals3D,
			double[] voxelSpacing )
	{
		ArrayList< FinalInterval > voxelIntervals = new ArrayList<>(  );

		for ( RealInterval region : calibratedIntervals3D )
		{
			final long[] min = Intervals.minAsLongArray( rai5D );
			final long[] max = Intervals.maxAsLongArray( rai5D );

			for ( int d = 0; d < 3; d++ )
			{
				min[ d ] = ( long ) ( region.realMin( d ) / voxelSpacing[ d ] );
				max[ d ] = ( long ) ( region.realMax( d ) / voxelSpacing[ d ] );
			}

			voxelIntervals.add( new FinalInterval( min, max ) );
		}
		return voxelIntervals;
	}

	public static < R extends RealType< R > >
	ArrayList< FinalInterval > getVoxelntervals5D(
			RandomAccessibleInterval< R > rai5D,
			ArrayList< double[] > calibratedCentres2D,
			double[] calibratedSpan2D,
			double[] voxelSpacing )
	{
		ArrayList< FinalInterval > voxelIntervals = new ArrayList<>(  );

		for ( double[] centre : calibratedCentres2D )
		{
			final long[] min = Intervals.minAsLongArray( rai5D );
			final long[] max = Intervals.maxAsLongArray( rai5D );

			for ( int d = 0; d < 2; d++ )
			{
				min[ d ] = ( long ) (
						( centre[ d ] - calibratedSpan2D[ d ] / 2.0 )
								/ voxelSpacing[ d ] );


				max[ d ] = ( long ) (
						( centre[ d ] + calibratedSpan2D[ d ] / 2.0 )
								/ voxelSpacing[ d ] );
			}

			voxelIntervals.add( new FinalInterval( min, max ) );
		}

		return voxelIntervals;
	}
}
