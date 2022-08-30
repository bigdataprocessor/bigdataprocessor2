/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2022 EMBL
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package de.embl.cba.bdp2.log;

import ij.IJ;
import ij.gui.GenericDialog;
import net.imagej.patcher.LegacyInjector;

public class Logger
{
	private static boolean useSystemOut;

	static { LegacyInjector.preinit(); }

	public static void useSystemOut( boolean b )
	{
		useSystemOut = b;
	}

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
		print( msg, "" );
	}

	public static void progress( String msg, String progress )
	{
		progress = msg + ": " + progress;

		if ( ! useSystemOut && IJ.getLog() != null )
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

		print( progress, "" );
	}

	public static void info( String msg )
	{
		print( msg, "");
	}

	public static void benchmark( String msg )
	{
		if ( level.equals( Level.Benchmark ) )
		{
			print( msg, "[BENCHMARK] " );
		}
	}

	private static void print( String msg, String preamble )
	{
		if ( useSystemOut )
			System.out.print( preamble + msg + "\n" );
		else
			IJ.log( preamble + msg );
	}

	public static void warn( String msg )
	{
		print( msg, "[WARN] " );
	}

	public static void debug( String msg )
	{
		if ( level.equals( Level.Debug ) )
			print( msg, "[DEBUG] " );
	}

	public static void error( String msg )
	{
		IJ.error( msg );
	}

	public static void progress( String message, Object o, long start, int i, int size )
	{
		// TODO
	}

	/*
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
