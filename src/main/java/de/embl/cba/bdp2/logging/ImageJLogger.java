package de.embl.cba.bdp2.logging;

import ij.IJ;

public abstract class ImageJLogger
{

	public static void info( String text )
	{
		IJ.log( text );
	}

}
