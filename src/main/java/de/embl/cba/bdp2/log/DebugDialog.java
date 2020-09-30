package de.embl.cba.bdp2.log;

import ij.gui.GenericDialog;
import ij.plugin.frame.Recorder;

public class DebugDialog
{
	public DebugDialog()
	{
		final GenericDialog genericDialog = new GenericDialog("Debug");
		genericDialog.addCheckbox( "Enable debug logging", true );
		genericDialog.showDialog();

		if ( genericDialog.wasCanceled() ) return;

		Logger.debug.set( genericDialog.getNextBoolean() );
	}
}
