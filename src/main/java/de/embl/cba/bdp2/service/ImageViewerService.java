package de.embl.cba.bdp2.service;

import de.embl.cba.bdp2.BigDataProcessor2UI;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.viewer.ImageViewer;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public abstract class ImageViewerService
{
	public static Map< String, ImageViewer > imageNameToBdvImageViewer = Collections.synchronizedMap( new WeakHashMap<>( ) );

	private static ImageViewer focusedViewer;

	public static synchronized void setFocusedViewer( ImageViewer viewer )
	{
		focusedViewer = viewer;
		if ( viewer != null )
		{
			//Logger.log( "Active image: " + viewer.getImage().getName() );
			BigDataProcessor2UI.setImageInformation( viewer.getImage() );
		}
		else
		{
			BigDataProcessor2UI.setImageInformation( null );
		}
	}

	public static synchronized ImageViewer getActiveViewer()
	{
		return focusedViewer;
	}
}
