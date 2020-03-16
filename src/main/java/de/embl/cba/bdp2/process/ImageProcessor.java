package de.embl.cba.bdp2.process;

import de.embl.cba.bdp2.image.Image;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public interface ImageProcessor < R extends RealType< R > & NativeType< R > >
{
	Image< R > process( Image< R > image );
}
