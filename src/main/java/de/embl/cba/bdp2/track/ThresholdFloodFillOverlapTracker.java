package de.embl.cba.bdp2.track;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.utils.IntervalImageViews;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import de.embl.cba.bdv.utils.BdvUtils;
import de.embl.cba.bdv.utils.objects3d.ThresholdFloodFill;
import ij.gui.NonBlockingGenericDialog;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.type.NativeType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;

import java.util.ArrayList;

public class ThresholdFloodFillOverlapTracker< R extends RealType< R > & NativeType< R > >
{
	private final Image< R > image;
	private final Settings settings;
	private Track track;
	private int numDimensions;
	private ArrayList< long[] > positions;
	private boolean isRunning;


	public static class Settings
	{
		public double[] initialPositionCalibrated = new double[ 3 ];
		public long[] timeInterval = new long[]{ 0, 1 };
		public long channel = 0;
		public long maxNumObjectElements = 100 * 100 * 100;
		public int numThreads = 1; // TODO: multithreaded?
		public double threshold = 0.0;
		public String trackId;
	}

	public ThresholdFloodFillOverlapTracker( Image< R > image,
											 Settings settings )
	{
		this.image = image;
		this.settings = settings;
		this.numDimensions = settings.initialPositionCalibrated.length;
		track = new Track( settings.trackId );
		track.setVoxelSpacing( image.getVoxelSpacing() );
	}

	public static < R extends RealType< R > & NativeType< R > > void trackObjectDialog(
			BdvImageViewer< R > bdvImageViewer )
	{
		Settings settings = new Settings();

		BdvUtils.getGlobalMouseCoordinates( bdvImageViewer.getBdvHandle() ).localize( settings.initialPositionCalibrated );

		final NonBlockingGenericDialog gd = new NonBlockingGenericDialog( "Tracking settings" );
		gd.addStringField( "Track ID", "Track_000" );
		gd.addNumericField( "Channel ", 0, 0 );
		gd.addNumericField( "First time point ", bdvImageViewer.getBdvHandle()
				.getViewerPanel().getState().getCurrentTimepoint(), 0 );
		gd.addNumericField( "Last time point ",  bdvImageViewer.getImage().getRai().dimension( 4 ) - 1 , 0 );
		gd.addNumericField( "Threshold ", 10 , 0 );
		gd.showDialog();
		if ( gd.wasCanceled() ) return;
		settings.trackId = gd.getNextString();
		settings.channel = (long) gd.getNextNumber();
		settings.timeInterval = new long[]{ (long) gd.getNextNumber(), (long) gd.getNextNumber() };
		settings.threshold = gd.getNextNumber();

		final ThresholdFloodFillOverlapTracker tracker =
				new ThresholdFloodFillOverlapTracker< R >( bdvImageViewer.getImage(), settings );

		new Thread( () -> tracker.track() ).start();

		new TrackDisplayBehaviour( bdvImageViewer.getBdvHandle(), tracker.getTrack() );

		bdvImageViewer.addTrack( tracker.getTrack() );




	}

	public void track()
	{
		isRunning = true;

		track.setPosition( settings.timeInterval[ 0 ], settings.initialPositionCalibrated );

		for ( long t = settings.timeInterval[ 0 ]; t < settings.timeInterval[ 1 ]; t++ )
		{
			final ThresholdFloodFill< R > fill = new ThresholdFloodFill<>(
					IntervalImageViews.getVolumeView( image.getRai(), settings.channel, t ),
					settings.threshold,
					new RectangleShape( 1, false ),
					settings.maxNumObjectElements
			);

			long[] seed = getSeed( t );

//			Logger.log( "Seed: " + Arrays.toString( seed ) );

			fill.run( seed );

			final RandomAccessibleInterval< BitType > mask = fill.getMask();

//			if ( t == 10 )
//				Utils.showVolumeInImageJ1( mask, "mask " + t );

			positions = fill.getPositions();

			final double[] centroid = computeCentroid( positions );

			//System.out.println( t + " -> " + ( t + 1 ) + ": " + Arrays.toString( shift ) );

			//final long[] shiftedPosition = getShiftedPosition( shift, position );

			track.setPosition( t, centroid );

			TrackingUtils.logTrackPosition( track, t );
		}

		isRunning = false;

	}

	/**
	 * TODO: Be more clever here to avoid looping through the new image twice?
	 * Maybe this can be combined with the actual flood fill procedure?
	 * For example, each position that is above the threshold could be the source of a parallel flood fill
	 *
	 * @param t
	 * @return
	 */
	private long[] getSeed( long t )
	{
		if ( t > settings.timeInterval[ 0 ] )
		{
			double max = - Double.MAX_VALUE;
			double value;
			long[] seed = new long[ numDimensions ];

			final RandomAccessibleInterval< R > volume = IntervalImageViews.getVolumeView( image.getRai(), settings.channel, t );

			final RandomAccess< R > access = volume.randomAccess();

			for ( long[] position : positions )
			{
				access.setPosition( position );
				value = access.get().getRealDouble();
				if ( value >= settings.threshold )
				{
					if ( value > max )
					{
						max = value;
						seed = position;
					}
				}
			}

			return seed;
		}
		else
		{
			return track.getLongPosition( t );
		}
	}

	private double[] computeCentroid( ArrayList< long[] > positions )
	{
		final double[] centroid = new double[ numDimensions ];

		for ( long[] position : positions )
			for ( int d = 0; d < numDimensions; d++ )
				centroid[ d ] += position[ d ];

		for ( int d = 0; d < numDimensions; d++ )
			centroid[ d ] /= positions.size();

		return centroid;
	}

	public Track getTrack()
	{
		return track;
	}

	public boolean isFinished()
	{
		return ! isRunning ;
	}



}
