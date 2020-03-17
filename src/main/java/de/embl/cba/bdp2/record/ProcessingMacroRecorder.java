package de.embl.cba.bdp2.record;

import de.embl.cba.bdp2.image.Image;
import ij.plugin.frame.Recorder;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class ProcessingMacroRecorder < R extends RealType< R > & NativeType< R > >
{
	private final String commandName;
	private final Image< R > inputImage;
	private final Image< R > outputImage;
	private String options;

	public ProcessingMacroRecorder( String commandName, Image< R > inputImage, Image< R > outputImage )
	{
		this.commandName = commandName;
		this.inputImage = inputImage;
		this.outputImage = outputImage;

		options = "";
		options += "inputImage=[" + inputImage.getName() + "] ";
		options += "outputImageName=[" + outputImage.getName() + "] ";

	}

	public void addOption( String name, Object value )
	{
		options += name + "=" + value + " ";
	}

	public void record()
	{
		Recorder recorder =  Recorder.getInstance();
		if( recorder != null )
			if ( ! Recorder.scriptMode() )
				Recorder.record( "run", commandName, options );
	}
}
