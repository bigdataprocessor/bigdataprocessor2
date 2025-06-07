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

import net.imglib2.Interval;

import java.util.ArrayList;
import java.util.List;

public class Corners
{
    public static final int MIN = 0;
    public static final int MAX = 1;
    public static int[] MIN_MAX = new int[] { MIN, MAX };


    public static long[] corner( int[] minMax, Interval interval )
    {
        assert minMax.length == interval.numDimensions();

        long[] corner = new long[ minMax.length ];

        for ( int d = 0; d < corner.length; ++d )
        {
            if ( minMax[ d ] == MIN )
            {
                corner[ d ] = interval.min( d );
            }
            else if ( minMax[ d ] == MAX )
            {
                corner[ d ] = interval.max( d );
            }
        }

        return corner;
    }


    public static List< long[] > corners( Interval interval )
    {
        int[] minMaxArray = new int[ interval.numDimensions() ];
        ArrayList< long[] > corners = new ArrayList<>(  );
        setCorners( corners, interval, minMaxArray,-1 );
        return corners;
    }

    public static void setCorners( ArrayList< long[] > corners, Interval interval, int[] minMaxArray, int d )
    {
        ++d;

        for ( int minMax : MIN_MAX )
        {
            minMaxArray[ d ] = minMax;

            if ( d == minMaxArray.length - 1 )
            {
                corners.add( corner( minMaxArray, interval ) );
            }
            else
            {
                setCorners( corners, interval, minMaxArray, d );
            }
        }

    }


}
