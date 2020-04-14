package de.embl.cba.bdp2.record;

import de.embl.cba.bdp2.image.Image;
import ij.plugin.frame.Recorder;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import static de.embl.cba.bdp2.scijava.command.AbstractProcessingCommand.*;

public class MacroRecorder< R extends RealType< R > & NativeType< R > >
{
	private final String commandName;
	private String options;

	public MacroRecorder( String commandName, Image< R > inputImage, Image< R > outputImage )
	{
		this( commandName, inputImage, outputImage, REPLACE_IN_VIEWER );
	}

	public MacroRecorder( String commandName, Image< R > inputImage, Image< R > outputImage, String outputImageHandling )
	{
		this.commandName = commandName;
		this.options = "";

		addOption( INPUT_IMAGE_PARAMETER, inputImage.getName() );
		addOption( OUTPUT_IMAGE_NAME_PARAMETER, outputImage.getName() );
		addOption( OUTPUT_MODALITY_PARAMETER, outputImageHandling );
	}

	public MacroRecorder( String commandName, Image< R > inputImage )
	{
		this.commandName = commandName;
		this.options = "";

		addOption( INPUT_IMAGE_PARAMETER, inputImage.getName() );
	}

	public void addOption( String name, Object value )
	{
		name = name.toLowerCase();

		if ( value instanceof String )
		{
			options += name + "=[" + value + "] ";
		}
		else
		{
			options += name + "=" + value + " ";
		}
	}

	public void record()
	{
		new Thread( () -> {
			Recorder recorder = Recorder.getInstance();

			if ( recorder != null )
				if ( ! Recorder.scriptMode() )
					Recorder.record( "run", commandName, options );

		}).start();
	}
}
