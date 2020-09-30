package de.embl.cba.bdp2.scijava;

import de.embl.cba.bdp2.log.Logger;
import org.scijava.Context;
import org.scijava.command.CommandService;
import org.scijava.ui.UIService;

public class Services
{
	public static CommandService commandService;
	public static UIService uiService;
	public static Context context;

	public static void setCommandService( CommandService commandService )
	{
		Services.commandService = commandService;
	}

	public static void setUiService( UIService uiService )
	{
		System.out.println( "Setting SciJava uiService.");
		Services.uiService = uiService;
		if ( uiService.isHeadless() )
		{
			Logger.info( "Detected headless mode." );
			Logger.setLevel( Logger.Level.Debug );
		}
	}

	public static void setContext( Context context )
	{
		System.out.println( "Setting SciJava context.");
		Services.context = context;
	}
}
