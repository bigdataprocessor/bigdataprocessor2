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
