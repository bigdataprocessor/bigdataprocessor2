package de.embl.cba.bdp2.open;

import ij.gui.GenericDialog;

import java.util.ArrayList;
import java.util.List;

public class ChannelChooserDialog
{
	private final List< String > channels;

	public ChannelChooserDialog( List< String > channels )
	{
		this.channels = channels;
	}

	public List< String > getChannelsViaDialog( )
	{
		final GenericDialog gd = new GenericDialog( "Select Channels..." );
		final int numChannels = channels.size();
		for ( int c = 0; c < numChannels; c++ )
		{
			gd.addCheckbox( channels.get( c ), true );
		}

		gd.showDialog();
		if ( gd.wasCanceled() ) return channels;

		final ArrayList< String > selected = new ArrayList<>();
		for ( int c = 0; c < numChannels; c++ )
		{
			if ( gd.getNextBoolean() )
			{
				selected.add( channels.get( c ) );
			}
		}

		return selected;
	}
}
