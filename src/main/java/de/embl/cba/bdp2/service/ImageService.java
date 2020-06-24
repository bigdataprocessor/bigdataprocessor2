package de.embl.cba.bdp2.service;

import de.embl.cba.bdp2.image.Image;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ImageService
{
	public static Map< String, Image > imageNameToImage =
			Collections.synchronizedMap( new WeakHashMap<>( ) );
}
