package de.embl.cba.bdp2.log;

import ij.IJ;
import ij.gui.GenericDialog;

public class Logger
{
	public enum Level
	{
		Normal,
		Debug,
		Benchmark
	}

	private static Level level = Level.Normal;

	public static Level getLevel()
	{
		return level;
	}

	public static synchronized void setLevel( Level level )
	{
		Logger.level = level;
		info( "Setting logging level to: " + level );
	}

	public static void log( String msg )
	{
		IJ.log( msg );
	}

	public static void progress( String msg, String progress )
	{
		progress = msg + ": " + progress;

		if ( IJ.getLog() != null )
		{
			String[] logs = IJ.getLog().split( "\n" );
			if ( logs.length > 1 )
			{
				if ( logs[ logs.length - 1 ].contains( msg ) )
				{
					progress = "\\Update:" + progress;
				}
			}
		}

		IJ.log( progress );
		System.out.println( progress );
	}

	public static void info( String msg )
	{
		IJ.log( msg );
	}

	public static void benchmark( String msg )
	{
		if ( level.equals( Level.Benchmark ) )
			IJ.log( "[BENCHMARK] " + msg );
	}

	public static void warn( String msg )
	{
		IJ.log( "[WARN] " + msg );
	}

	public static void debug( String msg )
	{
		if ( level.equals( Level.Debug ) )
			IJ.log( "[DEBUG] " + msg );
	}

	public static void error( String msg )
	{
		IJ.error( msg );
	}

	public static void progress( String message, Object o, long start, int i, int size )
	{
		// TODO
	}

	/**
	 * Use LoggingLevelCommand instead, because this is automatically macro recorded.
	 */
	@Deprecated
	public synchronized static void showLoggingLevelDialog()
	{
		final GenericDialog gd = new GenericDialog( "Logging" );
		gd.addChoice( "Level", new String[]{
				Level.Debug.toString(), Level.Normal.toString(), Level.Benchmark.toString()
		}, Logger.getLevel().toString() );
		gd.addMessage( "Benchmark mode may significantly slow down the application!" );

		gd.showDialog();
		if ( gd.wasCanceled() ) return;
		setLevel( Level.valueOf( gd.getNextChoice() ) );
	}
}
