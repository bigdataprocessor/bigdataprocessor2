package de.embl.cba.bdp2.logging;

import ij.IJ;
import ij.gui.GenericDialog;

import java.util.concurrent.atomic.AtomicBoolean;

public class Logger
{
	public static final String NORMAL = "Normal";
	public static final String DEBUG = "Debug";
	public static AtomicBoolean debug = new AtomicBoolean( false );
	private static String level;

	public static void setLevel( String level )
	{
		Logger.level = level;
		if ( level.equals( DEBUG ) ) debug.set( true );
		if ( level.equals( NORMAL ) ) debug.set( false );
	}

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

	public static void showLoggingLevelDialog()
	{
		final GenericDialog gd = new GenericDialog( "Logging" );
		gd.addChoice( "Logging", new String[]{
				NORMAL, DEBUG
		}, NORMAL );

		gd.showDialog();
		if ( gd.wasCanceled() ) return;
		setLevel( gd.getNextChoice() );
	}
}
