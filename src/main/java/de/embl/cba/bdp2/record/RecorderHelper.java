package de.embl.cba.bdp2.record;

import ij.plugin.frame.Recorder;

import java.awt.*;
import java.lang.reflect.Field;

public class RecorderHelper
{
	public static void removeCommandFromRecorder( String commandFullName )
	{
		try
		{
			Recorder recorder = Recorder.getInstance();
			if ( recorder == null ) return;
			Field f = recorder.getClass().getDeclaredField("textArea"); //NoSuchFieldException
			f.setAccessible(true);
			TextArea textArea = (TextArea) f.get(recorder); //IllegalAccessException
			String text = textArea.getText();
			int removeNumChars = Recorder.scriptMode() ? 8 : 5;
			int start = text.indexOf( commandFullName ) - removeNumChars;
			int end = text.length() - 1;
			textArea.replaceRange("", start, end );
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
	}

	private RecorderHelper()
	{
	}
}
