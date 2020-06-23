package de.embl.cba.bdp2.open;

import de.embl.cba.bdp2.open.ui.ChannelChooserDialog;
import de.embl.cba.bdp2.open.ui.OpenLuxendoChannelsCommand;
import de.embl.cba.bdp2.record.MacroRecorder;

import java.util.List;

public class LuxendoInteractiveChannelSubsetter implements ChannelSubsetter
{
	private final String viewingModality;
	private final MacroRecorder< ? > recorder;

	public LuxendoInteractiveChannelSubsetter( String commandFullName, String viewingModality )
	{
		recorder = new MacroRecorder<>( commandFullName, viewingModality );
		this.viewingModality = viewingModality;
	}

	@Override
	public List< String > getChannelSubset( List< String > channels )
	{
		final ChannelChooserDialog dialog = new ChannelChooserDialog( channels );
		channels = dialog.getChannelsViaDialog();
		recorder.addOption( OpenLuxendoChannelsCommand.CHANNELS_PARAMETER, String.join( ",", channels ) );
		recorder.record();
		return channels;
	}
}
