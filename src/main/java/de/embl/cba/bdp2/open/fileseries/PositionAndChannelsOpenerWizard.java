/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2022 EMBL
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
package de.embl.cba.bdp2.open.fileseries;

import de.embl.cba.bdp2.open.ChannelChooserDialog;
import de.embl.cba.bdp2.open.NamingSchemes;
import ij.IJ;
import ij.gui.GenericDialog;

import java.io.File;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class PositionAndChannelsOpenerWizard
{
	private File directory;
	private final String positionRegExp;
	private final String positionChannelTimeRegExp;

	public PositionAndChannelsOpenerWizard( File directory, String positionRegExp, String positionChannelTimeRegExp )
	{
		this.directory = directory;
		this.positionRegExp = positionRegExp;
		this.positionChannelTimeRegExp = positionChannelTimeRegExp;
	}

	public void run()
	{
		String position = FileInfosHelper.captureRegExp( directory.toString(), positionRegExp );

		if ( position != null )
		{
			directory = new File( directory.getParent() );
		}
		else
		{
			Set< String > datasets = FileInfosHelper.captureMatchesInSubFolders( directory, positionRegExp );
			if ( datasets.isEmpty() )
			{
				IJ.showMessage("Could not find any folders matching " + positionRegExp );
				return;
			}
			else
			{
				GenericDialog gd = new GenericDialog( "Dataset Chooser" );
				gd.addChoice( "Open Dataset", datasets.toArray( new String[]{} ), null );
				gd.showDialog();
				if ( gd.wasCanceled() ) return;
				position = gd.getNextChoice();
			}
		}

		String channelTimeRegExp = positionChannelTimeRegExp.replace( NamingSchemes.P, position );

		// Fetch available channels and let user choose which ones to open
		//
		FileInfos fileInfos = new FileInfos( directory.toString(), channelTimeRegExp );
		final ChannelChooserDialog dialog = new ChannelChooserDialog( fileInfos.channelNames  );
		String[] selectedChannels = dialog.getChannelsViaDialog();

		// Open the image and record a macro
		//
		String channels = Arrays.stream( selectedChannels ).collect( Collectors.joining( "," ) );
		OpenChannelsFileSeriesCommand< ? > openCommand = new OpenChannelsFileSeriesCommand( directory, fileInfos.paths, channels, channelTimeRegExp );
		openCommand.run();
	}
}
