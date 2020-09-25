package de.embl.cba.bdp2.scijava;

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
	}

	public static void setContext( Context context )
	{
		System.out.println( "Setting SciJava context.");
		Services.context = context;
	}
}
