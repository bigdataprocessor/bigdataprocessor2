/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2021 EMBL
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

import de.embl.cba.bdp2.quit.QuitCommand;
import de.embl.cba.bdp2.viewer.ViewingModalities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class HeadlessMacroCreator
{
	private final File macroFile;
	private ArrayList< String > lines;

	public HeadlessMacroCreator( File macroFile )
	{
		this.macroFile = macroFile;
		readLines( macroFile );
	}

	private void readLines( File macroFile )
	{
		try
		{
			BufferedReader bufferedReader = new BufferedReader( new FileReader( macroFile ) );

			lines = new ArrayList<>();
			String line;
			while ( ( line = bufferedReader.readLine() ) != null )
			{
				if ( ! line.endsWith( ";" ) )
					line = line + ";";
				lines.add( line );
			}
			
		} catch ( Exception e )
		{
			e.printStackTrace();
		}
	}
	
	public String createHeadlessExecutableMacroString()
	{
		ArrayList< String > headlessCommands = new ArrayList<>();

		headlessCommands.add( "run(\"BDP2 Set Logging Level...\", \"level=Benchmark\");" );

		for ( String command : lines )
		{
			if ( command.contains( "BigDataProcessor2") ) continue;
			if ( command.equals( "" ) ) continue;

			command = command.replace( ViewingModalities.SHOW_IN_NEW_VIEWER, ViewingModalities.DO_NOT_SHOW );
			command = command.replace( ViewingModalities.SHOW_IN_CURRENT_VIEWER, ViewingModalities.DO_NOT_SHOW );
			headlessCommands.add( command );
		}

		headlessCommands.add( "run(\"" + QuitCommand.COMMAND_FULL_NAME +"\");" );

		String join = headlessCommands.stream().collect( Collectors.joining( " " ) );

		return join;
	}

	public static void main( String[] args )
	{
		HeadlessMacroCreator macroCreator = new HeadlessMacroCreator( new File( "/Users/tischer/Desktop/tmp2/SaveBDP2.ijm" ) );
		String headlessString = macroCreator.createHeadlessExecutableMacroString();
		System.out.println(
				"/Users/tischer/Desktop/Fiji-bdp2.app/Contents/MacOS/ImageJ-macosx --headless --mem=10000M -eval " +
						"'" + headlessString + "'" );
		// /Users/tischer/Desktop/Fiji-imflow.app/Contents/MacOS/ImageJ-macosx --headless -eval
	}

}
