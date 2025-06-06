/*-
 * #%L
 * Various Java code for ImageJ
 * %%
 * Copyright (C) 2018 - 2024 EMBL
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
package de.embl.cba.bdp2.utils;

import net.imglib2.Cursor;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;

import java.util.ArrayList;
import java.util.Arrays;

public class NeighborhoodNonZeroBoundariesConverter< R extends RealType< R > >
		implements Converter< Neighborhood< R >, R >
{
	@Override
	public void convert( Neighborhood< R > neighborhood, R output )
	{
		long[] centrePosition = new long[ neighborhood.numDimensions() ];
		neighborhood.localize( centrePosition );

		final Cursor< R > cursor = neighborhood.localizingCursor();

		long[] position = new long[ neighborhood.numDimensions() ];
		final ArrayList< Double > values = new ArrayList<>();

		double centreValue = 0;

		while ( cursor.hasNext() )
		{
			final double value = cursor.next().getRealDouble();
			cursor.localize( position );
			if ( Arrays.equals( centrePosition, position ) )
				centreValue = value;
			values.add( value );
		}

		if ( centreValue == 0 )
		{
			output.setZero();
			return;
		}

		for ( double value : values )
		{
			if ( value != centreValue )
			{
				output.setReal( centreValue );
				return;
			}
		}

		output.setZero();
    }
}
