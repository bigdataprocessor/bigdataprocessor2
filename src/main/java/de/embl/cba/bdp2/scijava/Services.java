package de.embl.cba.bdp2.scijava;

import de.embl.cba.bdp2.log.Logger;
import org.scijava.Context;
import org.scijava.command.CommandService;
import org.scijava.ui.UIService;

public class Services
{
	private static CommandService commandService;
	private static UIService uiService;
	private static Context context;

	public static void setCommandService( CommandService commandService )
	{
		if ( commandService == null ) return;

		Services.commandService = commandService;
	}

	public static void setUiService( UIService uiService )
	{
		if ( uiService == null ) return;

		System.out.println( "Setting SciJava uiService.");
		Services.uiService = uiService;
		if ( uiService.isHeadless() )
		{
			Logger.info( "Detected headless mode." );
		}
	}

	public static void setContext( Context context )
	{
		if ( context == null ) return;

		System.out.println( "Setting SciJava context.");
		Services.context = context;
	}

	public static CommandService getCommandService()
	{
		return commandService;
	}

	public static UIService getUiService()
	{
		return uiService;
	}

	public static Context getContext()
	{
		return context;
	}
}
