package de.embl.cba.bdp2.macro;

import ij.gui.GenericDialog;
import ij.plugin.frame.Recorder;

public class MacroRecordingDialog
{
	public MacroRecordingDialog()
	{
		final GenericDialog genericDialog = new GenericDialog("Macro Recording");
		genericDialog.addCheckbox( "Enable macro recording", true );
		genericDialog.showDialog();

		if ( genericDialog.wasCanceled() ) return;

		final boolean enableMacroRecording = genericDialog.getNextBoolean();

		if ( enableMacroRecording )
		{
			new Recorder();
		}
		else
		{
			final Recorder instance = Recorder.getInstance();
			if ( instance != null )
				instance.close();
		}
	}
}
