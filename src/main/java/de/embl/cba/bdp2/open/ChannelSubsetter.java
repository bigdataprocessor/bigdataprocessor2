package de.embl.cba.bdp2.open;

import java.util.List;

public class ChannelSubsetter
{
	private final List< String > channelSubset;

	public ChannelSubsetter( List< String > channelSubset )
	{
		this.channelSubset = channelSubset;
	}

	public List< String > getChannelSubset( List< String > channels )
	{
		for ( String s : channelSubset )
		{
			if ( ! channels.contains( s ) )
			{
				throw new UnsupportedOperationException( "Channel " + s + " does not exist in " + String.join( ",", channels ));
			}
		}

		return channelSubset;
	}
}
