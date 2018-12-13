package de.embl.cba.bigDataToolViewerIL2.viewers;

public abstract class ViewerUtils
{
	public static final String BIG_DATA_VIEWER = "BigDataViewer";
	public static final String IMAGE_HYPERSTACK_VIEWER = "ImageJ Hyperstack Viewer";

	public static ImageViewer getImageViewer( String imageViewerChoice )
	{
		ImageViewer imageViewer;
		switch ( imageViewerChoice )
		{
			case BIG_DATA_VIEWER:
				imageViewer = new BdvImageViewer();
				break;
			case IMAGE_HYPERSTACK_VIEWER:
				imageViewer = new ImageJ1Viewer();
				break;
			default:
				imageViewer = new BdvImageViewer();
		}
		return imageViewer;
	}
}
