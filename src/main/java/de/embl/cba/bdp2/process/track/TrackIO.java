package de.embl.cba.bdp2.process.track;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

public class TrackIO
{
	public static String[] XYZ = new String[]{"X","Y","Z" };

	public static void saveTrack( File file, Track track ) throws IOException
	{
		final String delim = ",";

		BufferedWriter br = new BufferedWriter(new FileWriter( file ));
		StringBuilder sb = new StringBuilder();

		final Set< Integer > timePoints = track.getTimePoints();

		sb.append( "TimePoint" + delim );

		for ( int d = 0; d < track.numDimensions(); d++ )
			sb.append( XYZ[ d ] + "_Voxels" + delim );

		for ( int d = 0; d < track.numDimensions(); d++ )
			sb.append( XYZ[ d ] + "_Calibrated" + delim );

		endLine( sb );

		for ( int timePoint : timePoints )
		{
			sb.append( timePoint );
			sb.append( delim );
			Arrays.stream( track.getVoxelPosition( timePoint ) ).forEach( x -> sb.append( x + delim ) );
			Arrays.stream( track.getPosition( timePoint ) ).forEach( x -> sb.append( x + delim ) );
			endLine( sb );
		}

		br.write(sb.toString());
		br.close();
	}

	private static void endLine( StringBuilder sb )
	{
		sb.setLength( sb.length() - 1 ); // remove trailing delim
		sb.append( "\n" );
	}
}
