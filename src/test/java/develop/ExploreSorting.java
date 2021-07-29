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
package develop;

import java.util.*;
import java.util.stream.Collectors;

public class ExploreSorting
{
	public static void main( String[] args )
	{
		final HashSet< String > strings = new HashSet<>();
		strings.add( "1" );
		strings.add( "02" );
		strings.add( "10" );

		final List< String > sorted = sort2( strings );

		int a = 1;
	}

	public static List< String > sort( HashSet< String > strings )
	{
		try
		{
			final List< Integer > integers = strings.stream().mapToInt( Integer::parseInt ).boxed().collect( Collectors.toList() );
			Collections.sort( integers );
			final List< String > sorted = integers.stream().map( x -> "" + x ).collect( Collectors.toList() );
			return sorted;
		}
		catch ( Exception e )
		{
			List< String > sorted = new ArrayList< >( strings );
			Collections.sort( sorted );
			return sorted;
		}
	}

	public static List< String > sort2( HashSet< String > strings )
	{
		try
		{
			List< String > sorted = new ArrayList< >( strings );
			Collections.sort( sorted, new Comparator< String >()
			{
				@Override
				public int compare( String o1, String o2 )
				{
					final Integer i1 = Integer.parseInt( o1 );
					final Integer i2 = Integer.parseInt( o2 );
					return i1.compareTo( i2 );
				}
			} );

			return sorted;
		}
		catch ( Exception e )
		{
			List< String > sorted = new ArrayList< >( strings );
			Collections.sort( sorted );
			return sorted;
		}
	}
}
