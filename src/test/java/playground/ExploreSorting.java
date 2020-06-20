package playground;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class ExploreSorting
{
	public static void main( String[] args )
	{
		final HashSet< String > strings = new HashSet<>();
		strings.add( "1" );
		strings.add( "2" );
		strings.add( "10" );

		final List< String > sorted = sort( strings );

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
}
