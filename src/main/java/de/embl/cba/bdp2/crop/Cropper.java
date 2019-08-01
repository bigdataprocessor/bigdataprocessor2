package de.embl.cba.bdp2.crop;

import de.embl.cba.bdp2.Image;
import net.imglib2.Interval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

public class Cropper
{
	public static < T extends RealType< T > & NativeType< T > >
	Image< T > crop( Image< T > image, Interval interval )
	{
		final IntervalView< T > crop =
				Views.zeroMin( Views.interval( image.getRai(), interval ) );

		return image.newImage( crop );
	}
}
