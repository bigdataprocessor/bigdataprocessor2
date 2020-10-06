package de.embl.cba.bdp2.service;

import de.embl.cba.bdp2.BigDataProcessor2UserInterface;
import de.embl.cba.bdp2.viewers.ImageViewer;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public abstract class BdvService
{
	public static Map< String, ImageViewer > imageNameToBdvImageViewer =
			Collections.synchronizedMap( new WeakHashMap<>( ) );

	private static ImageViewer focusedViewer;

	public static synchronized void setFocusedViewer( ImageViewer viewer )
	{
		focusedViewer = viewer;
		if ( viewer != null )
			BigDataProcessor2UserInterface.setImageInformation( viewer.getImage() );
		else
			BigDataProcessor2UserInterface.setImageInformation( null );
	}

	public static synchronized ImageViewer getActiveViewer()
	{
		return focusedViewer;
	}
}
