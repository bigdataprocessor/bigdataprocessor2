package de.embl.cba.bdp2.process.rename;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.macro.MacroRecorder;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import ij.gui.GenericDialog;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.jetbrains.annotations.NotNull;

import static de.embl.cba.bdp2.process.rename.ImageRenameCommand.CHANNEL_NAMES_PARAMETER;

public class ImageRenameDialog< R extends RealType< R > & NativeType< R > >
{
	private final Image< R > inputImage;
	private final BdvImageViewer< R > viewer;
	private Image< R > outputImage;
	private String[] channelNames;

	public ImageRenameDialog( final BdvImageViewer< R > viewer  )
	{
		this.viewer = viewer;
		this.inputImage = viewer.getImage();
		showDialog();
	}

	protected void recordMacro()
	{
		final MacroRecorder recorder = new MacroRecorder( ImageRenameCommand.COMMAND_FULL_NAME, inputImage, outputImage );
		recorder.addOption( CHANNEL_NAMES_PARAMETER, String.join( ",", channelNames ) );
		recorder.record();
	}

	protected void showDialog()
	{
		final GenericDialog gd = new GenericDialog( "Rename Image" );

		final int length = inputImage.getName().length();
		gd.addStringField( "Image name", inputImage.getName(), 2 * length );

		channelNames = inputImage.getChannelNames();
		for ( int c = 0; c < channelNames.length; c++ )
		{
			gd.addStringField( "Channel " + c + " name", channelNames[ c ], 2 * length  );
		}

		gd.showDialog();

		if( gd.wasCanceled() ) return;

		outputImage = new Image<>( inputImage );

		outputImage.setName( gd.getNextString() );
		for ( int c = 0; c < channelNames.length; c++ )
		{
			channelNames[ c ] = gd.getNextString();
		}
		outputImage.setChannelNames( channelNames );

		viewer.replaceImage( outputImage, false, true );

		recordMacro();
	}
}
