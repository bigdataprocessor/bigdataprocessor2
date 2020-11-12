package de.embl.cba.bdp2.macro;

import ij.Prefs;
import ij.gui.GenericDialog;
import ij.plugin.frame.Recorder;

public class RecordingDialog
{

	public static final String MACRO = "Macro";
	public static final String JYTHON = "Jython";
	public static final String JAVA_SCRIPT = "JavaScript";

	public RecordingDialog()
	{
		final GenericDialog genericDialog = new GenericDialog("Recording");
		genericDialog.addCheckbox( "Enable recording", true );
		genericDialog.addChoice( "Recording language", new String[]{ MACRO, JYTHON }, JYTHON );
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

		RecordingLanguageManager languageManager = new RecordingLanguageManager();
		String language = genericDialog.getNextChoice();
		if ( language.equals( MACRO ) )
		{
			languageManager.setLanguage( MACRO );
		}
		else if ( language.equals( JYTHON ) )
		{
			// TODO: would be nice to have JYTHON as an actual choice:
			//  https://forum.image.sc/t/macrorecorder-imagej-language/44240/8
			Prefs.set( "recorder.mode", JAVA_SCRIPT );
			languageManager.setLanguage( JAVA_SCRIPT );
		}

	}
}
