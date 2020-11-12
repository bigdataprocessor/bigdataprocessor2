package de.embl.cba.bdp2.macro;

import ij.Prefs;
import ij.plugin.frame.Recorder;

import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

public class RecordingLanguageManager
{
	private final static String[] modes = {"Macro", "JavaScript", "BeanShell", "Java"};

	public RecordingLanguageManager()
	{
	}

	public void setLanguage( String language )
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
			e.printStackTrace();
		}
	}
}
