package de.embl.cba.bdp2.process;

import de.embl.cba.bdp2.viewers.ImageViewer;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public interface ImageProcessingDialog< R extends RealType< R > & NativeType< R > >
{
	void showDialog( ImageViewer< R > imageViewer );
}
