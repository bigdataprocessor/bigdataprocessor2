package de.embl.cba.bdp2.track;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.utils.IntervalImageViews;
import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import java.util.ArrayList;
import java.util.Arrays;

public class TrackApplier< R extends RealType< R > & NativeType< R > >
{
	private final Image< R > image;

	public TrackApplier( Image< R > image )
	{
		this.image = image;
	}

	public Image< R > applyTrack( Track track )
	{
		final ArrayList< RandomAccessibleInterval< R > > timePoints = new ArrayList<>();

		RandomAccessibleInterval< R > volumeView = IntervalImageViews.getVolumeView( image.getRai(), 0, 0 );

		Interval union = getUnion( track, volumeView );

		// TODO: interpolate missing time-points
		// make user chose interpolation method
		for (int t = track.tMin(); t < track.tMax(); ++t)
		{
			final ArrayList< RandomAccessibleInterval< R > > channels = new ArrayList<>();
			for ( int c = 0; c < image.numChannels(); c++ )
			{
				volumeView = IntervalImageViews.getVolumeView( image.getRai(), c, t );

				RandomAccessible< R > extendBorder = Views.extendBorder( volumeView );
				RandomAccessible< R > translate = Views.translate( extendBorder, getTranslation( track, t ) );
				final IntervalView< R > intervalView = Views.interval( translate, union );

				channels.add( intervalView );
			}

			timePoints.add( Views.stack( channels ) );
		}

		final RandomAccessibleInterval< R > trackView = Views.stack( timePoints );

		final Image< R > trackViewImage = image.newImage( trackView );
		trackViewImage.setName( track.getTrackName() );
		return trackViewImage;
	}

	private static < R extends RealType< R > & NativeType< R > > Interval getUnion( Track track, RandomAccessibleInterval< R > volume )
	{
		Interval union = null;
		for (int t = track.tMin(); t < track.tMax(); ++t)
		{
			Interval translateInterval = Views.translate( volume, getTranslation( track, t ) );
			if ( union == null )
				union = translateInterval;
			else
				union = Intervals.union( union, translateInterval );
		}
		return union;
	}

	private static long[] getTranslation( Track track, int t )
	{
		final long[] voxelPosition = track.getVoxelPosition( t );
		final long[] shifts = Arrays.stream( voxelPosition ).map( x -> -x ).toArray();
		return shifts;
	}
}
