package de.embl.cba.bdp2.service;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.BigDataProcessor2UI;
import de.embl.cba.bdp2.viewers.BdvImageViewer;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public abstract class BdvService
{
	public static Map< String, BdvImageViewer > imageNameToBdvImageViewer =
			Collections.synchronizedMap( new WeakHashMap<>( ) );

	private static BdvImageViewer focusedViewer;

	public static synchronized void setFocusedViewer( BdvImageViewer viewer )
	{
		focusedViewer = viewer;
		if ( viewer != null )
			BigDataProcessor2UI.setImageInformation( viewer.getImage() );
		else
			BigDataProcessor2UI.setImageInformation( null );
	}

	public static synchronized BdvImageViewer getActiveViewer()
	{
		return focusedViewer;
	}
}
