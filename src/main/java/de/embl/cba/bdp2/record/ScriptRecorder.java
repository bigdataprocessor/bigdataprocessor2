package de.embl.cba.bdp2.record;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.open.AbstractOpenFileSeriesCommand;
import ij.plugin.frame.Recorder;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static de.embl.cba.bdp2.process.AbstractImageProcessingCommand.*;

public class ScriptRecorder
{
	private static final String COMMA = ", ";
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

	public ScriptRecorder()
	{
	}

	public ScriptRecorder( Image< ? > outputImage )
	{
		this.outputImage = outputImage;
	}

	public ScriptRecorder( String commandName, String outputImageHandling, Image< ? > outputImage )
	{
		this.commandName = commandName;
		this.outputImage = outputImage;
		this.options = "";

		addCommandParameter( VIEWING_MODALITY_PARAMETER, outputImageHandling );
	}

	public ScriptRecorder( String commandName, Image< ? > inputImage, Image< ? > outputImage )
	{
		this( commandName, inputImage, outputImage, AbstractOpenFileSeriesCommand.SHOW_IN_CURRENT_VIEWER );
	}

	public ScriptRecorder( String commandName, Image< ? > inputImage, Image< ? > outputImage, String outputImageHandling )
	{
		this.commandName = commandName;
		this.options = "";
		this.inputImage = inputImage;
		this.outputImage = outputImage;

		addCommandParameter( INPUT_IMAGE_PARAMETER, this.inputImage.getName() );
		addCommandParameter( OUTPUT_IMAGE_NAME_PARAMETER, this.outputImage.getName() );
		addCommandParameter( VIEWING_MODALITY_PARAMETER, outputImageHandling );
	}

	public ScriptRecorder( String commandName, Image< ? > inputImage )
	{
		this.commandName = commandName;
		this.inputImage = inputImage;
		this.options = "";

		addCommandParameter( INPUT_IMAGE_PARAMETER, inputImage.getName() );
	}

	public static String asScriptArray( double[] doubles )
	{
//		return "array([" + asCSV( doubles ) + "], \"d\")";
		return "[" + asCSV( doubles ) + "]";
	}

	public static String asScriptArray( long[] longs )
	{
		//return "array([" + asCSV( longs ) + "], \"l\")";
		return "[" + asCSV( longs ) + "]";
	}

	public static String asScriptArray( String[] strings )
	{
		//return "array([" + asCSV( strings ) + "], java.lang.String )";
		return "[" + asCSV( strings ) + "]";
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
			options += name + "=[" + fixBackslashes( (String) value ) + "] ";
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
		new Thread( () ->
		{
			Recorder recorder = Recorder.getInstance();

			if ( recorder != null )
			{
				if ( Recorder.scriptMode() )
				{
					removeMacroCallFromRecorder();

					if ( recordImportStatments )
					{
						if ( LanguageManager.getLanguage() == LanguageManager.PYTHON )
						{
							recorder.recordString( asComment( "To run this script, please select language: Python" ) );
							recorder.recordString( "import java;\n" );
							recorder.recordString( "from de.embl.cba.bdp2 import BigDataProcessor2;\n" );
							//recorder.recordString( "from jarray import array;\n" );
							recorder.recordString( "from de.embl.cba.bdp2.save import SavingSettings;\n" );
							recorder.recordString( "from de.embl.cba.bdp2.save import SaveFileType;\n" );
						}
						else if ( LanguageManager.getLanguage() == LanguageManager.JAVA_SCRIPT )
						{
							recorder.recordString( asComment( "To run this script, please select language: JavaScript" ) );
							recorder.recordString( "importClass(Packages.de.embl.cba.bdp2.BigDataProcessor2);\n" );
							recorder.recordString( "importClass(Packages.de.embl.cba.bdp2.save.SavingSettings);\n" );
							recorder.recordString( "importClass(Packages.de.embl.cba.bdp2.save.SaveFileType);\n" );
						}
						else
						{
							Logger.error( "Scripting language not supported: " + LanguageManager.getLanguage() + 									"\nPlease set the recording language in [ BigDataProcessor > Record > Record... ]" +
									"\nAnd repeat the processing step that you just did in order for it to be recorded."
							);
							return;
						}

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
					{
						recorder.recordString( asComment( "BigDataProcessor2.showImage( image, " + booleanToString( true ) + " );" ) );
					}

					recorder.recordString("\n");

				}
				else // macro recording
				{
					if ( message != null )
						recorder.recordString( message );
					if ( commandName != null && options != null )
						recorder.record( "run", commandName, options );
				}
			}
		}).start();
	}

	public static String booleanToString( boolean bool )
	{
		switch ( LanguageManager.getLanguage() )
		{
			case LanguageManager.PYTHON:
				return bool ? "True" : "False";
			case LanguageManager.JAVA_SCRIPT:
				return bool ? "true" : "false";
			default:
				return bool ? "true" : "false";
		}
	}

	private String createAPICall()
	{
		if ( apiFunction == null )
		{
			return asComment( "ERROR: no API call for:  " + commandName );
		}

		String apiCall = "";
		if ( outputImage != null ) apiCall += "image = ";
		apiCall += "BigDataProcessor2." + apiFunction + "( ";
		if ( inputImage != null ) apiCall += "image" + COMMA;
		apiCall += parameters.stream().collect( Collectors.joining( COMMA ) );
		apiCall += " );\n";

		apiCall = fixBackslashes( apiCall );

		return apiCall;
	}

	private String fixBackslashes( String apiCall )
	{
		final String language = LanguageManager.getLanguage();
		if ( language.equals( LanguageManager.JAVA_SCRIPT ) || language.equals( LanguageManager.MACRO ) )
			apiCall = apiCall.replace( "\\", "\\\\" );
		return apiCall;
	}

	private String asComment( String comment )
	{
		final String language = LanguageManager.getLanguage();

		if ( language == null ) return "";

		if ( language.equals( LanguageManager.PYTHON ) )
			return "# " + comment + "\n";
		else if ( language.equals( LanguageManager.JAVA_SCRIPT ) )
			return "// " + comment + "\n";
		else
			return "";
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
		parameters.add( asScriptArray( parameter ) );
	}

	public void addAPIFunctionParameter( long[] parameter )
	{
		parameters.add( asScriptArray( parameter ) );
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
		parameters.add( asScriptArray( strings ) );
	}

	public void addAPIFunctionPrequelComment( String comment )
	{
		apiFunctionPrequels.add( asComment( comment ) );
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
			listItems.add( asScriptArray( longs ) );
		}

		String parameter = "ArrayList( [ ";
		parameter += listItems.stream().collect( Collectors.joining( " ," ) );
		parameter += " ] )";
		parameters.add( parameter );
	}

	public static void removeMacroCallFromRecorder()
	{
		try
		{
			Recorder recorder = Recorder.getInstance();
			if ( recorder == null ) return;

			final String language = LanguageManager.getLanguage();
			if ( language != null  )
			{
				Field f = recorder.getClass().getDeclaredField( "textArea" ); //NoSuchFieldException
				f.setAccessible( true );
				TextArea textArea = ( TextArea ) f.get( recorder ); //IllegalAccessException
				String text = textArea.getText();
				if ( language.equals( LanguageManager.MACRO ))
				{
					int start = text.indexOf( "run(" );
					int end = text.length() - 1;
					textArea.replaceRange( "", start, end );
				}
				else
				{
					int start = text.indexOf( "IJ.run(" );
					int end = text.length() - 1;
					textArea.replaceRange( "", start, end );
				}
			}
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
	}
}
