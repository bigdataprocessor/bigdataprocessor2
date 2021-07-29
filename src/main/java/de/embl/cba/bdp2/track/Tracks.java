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
package de.embl.cba.bdp2.track;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.*;

public class Tracks
{
	public static Track fromJson( File file )
	{
		try
		{
			final JsonReader reader = new JsonReader( new FileReader( file ) );
			final Gson gson = new Gson();
			final Track track = gson.fromJson( reader, Track.class );
			return track;
		}
		catch ( Exception e )
		{
			e.printStackTrace();
			throw new RuntimeException( "Could not parse file " + file.getAbsolutePath() );
		}
	}

	public static void toJson( File file, Track track )
	{
		if ( ! file.getName().endsWith( ".json" ) )
		{
			file = new File( file.getAbsolutePath() + ".json" );
		}

		track.setName( file.getName().replace( ".json", "" ) );

		try
		{
			OutputStream outputStream = new FileOutputStream( file );
			final JsonWriter writer = new JsonWriter( new OutputStreamWriter(outputStream, "UTF-8"));
			writer.setIndent("	");
			new Gson().toJson( track, track.getClass(), writer);
			writer.close();
		}
		catch ( Exception ex )
		{
			ex.printStackTrace();
		}
	}
}
