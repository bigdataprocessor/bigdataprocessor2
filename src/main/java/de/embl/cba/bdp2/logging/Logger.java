package de.embl.cba.bdp2.logging;

import ij.IJ;

import java.util.concurrent.atomic.AtomicBoolean;

public class Logger
{
	public static AtomicBoolean debug = new AtomicBoolean( true );

	public static void log( String msg )
	{
		IJ.log( msg );
	}

	public static void progress( String msg, String progress )
	{
		IJ.log( msg + ": " + progress );
	}

	public static void info( String msg )
	{
		IJ.log( msg );
	}

	public static void warning( String msg )
	{
		IJ.log( "WARNING: " + msg );
	}

	public static void debug( String msg )
	{
		if ( debug.get() )
			IJ.log( "[DEBUG] " + msg );
	}

	public static void error( String msg )
	{
		IJ.error( msg );
	}

	public static boolean isShowDebug()
	{
		return false;
	}

	public static void progress( String message, Object o, long start, int i, int size )
	{
		// TODO
	}
}
