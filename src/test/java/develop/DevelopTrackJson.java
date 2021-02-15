package develop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import de.embl.cba.bdp2.track.Track;

public class DevelopTrackJson
{
	public static void main( String[] args )
	{
		final Track hello = new Track( "hello", new double[]{ 1, 1, 1 } );
		hello.setPosition( 1, new double[]{2,2,2} );

		final ObjectMapper objectMapper = new ObjectMapper();

		try
		{
			final String s = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString( hello );
			System.out.println( s );
			final Gson gson = new Gson();
			final Track track = gson.fromJson( s, Track.class );
			int a = 1;
		}
		catch ( JsonProcessingException e )
		{
			e.printStackTrace();
			throw new RuntimeException( "Could not build Json string" );
		}
	}
}
