/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2021 EMBL
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
