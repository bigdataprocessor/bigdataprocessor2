package de.embl.cba.bdp2.macro;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.open.AbstractOpenFileSeriesCommand;
import ij.plugin.frame.Recorder;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static de.embl.cba.bdp2.process.AbstractImageProcessingCommand.*;

public class MacroRecorder
{
	public static final String COMMA = ", ";
	private String commandName;
	private String options;
	private String message;
	private String apiFunction;
	private ArrayList< String > parameters = new ArrayList<>();
	private Image< ? > inputImage;
	private Image< ? > outputImage;
	private boolean recordImportStatments = false;
	private boolean recordShowImageCall = false;
	private List< String > apiFunctionPrequels = new ArrayList<>(  );

	public MacroRecorder()
	{
	}

	public MacroRecorder( Image< ? > outputImage )
	{
		this.outputImage = outputImage;
	}

	public MacroRecorder( String commandName, String outputImageHandling, Image< ? > outputImage )
	{
		this.commandName = commandName;
		this.outputImage = outputImage;
		this.options = "";

		addCommandParameter( VIEWING_MODALITY_PARAMETER, outputImageHandling );
	}

	public MacroRecorder( String commandName, Image< ? > inputImage, Image< ? > outputImage )
	{
		this( commandName, inputImage, outputImage, AbstractOpenFileSeriesCommand.SHOW_IN_CURRENT_VIEWER );
	}

	public MacroRecorder( String commandName, Image< ? > inputImage, Image< ? > outputImage, String outputImageHandling )
	{
		this.commandName = commandName;
		this.options = "";
		this.inputImage = inputImage;
		this.outputImage = outputImage;

		addCommandParameter( INPUT_IMAGE_PARAMETER, this.inputImage.getName() );
		addCommandParameter( OUTPUT_IMAGE_NAME_PARAMETER, this.outputImage.getName() );
		addCommandParameter( VIEWING_MODALITY_PARAMETER, outputImageHandling );
	}

	public MacroRecorder( String commandName, Image< ? > inputImage )
	{
		this.commandName = commandName;
		this.inputImage = inputImage;
		this.options = "";

		addCommandParameter( INPUT_IMAGE_PARAMETER, inputImage.getName() );
	}

	public static String asJythonArray( double[] doubles )
	{
		return "array([" + asCSV( doubles ) + "], \"d\")";
	}

	public static String asJythonArray( long[] longs )
	{
		return "array([" + asCSV( longs ) + "], \"l\")";
	}

	public static String asJythonArray( String[] strings )
	{
		return "array([" + asCSV( strings ) + "], java.lang.String )";
	}

	public static String asCSV( double[] doubles )
	{
		return Arrays.stream( doubles ).mapToObj( x -> String.valueOf( x ) ).collect( Collectors.joining( "," ) );
	}

	public static String asCSV( long[] longs )
	{
		return Arrays.stream( longs ).mapToObj( x -> String.valueOf( x ) ).collect( Collectors.joining( "," ) );
	}

	public static String asCSV( String[] strings )
	{
		return Arrays.stream( strings ).map( s -> quote( s ) ).collect( Collectors.joining( "," ) );
	}

	public void addCommandParameter( String name, Object value )
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

	public static boolean isScriptMode()
	{
		return Recorder.scriptMode();
	}

	public void record()
	{
		new Thread( () -> {
			Recorder recorder = Recorder.getInstance();

			if ( recorder != null )
			{
				if ( Recorder.scriptMode() ) // Jython
				{
					removeIJRunCallFromRecorder();

					if ( recordImportStatments )
					{
						recorder.recordString( "# To run this script, please select language: Python\n" );
						recorder.recordString( "from de.embl.cba.bdp2 import BigDataProcessor2;\n" );
						recorder.recordString( "import java;\n" );
						recorder.recordString( "from jarray import array;\n" );
						recorder.recordString( "from de.embl.cba.bdp2.save import SavingSettings;\n" );
						recorder.recordString( "from de.embl.cba.bdp2.save import SaveFileType;\n" );
						recorder.recordString( "\n" );
					}

					if ( apiFunction != null )
					{
						for ( String prequel : apiFunctionPrequels )
						{
							recorder.recordString( prequel );
						}

						recorder.recordString( createAPICall() );
					}

					if ( outputImage != null )
					{
						if ( inputImage != null && !inputImage.getName().equals( outputImage.getName() ) )
							recorder.recordString( "image.setName( " + quote( outputImage.getName() ) + " );\n" );
					}

					if ( recordShowImageCall )
						recorder.recordString( "# BigDataProcessor2.showImage( image, True );\n" );

					recorder.recordString("\n");

				}
				else // macro recording
				{
					if ( message != null )
						recorder.recordString( message );
					recorder.record( "run", commandName, options );
				}
			}
		}).start();
	}

	private String createAPICall()
	{
		if ( apiFunction == null )
		{
			return "// ERROR: no API call for:  " + commandName + "\n";
		}

		String apiCall = "";
		if ( outputImage != null ) apiCall += "image = ";
		apiCall += "BigDataProcessor2." + apiFunction + "( ";
		if ( inputImage != null ) apiCall += "image" + COMMA;
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

	public void setAPIFunctionName( String function )
	{
		this.apiFunction = function;
	}

	public void addAPIFunctionParameter( String parameter )
	{
		parameters.add( parameter );
	}

	public void addAPIFunctionParameter( double[] parameter )
	{
		parameters.add( asJythonArray( parameter ) );
	}

	public void addAPIFunctionParameter( long[] parameter )
	{
		parameters.add( asJythonArray( parameter ) );
	}

	public void recordImportStatements( boolean recordImportStatements )
	{
		this.recordImportStatments = recordImportStatements;
	}

	public void recordShowImage( boolean recordShowImageCall )
	{
		this.recordShowImageCall = recordShowImageCall;
	}

	public void addAPIFunctionParameter( String[] strings )
	{
		parameters.add( asJythonArray( strings ) );
	}

	public void addAPIFunctionPrequel( String apiFunctionPrequel )
	{
		apiFunctionPrequels.add( apiFunctionPrequel + "\n" );
	}

	public void addAPIFunctionParameter( ArrayList< long[] > longsList )
	{
		List< String > listItems = new ArrayList<>( );
		for ( long[] longs : longsList )
		{
			listItems.add( asJythonArray( longs ) );
		}

		String parameter = "ArrayList( [ ";
		parameter += listItems.stream().collect( Collectors.joining( " ," ) );
		parameter += " ] )";
		parameters.add( parameter );
	}

	private void removeIJRunCallFromRecorder()
	{
		try
		{
			Recorder recorder = Recorder.getInstance();
			if ( recorder == null ) return;
			Field f = recorder.getClass().getDeclaredField("textArea"); //NoSuchFieldException
			f.setAccessible(true);
			TextArea textArea = (TextArea) f.get(recorder); //IllegalAccessException
			String text = textArea.getText();
			int start = text.indexOf( "IJ.run" );
			int end = text.length() - 1;
			textArea.replaceRange("", start, end );
		}
		catch ( Exception e )
		{
			//e.printStackTrace();
		}
	}
}
