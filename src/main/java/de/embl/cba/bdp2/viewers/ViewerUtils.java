package de.embl.cba.bdp2.viewers;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public abstract class ViewerUtils
{
	public static final String BIG_DATA_VIEWER = "BigDataViewer";
	public static final String IJ1_VIEWER = "ImageJ Hyperstack Viewer";

	public static < R extends RealType< R > & NativeType< R > >
	ImageViewer getImageViewer( String imageViewerChoice )
	{
		switch ( imageViewerChoice )
		{
			case BIG_DATA_VIEWER:
				return new BdvImageViewer< R >();
			case IJ1_VIEWER:
				//imageViewer = new IJ1ImageViewer();
				return null;
			default:
				return null;
		}
	}
}
