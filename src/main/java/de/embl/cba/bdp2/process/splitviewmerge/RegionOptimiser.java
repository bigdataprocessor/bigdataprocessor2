package de.embl.cba.bdp2.process.splitviewmerge;

import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.logging.Logger;
import de.embl.cba.bdp2.tracking.Trackers;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Executors;

import static de.embl.cba.bdp2.process.splitviewmerge.SplitImageMerger.getInterval5D;
import static de.embl.cba.bdp2.utils.DimensionOrder.*;

public class RegionOptimiser
{

	public static < R extends RealType< R > & NativeType< R > >
	ArrayList< double[] > optimiseCentres2D(
			Image< R > image,
			ArrayList< double[] > centres,
			double[] spans )
	{
		final double[] voxelSpacing = image.getVoxelSpacing();

		ArrayList< FinalInterval > intervals =
				SplitViewMergingHelpers.asIntervals(
						centres, spans, voxelSpacing );

		final double[] shift = optimiseCentres2D( image.getRai(), intervals );

		Logger.info( "Region Centre Optimiser: Shift [Voxels]: "
				+ shift[ 0 ] + ", "
				+ shift[ 1 ] );
		Logger.info( "Region Centre Optimiser: Shift ["+ image.getVoxelUnit()+ "]: "
				+ shift[ 0 ] * voxelSpacing[ 0 ] + ", "
				+ shift[ 1 ] * voxelSpacing[ 1 ]);

		final ArrayList< double[] > newCentres = new ArrayList<>();
		newCentres.add( centres.get( 0 ) );

		final double[] newCentre = new double[ 2 ];
		final double[] oldCentre = centres.get( 1 );
		for ( int d = 0; d < 2; d++ )
			newCentre[ d ] = oldCentre[ d ] - shift[ d ] * voxelSpacing[ d ];

		newCentres.add( newCentre );

		return newCentres;
	}


	public static < R extends RealType< R > & NativeType< R > >
	ArrayList< long[] > optimiseCentres2D(
			Image< R > image,
			ArrayList< long[] > centres,
			long[] spans )
	{
		ArrayList< FinalInterval > intervals =
				SplitViewMergingHelpers.asIntervals(
						centres, spans );

		final double[] shift = optimiseCentres2D( image.getRai(), intervals );

		Logger.info( "Region Centre Optimiser: Shift [Voxels]: "
				+ shift[ 0 ] + ", "
				+ shift[ 1 ] );

		final ArrayList< long[] > newCentres = new ArrayList<>();
		newCentres.add( centres.get( 0 ) );

		final long[] newCentre = new long[ 2 ];
		final long[] oldCentre = centres.get( 1 );
		for ( int d = 0; d < 2; d++ )
			newCentre[ d ] = oldCentre[ d ] - (long) shift[ d ];

		newCentres.add( newCentre );

		for ( long[] centre : newCentres )
			Logger.info( "Region Centre Optimiser: Centre " + Arrays.toString( centre ) );

		return newCentres;
	}


	private static < R extends RealType< R > & NativeType< R > >
	double[] optimiseCentres2D(
			RandomAccessibleInterval< R > rai5D,
			ArrayList< FinalInterval > intervals )
	{
		final ArrayList< RandomAccessibleInterval< R > > planes
				= new ArrayList<>();

		for ( FinalInterval interval : intervals )
		{
			final FinalInterval interval5D = getInterval5D( rai5D, interval );

			final IntervalView< R > crop =
					Views.zeroMin(
							Views.interval(
									rai5D,
									interval5D ) );

			final IntervalView< R > time = Views.hyperSlice( crop, T, 0 );
			final IntervalView< R > channel = Views.hyperSlice( time, C, 0 );
			final IntervalView< R > plane =
					Views.hyperSlice(
							channel,
							Z,
							channel.dimension( Z ) / 2 );

			planes.add( plane );
		}

		final double[] shift = Trackers.getPhaseCorrelationShift(
				planes.get( 0 ), planes.get( 1 ),
				Executors.newFixedThreadPool( 2 ) );

		return shift;
	}
}
