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
		OpenChannelsFileSeriesCommand< ? > openCommand = new OpenChannelsFileSeriesCommand( directory, fileInfos.relativeFilePaths, channels, channelTimeRegExp );
		openCommand.run();
	}
}
