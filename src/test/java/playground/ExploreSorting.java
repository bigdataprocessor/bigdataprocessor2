package playground;

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
