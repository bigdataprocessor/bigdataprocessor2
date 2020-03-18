package de.embl.cba.bdp2.track;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.process.IntervalImageViews;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import java.util.Arrays;
import java.util.concurrent.Executors;

public class StaticVolumePhaseCorrelationTracker < R extends RealType< R > & NativeType< R > >
{
	private final Image< R > image;
	private final Settings settings;
	private final String id;
	private Track track;
	private int numDimensions;

	public static class Settings
	{
		public long[] volumeDimensions; // voxels
		public double[] initialPosition;
		public long[] timeInterval = new long[]{ 0, 1 };
		public long channel = 0;
		public int numThreads = 1;
	}

	public StaticVolumePhaseCorrelationTracker( Image< R > image,
												Settings settings,
												String id )
	{
		this.image = image;
		this.settings = settings;
		this.id = id;
		this.numDimensions = settings.initialPosition.length;
		track = new Track( id );
		track.setVoxelSpacing( image.getVoxelSpacing() );
	}

	public void track()
	{
		track.setPosition( settings.timeInterval[ 0 ], settings.initialPosition );

		for ( long t = settings.timeInterval[ 0 ]; t < settings.timeInterval[ 1 ]; t++ )
		{
			final long[] position = track.getLongPosition( t );

			Logger.log( "Track: " + track.getId() +
					"; t = " + t +
					"; pos = " + Arrays.toString( track.getCalibratedPosition( t ) ) ) ;

			System.out.println( "Position [pixels]: " + Arrays.toString( position ) );
			System.out.println( "Position [calibrated]: " + Arrays.toString( track.getCalibratedPosition( t ) ) );

			final FinalInterval volume = getVolume( position );

			final RandomAccessibleInterval< R > rai0 =
					IntervalImageViews.getNonVolatileVolumeCopy( image.getRai(), volume, settings.channel, t, settings.numThreads );

//			 Utils.showVolumeInImageJ1( rai0, "Time-point " + t );

			final RandomAccessibleInterval< R > rai1 =
					IntervalImageViews.getNonVolatileVolumeCopy( image.getRai(), volume, settings.channel, t + 1, settings.numThreads );

//			Utils.showVolumeInImageJ1( rai1, "Time-point " + ( t + 1 ) );

			final double[] shift = PhaseCorrelationTranslationComputer.computeShift(
					rai1,
					rai0,
					Executors.newFixedThreadPool( settings.numThreads ) );

			System.out.println( t + " -> " + ( t + 1 ) + ": " + Arrays.toString( shift ) );

			final double[] shiftedPosition = getShiftedPosition( shift, position );

			track.setPosition( t + 1, shiftedPosition );
		}

	}

	private FinalInterval getVolume( long[] position )
	{
		long[] min = new long[ numDimensions ];
		long[] max = new long[ numDimensions ];
		for ( int d = 0; d < numDimensions; d++ )
		{
			min[ d ] = position[ d ] - settings.volumeDimensions[ d ] / 2;
			max[ d ] = position[ d ] + settings.volumeDimensions[ d ] / 2;
		}

		return new FinalInterval( min, max );
	}

	private double[] getShiftedPosition( double[] shift, long[] position )
	{
		final double[] shiftedPosition = new double[ position.length];

		for ( int d = 0; d < position.length; d++ )
			shiftedPosition[ d ] = position[ d ] + (long) shift[ d ];

		return shiftedPosition;
	}

	public Track getTrack()
	{
		return track;
	}

}
