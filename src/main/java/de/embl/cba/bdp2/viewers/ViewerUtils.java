package de.embl.cba.bdp2.viewers;

public abstract class ViewerUtils
{
	public static final String BIG_DATA_VIEWER = "BigDataViewer";
	public static final String IJ1_VIEWER = "ImageJ Hyperstack Viewer";

	public static ImageViewer getImageViewer( String imageViewerChoice )
	{
		ImageViewer imageViewer = null;
		switch ( imageViewerChoice )
		{
			case BIG_DATA_VIEWER:
				imageViewer = new BdvImageViewer();
				break;
			case IJ1_VIEWER:
				//imageViewer = new IJ1ImageViewer();
				break;
			default:
				imageViewer = new BdvImageViewer();
		}
		return imageViewer;
	}
}
