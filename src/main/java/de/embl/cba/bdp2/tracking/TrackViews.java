package de.embl.cba.bdp2.tracking;

import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.process.VolumeExtractions;
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

public class TrackViews< R extends RealType< R > & NativeType< R > >
{
	public static < R extends RealType< R > & NativeType< R > >
	Image< R > applyTrack( Image< R > image, Track track )
	{
		final ArrayList< RandomAccessibleInterval< R > > timePoints = new ArrayList<>();

		RandomAccessibleInterval< R > volumeView
				= VolumeExtractions.getVolumeView( image.getRai(), 0, 0 );

		Interval union = getUnion( track, volumeView );

		for (long t = track.tMin(); t < track.tMax(); ++t)
		{
			final ArrayList< RandomAccessibleInterval< R > > channels = new ArrayList<>();
			for ( int c = 0; c < image.numChannels(); c++ )
			{
				volumeView
						= VolumeExtractions.getVolumeView( image.getRai(), c, t );

				RandomAccessible< R > extendBorder
						= Views.extendBorder( volumeView );
				RandomAccessible< R > translate
						= Views.translate( extendBorder,
						getTranslation( track, t ) );
				final IntervalView< R > intervalView = Views.interval( translate, union );

				channels.add( intervalView );
			}

			timePoints.add( Views.stack( channels ) );
		}

		final RandomAccessibleInterval< R > trackView = Views.stack( timePoints );

		final Image< R > trackViewImage = image.newImage( trackView );
		trackViewImage.setName( track.getId() );
		return trackViewImage;
	}

	private static < R extends RealType< R > & NativeType< R > > Interval
	getUnion( Track track, RandomAccessibleInterval< R > volumeView )
	{
		Interval union = null;
		for (long t = track.tMin(); t < track.tMax(); ++t)
		{
			Interval translateInterval = Views.translate( volumeView,
					getTranslation( track, t ) );
			if ( union == null )
				union = translateInterval;
			else
				union = Intervals.union( union, translateInterval );
		}
		return union;
	}

	private static long[] getTranslation( Track track, long t )
	{
		return Arrays.stream( track.getLongPosition( t ) ).map( x -> -x ).toArray();
	}

}
