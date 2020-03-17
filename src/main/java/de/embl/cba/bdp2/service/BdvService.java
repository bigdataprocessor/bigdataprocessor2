package de.embl.cba.bdp2.service;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.viewers.BdvImageViewer;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public abstract class BdvService
{
	public static Map< String, BdvImageViewer > imageNameToBdv =
			Collections.synchronizedMap( new WeakHashMap<>( ) );

//	private static final ImageService imageService = new ImageService();
//
//	private ImageService(){}
//
//	public static ImageService getInstance()
//	{
//		return imageService;
//	}
//
//	public static synchronized void addImage( Image image )
//	{
//		nameToImage.put( image.getName(), image );
//	}
//
//	public static synchronized void addImage( Image image )
//	{
//		nameToImage.put( image.getName(), image );
//	}

}
