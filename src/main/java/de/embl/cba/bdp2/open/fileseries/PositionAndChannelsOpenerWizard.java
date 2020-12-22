package de.embl.cba.bdp2.open.fileseries;

import de.embl.cba.bdp2.open.ChannelChooserDialog;
import de.embl.cba.bdp2.open.fileseries.luxendo.OpenPositionAndChannelsFileSeriesCommand;
import ij.IJ;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class PositionAndChannelsOpenerWizard
{
	private File directory;
	private final String positionRegExp;
	private final String channelTimeRegExp;

	public PositionAndChannelsOpenerWizard( File directory, String positionRegExp, String channelTimeRegExp )
	{
		this.directory = directory;
		this.positionRegExp = positionRegExp;
		this.channelTimeRegExp = channelTimeRegExp;
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
			ArrayList< String > positions = FileInfosHelper.captureMatchesInSubFolders( directory, positionRegExp );
			if ( positions.isEmpty() )
			{
				IJ.showMessage("Could not find any folders matching " + positionRegExp );
				return;
			}
			else
			{
				// TODO: show position choice
				position = "";
			}
		}

		// Fetch available channels and let user choose which ones to open
		//
		FileInfos fileInfos = new FileInfos( directory.toString(), channelTimeRegExp );
		final ChannelChooserDialog dialog = new ChannelChooserDialog( fileInfos.channelNames  );
		String[] selectedChannels = dialog.getChannelsViaDialog();

		// Open the image and record a macro
		//
		String channels = Arrays.stream( selectedChannels ).collect( Collectors.joining( "," ) );
		OpenPositionAndChannelsFileSeriesCommand< ? > openCommand = new OpenPositionAndChannelsFileSeriesCommand( directory, fileInfos.getRelativeFilePaths(), channels, position, positionRegExp );
		openCommand.run();
		openCommand.recordMacro();


	}
}
