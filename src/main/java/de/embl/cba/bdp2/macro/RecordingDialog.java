package de.embl.cba.bdp2.macro;

import ij.gui.GenericDialog;
import ij.plugin.frame.Recorder;

public class RecordingDialog
{

	public static final String MACRO = "Macro";
	public static final String PYTHON = "Python"; // == Jython
	public static final String JAVA_SCRIPT = "JavaScript";

	public RecordingDialog()
	{
		final GenericDialog genericDialog = new GenericDialog( "Recording" );
		genericDialog.addCheckbox( "Enable recording", true );
		genericDialog.addChoice( "Recording language", new String[]{ MACRO, PYTHON, JAVA_SCRIPT }, PYTHON );
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
}
