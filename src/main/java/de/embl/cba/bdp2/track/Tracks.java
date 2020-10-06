package de.embl.cba.bdp2.track;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.File;
import java.io.FileReader;

public class Tracks
{
	public static Track fromJsonFile( File file )
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
}
