package de.embl.cba.bdp2.open;

import ij.gui.GenericDialog;

import java.util.ArrayList;
import java.util.List;

public class ChannelChooserDialog
{
	private final String[] channels;

	public ChannelChooserDialog( String[] channels )
	{
		this.channels = channels;
	}

	public String[] getChannelsViaDialog( )
	{
		final GenericDialog gd = new GenericDialog( "Select Channels..." );
		final int numChannels = channels.length;
		for ( int c = 0; c < numChannels; c++ )
		{
			gd.addCheckbox( channels[ c ], true );
		}

		gd.showDialog();
		if ( gd.wasCanceled() ) return channels;

		final ArrayList< String > selected = new ArrayList<>();
		for ( int c = 0; c < numChannels; c++ )
		{
			if ( gd.getNextBoolean() )
			{
				selected.add( channels[ c ] );
			}
		}

		return selected.toArray( new String[ 0 ] );
	}
}
