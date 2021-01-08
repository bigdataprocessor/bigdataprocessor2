package de.embl.cba.bdp2.record;

import de.embl.cba.bdp2.log.Logger;
import ij.Prefs;
import ij.plugin.frame.Recorder;

import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

public class LanguageManager
{
	public static final String MACRO = "Macro";
	public static final String JAVA_SCRIPT = "JavaScript";
	public static final String BEAN_SHELL = "BeanShell";
	public static final String PYTHON = "Python";
	public static final String JAVA = "Java";

	private LanguageManager()
	{
	}

	public static void setLanguage( String language )
	{
		try
		{
			Prefs.set( "recorder.mode", language );

			final Recorder recorder = Recorder.getInstance();
			if ( recorder == null ) return;

			Class recorderClass = Recorder.class;
			Field modeField = recorderClass.getDeclaredField("mode");
			modeField.setAccessible( true );
			Choice choice = (Choice) modeField.get(recorder);

			Field recorderModesField = recorderClass.getDeclaredField("modes");
			recorderModesField.setAccessible( true );
			String[] recorderModes = (String[]) recorderModesField.get(recorder);

			int languageIndex = Arrays.asList( recorderModes ).indexOf( language);
			if ( languageIndex == -1 )
			{
				throwError( language );
				return;
			}
			choice.select( languageIndex );

			Method setFileName = Recorder.class.getDeclaredMethod("setFileName" );
			setFileName.setAccessible( true );
			setFileName.invoke( recorder );
		}
		catch ( Exception e )
		{
			throwError( language );
		}
	}

	private static void throwError( String language )
	{
		Logger.error( "Failed to set recording language: " + language + "\n" +
				"Please [ Fiji > Help > Update ImageJ... ] select a version >1.53g and try again.");
	}

	public static String getLanguage()
	{
		try
		{
			Class  aClass = Recorder.class;
			Field field = aClass.getDeclaredField("mode");
			field.setAccessible( true );
			final Recorder recorder = Recorder.getInstance();
			if ( recorder == null ) return null;
			Choice choice = (Choice) field.get(recorder);
			return choice.getSelectedItem();
		}
		catch ( Exception e )
		{
			throw new RuntimeException( "Could not determine recording language." );
		}
	}
}
