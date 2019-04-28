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
	ArrayList< FinalInterval > asIntervals(
			ArrayList< ? extends RealInterval > realIntervals3D,
			double[] voxelSpacing )
	{
		ArrayList< FinalInterval > voxelIntervals = new ArrayList<>(  );


		for ( RealInterval region : realIntervals3D )
		{

			final int numDimensions = region.numDimensions();

			final long[] min = new long[ numDimensions ];
			final long[] max = new long[ numDimensions ];

			for ( int d = 0; d < numDimensions; d++ )
			{
				min[ d ] = ( long ) ( region.realMin( d ) / voxelSpacing[ d ] );
				max[ d ] = ( long ) ( region.realMax( d ) / voxelSpacing[ d ] );
			}

			voxelIntervals.add( new FinalInterval( min, max ) );
		}
		return voxelIntervals;
	}

	public static < R extends RealType< R > >
	ArrayList< FinalInterval > asIntervals(
			ArrayList< double[] > centres,
			double[] spans,
			double[] voxelSpacing )
	{
		ArrayList< FinalInterval > intervals = new ArrayList<>(  );

		for ( double[] centre : centres )
		{
			final long[] min = new long[ centre.length ];
			final long[] max = new long[ centre.length ];

			for ( int d = 0; d < 2; d++ )
			{
				min[ d ] = ( long ) (
						( centre[ d ] - spans[ d ] / 2.0 )
								/ voxelSpacing[ d ] );


				max[ d ] = ( long ) (
						( centre[ d ] + spans[ d ] / 2.0 )
								/ voxelSpacing[ d ] );
			}

			intervals.add( new FinalInterval( min, max ) );
		}

		return intervals;
	}
}
