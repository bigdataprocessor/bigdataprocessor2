package de.embl.cba.bdp2.process;

import de.embl.cba.bdp2.Image;
import net.imglib2.Interval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

public class Cropper
{
	public static < T extends RealType< T > & NativeType< T > >
	Image< T > crop( Image< T > image, Interval interval )
	{
		Views.zeroMin( Views.interval( image.getRai(), interval ) );

		return new Image<>(
				Views.zeroMin( Views.interval( image.getRai(), interval ) ),
				image.getName(),
				image.getVoxelSpacing(),
				image.getVoxelUnit()
		);
	}
}
