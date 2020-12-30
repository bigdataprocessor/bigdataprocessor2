package de.embl.cba.bdp2.macro;

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

	// copied from Recorder
	private final static String[] modes = new String[]{ MACRO, JAVA_SCRIPT, BEAN_SHELL, PYTHON, JAVA };;

	private LanguageManager()
	{
	}

	public static void setLanguage( String language )
	{
		try
		{
			Prefs.set( "recorder.mode", language );

			Class  aClass = Recorder.class;
			Field field = aClass.getDeclaredField("mode");
			field.setAccessible( true );
			final Recorder recorder = Recorder.getInstance();
			Choice choice = (Choice) field.get(recorder);
			int languageIndex = Arrays.asList( modes ).indexOf(language);
			choice.select( languageIndex );

			Method setFileName = Recorder.class.getDeclaredMethod("setFileName" );
			setFileName.setAccessible( true );
			setFileName.invoke( recorder );
		}
		catch ( Exception e )
		{
			Logger.error( "Failed to set recording language: " + language + "\n" +
					"Please [ Fiji > Help > Update ImageJ...] select a version >1.53g and try again!");
		}
	}

	public static String getLanguage()
	{
		try
		{
			Class  aClass = Recorder.class;
			Field field = aClass.getDeclaredField("mode");
			field.setAccessible( true );
			final Recorder recorder = Recorder.getInstance();
			Choice choice = (Choice) field.get(recorder);
			return choice.getSelectedItem();
		}
		catch ( Exception e )
		{
			throw new RuntimeException( "Could not determine recording language." );
		}
	}
}
