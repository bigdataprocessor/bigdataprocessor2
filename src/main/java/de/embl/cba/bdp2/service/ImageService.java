package de.embl.cba.bdp2.service;

import de.embl.cba.bdp2.image.Image;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ImageService
{
	public static Map< String, Image > nameToImage =
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
