package de.embl.cba.bdp2.tracking;

import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.process.VolumeExtractions;
import de.embl.cba.bdp2.ui.BigDataProcessor2;
import de.embl.cba.bdp2.ui.ShearingSettings;
import de.embl.cba.bdp2.utils.DimensionOrder;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import java.util.ArrayList;
import java.util.List;

public class TrackViews< R extends RealType< R > & NativeType< R > >
{
	public static < R extends RealType< R > & NativeType< R > >
	Image< R > applyTrack( Image< R > image, Track track )
	{
		final ArrayList< RandomAccessibleInterval< R > > timePoints = new ArrayList<>();

		for (long t = track.tMin(); t < track.tMax(); ++t)
		{
			final ArrayList< RandomAccessibleInterval< R > > channels = new ArrayList<>();
			for ( int c = 0; c < image.numChannels(); c++ )
			{
				RandomAccessibleInterval< R > volumeView = VolumeExtractions.getVolumeView( image.getRai(), c, t );
				// TODO: probably need to crop?!
				channels.add( Views.translate( volumeView, track.getLongPosition( t ) ) );
			}
			timePoints.add( Views.stack( channels ) );
		}

		final RandomAccessibleInterval< R > trackView = Views.stack( timePoints );

		return image.newImage( trackView );
	}

}
