package de.embl.cba.bdp2.open;

import de.embl.cba.bdp2.open.ui.AbstractOpenCommand;
import de.embl.cba.bdp2.open.ui.ChannelChooserDialog;
import de.embl.cba.bdp2.open.ui.OpenLuxendoChannelsCommand;
import de.embl.cba.bdp2.open.ui.OpenLuxendoCommand;
import de.embl.cba.bdp2.process.AbstractProcessingCommand;
import de.embl.cba.bdp2.record.MacroRecorder;

import java.io.File;
import java.util.List;

public class LuxendoInteractiveChannelSubsetter implements ChannelSubsetter
{
	private final String viewingModality;
	private final MacroRecorder< ? > recorder;

	public LuxendoInteractiveChannelSubsetter(  File directory, String viewingModality, boolean enableArbitraryPlaneSlicing, int stackIndex )
	{
		recorder = new MacroRecorder<>( OpenLuxendoChannelsCommand.COMMAND_FULL_NAME, viewingModality );
		recorder.setMessage( "// Please REMOVE ABOVE line before running the macro\n" );
		recorder.addOption( AbstractOpenCommand.DIRECTORY_PARAMETER, directory.getAbsolutePath() );
		recorder.addOption( AbstractOpenCommand.ARBITRARY_PLANE_SLICING_PARAMETER, enableArbitraryPlaneSlicing );
		recorder.addOption( OpenLuxendoCommand.STACK_INDEX_PARAMETER, stackIndex );

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
