package de.embl.cba.bdp2.record;

import ij.gui.GenericDialog;
import ij.plugin.frame.Recorder;

import java.util.Arrays;

public class MacroRecordingDialog
{
	public static final String MACRO = "Macro";
	public static final String PYTHON = "Python"; // == Jython
	public static final String JAVA_SCRIPT = "JavaScript";
	public static final String[] LANGUAGES = new String[]{ MACRO, PYTHON, JAVA_SCRIPT };

	public MacroRecordingDialog()
	{
		final GenericDialog genericDialog = new GenericDialog( "Recording" );
		genericDialog.addCheckbox( "Enable recording", true );
		genericDialog.addChoice( "Recording language", LANGUAGES, getDefaultLanguage() );
		genericDialog.showDialog();

		if ( genericDialog.wasCanceled() ) return;

		final boolean enableMacroRecording = genericDialog.getNextBoolean();

		if ( enableMacroRecording )
		{
			new Recorder();
		}
		else
		{
			final Recorder recorder = Recorder.getInstance();
			recorder.scriptMode();
			if ( recorder != null )
				recorder.close();
		}

		final String language = genericDialog.getNextChoice();
		LanguageManager.setLanguage( language );
	}

	private String getDefaultLanguage()
	{
		String selectedLanguage = LanguageManager.getLanguage();
		if ( ! Arrays.asList( LANGUAGES ).contains( selectedLanguage ) )
			selectedLanguage = LANGUAGES[ 0 ];
		return selectedLanguage;
	}
}
