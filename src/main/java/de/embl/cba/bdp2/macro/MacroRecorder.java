package de.embl.cba.bdp2.macro;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.open.AbstractOpenCommand;
import ij.plugin.frame.Recorder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.stream.Collectors;

import static de.embl.cba.bdp2.process.AbstractImageProcessingCommand.*;

public class MacroRecorder
{
	public static final String COMMA = ", ";
	private final String commandName;
	private String options;
	private String message;
	private String function;
	private ArrayList< String > parameters;
	private Image< ? > inputImage;
	private Image< ? > outputImage;

	public MacroRecorder( String commandName, String outputImageHandling )
	{
		this.commandName = commandName;
		this.options = "";

		addOption( VIEWING_MODALITY_PARAMETER, outputImageHandling );
	}

	public MacroRecorder( String commandName, Image< ? > inputImage, Image< ? > outputImage )
	{
		this( commandName, inputImage, outputImage, AbstractOpenCommand.SHOW_IN_CURRENT_VIEWER );
	}

	public MacroRecorder( String commandName, Image< ? > inputImage, Image< ? > outputImage, String outputImageHandling )
	{
		this.commandName = commandName;
		this.options = "";
		this.inputImage = inputImage;
		this.outputImage = outputImage;

		addOption( INPUT_IMAGE_PARAMETER, this.inputImage.getName() );
		addOption( OUTPUT_IMAGE_NAME_PARAMETER, this.outputImage.getName() );
		addOption( VIEWING_MODALITY_PARAMETER, outputImageHandling );
	}

	public MacroRecorder( String commandName, Image< ? > inputImage )
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
					recorder.recordString( createAPICall() );

					if ( outputImage != null && ! outputImage.getName().equals( inputImage.getName() ) )
					{
						recorder.recordString( "image.setName( " + outputImage.getName() + "); " );
					}

				}
				else // macro recording
				{
					if ( message != null )
						Recorder.recordString( message );
					Recorder.record( "run", commandName, options );
				}

		}).start();
	}

	private String createAPICall()
	{
		if ( function == null )
		{
			return "// ERROR: no API call for:  " + commandName + "\n";
		}

		String apiCall = "image = BigDataProcessor2." + function + "( ";

		if ( inputImage != null )
			apiCall += "image" + COMMA;

		apiCall += parameters.stream().collect( Collectors.joining( COMMA ) );
		apiCall += " );\n";

		return apiCall;
	}

	public static String quote( String name )
	{
		return "\"" + name + "\"";
	}

	public void setMessage( String message )
	{
		this.message = message;
	}

	public void setAPIFunction( String function )
	{
		this.function = function;
	}

	public void addAPIFunctionParameter( String parameter )
	{
		if ( parameters == null )
			parameters = new ArrayList<>();

		parameters.add( parameter );
	}
}
