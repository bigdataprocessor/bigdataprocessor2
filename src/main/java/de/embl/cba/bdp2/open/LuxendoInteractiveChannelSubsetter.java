package de.embl.cba.bdp2.open;

import de.embl.cba.bdp2.open.ui.AbstractOpenCommand;
import de.embl.cba.bdp2.open.ui.ChannelChooserDialog;
import de.embl.cba.bdp2.open.ui.OpenLuxendoChannelsCommand;
import de.embl.cba.bdp2.open.ui.OpenLuxendoCommand;
import de.embl.cba.bdp2.macro.MacroRecorder;
import ij.plugin.frame.Recorder;

import java.awt.*;
import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

public class LuxendoInteractiveChannelSubsetter implements ChannelSubsetter
{
	private final String viewingModality;
	private final MacroRecorder< ? > recorder;

	public LuxendoInteractiveChannelSubsetter(  File directory, String viewingModality, boolean enableArbitraryPlaneSlicing, int stackIndex )
	{
		recorder = new MacroRecorder<>( OpenLuxendoChannelsCommand.COMMAND_FULL_NAME, viewingModality );
		recorder.addOption( AbstractOpenCommand.DIRECTORY_PARAMETER, directory.getAbsolutePath() );
		recorder.addOption( AbstractOpenCommand.ARBITRARY_PLANE_SLICING_PARAMETER, enableArbitraryPlaneSlicing );
		recorder.addOption( OpenLuxendoCommand.STACK_INDEX_PARAMETER, stackIndex );

		this.viewingModality = viewingModality;
	}

	private void removeOpenLuxendoCommandFromRecorder()
	{
		try
		{
			Recorder recorder = Recorder.getInstance();
			if ( recorder == null ) return;
			Field f = recorder.getClass().getDeclaredField("textArea"); //NoSuchFieldException
			f.setAccessible(true);
			TextArea textArea = (TextArea) f.get(recorder); //IllegalAccessException
			String text = textArea.getText();
			int start = text.indexOf( OpenLuxendoCommand.COMMAND_FULL_NAME ) - 5;
			int end = text.length() - 1;
			textArea.replaceRange("", start, end );
		}
		catch ( Exception e )
		{
			//e.printStackTrace();
		}
	}

	@Override
	public List< String > getChannelSubset( List< String > channels )
	{
		final ChannelChooserDialog dialog = new ChannelChooserDialog( channels );
		channels = dialog.getChannelsViaDialog();
		recorder.addOption( OpenLuxendoChannelsCommand.CHANNELS_PARAMETER, String.join( ",", channels ) );
		removeOpenLuxendoCommandFromRecorder();
		recorder.record();
		return channels;
	}
}
