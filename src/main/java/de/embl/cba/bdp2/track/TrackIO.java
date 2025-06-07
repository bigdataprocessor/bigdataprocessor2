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
package de.embl.cba.bdp2.track;

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
