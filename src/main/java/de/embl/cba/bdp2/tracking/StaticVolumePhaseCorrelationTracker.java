package de.embl.cba.bdp2.tracking;

import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.process.Duplicator;
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

	public static class Settings
	{
		public long[] volumeDimensions; // voxels
		public long[] centerStartingPosition;
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
		track = new Track( id );
		track.setVoxelSpacing( image.getVoxelSpacing() );
	}

	public void track()
	{
		track.setPosition( settings.timeInterval[ 0 ], settings.centerStartingPosition );

		for ( long t = settings.timeInterval[ 0 ]; t < settings.timeInterval[ 1 ]; t++ )
		{

			// TODO: make method to crop subvolume from image: Duplicator.copyVolumeFromRai

			final RandomAccessibleInterval< R > rai0 =
					Duplicator.copyVolumeFromRai( image.getRai(), settings.channel, t, settings.numThreads );

			final RandomAccessibleInterval< R > rai1 =
					Duplicator.copyVolumeFromRai( image.getRai(), settings.channel, t + 1, settings.numThreads );

			final double[] shift = PhaseCorrelationTranslationComputer.computeShift(
					rai0,
					rai1,
					Executors.newFixedThreadPool( settings.numThreads ) );

			System.out.println( t + " -> " + ( t + 1 ) + ": " + Arrays.toString( shift ) );

			final long[] shiftedPosition = getShiftedPosition( shift, track.getPosition( t ) );

			track.setPosition( t + 1, shiftedPosition );
		}

	}

	private long[] getShiftedPosition( double[] shift, long[] position )
	{
		final long[] shiftedPosition = new long[ position.length];

		for ( int d = 0; d < position.length; d++ )
			shiftedPosition[ d ] = position[ d ] + (long) shift[ d ];

		return shiftedPosition;
	}

	public Track getTrack()
	{
		return track;
	}

}
