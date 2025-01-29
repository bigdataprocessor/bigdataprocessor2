/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2025 EMBL
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
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
