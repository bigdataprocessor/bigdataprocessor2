package de.embl.cba.bdp2.macro;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.open.AbstractOpenCommand;
import ij.plugin.frame.Recorder;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import java.util.ArrayList;
import java.util.stream.Collectors;

import static de.embl.cba.bdp2.process.AbstractImageProcessingCommand.*;

public class MacroRecorder< R extends RealType< R > & NativeType< R > >
{
	private final String commandName;
	private String options;
	private String message;
	private String function;
	private ArrayList< String > parameters;

	public MacroRecorder( String commandName, String outputImageHandling )
	{
		this.commandName = commandName;
		this.options = "";
		this.parameters = new ArrayList<>();

		addOption( VIEWING_MODALITY_PARAMETER, outputImageHandling );
	}

	public MacroRecorder( String commandName, Image< R > inputImage, Image< R > outputImage )
	{
		this( commandName, inputImage, outputImage, AbstractOpenCommand.SHOW_IN_CURRENT_VIEWER );
	}

	public MacroRecorder( String commandName, Image< R > inputImage, Image< R > outputImage, String outputImageHandling )
	{
		this.commandName = commandName;
		this.options = "";

		addOption( INPUT_IMAGE_PARAMETER, inputImage.getName() );
		addOption( OUTPUT_IMAGE_NAME_PARAMETER, outputImage.getName() );
		addOption( VIEWING_MODALITY_PARAMETER, outputImageHandling );
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

				if ( Recorder.scriptMode() ) // Java like recording
				{
					String apiCall = createAPICall();
					recorder.recordString( apiCall );
				}
				else // macro recording
				{
					if ( message != null )
						Recorder.recordString( message );
					Recorder.record( "run", commandName, options );
				}

		}).start();
	}

	public String createAPICall()
	{
		if ( function == null )
		{
			return "// ERROR: no API call for:  " + commandName;
		}

		String apiCall = "BigDataProcessor2." + function + "(";
		String arguments = parameters.stream().collect( Collectors.joining( "," ) );
		apiCall += arguments;
		apiCall += ");";

		return apiCall;
	}

	public void setMessage( String message )
	{
		this.message = message;
	}

	public void addAPIFunction( String function )
	{
		this.function = function;
	}

	public void addAPIFunctionParameter( String parameter )
	{
		parameters.add( parameter );
	}
}
