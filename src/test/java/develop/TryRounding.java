package develop;

public class TryRounding
{
	public static void main( String[] args )
	{
		for ( int i = 0; i < 4; i++ )
		{
			System.out.printf( "round(" + (i + 0.5) + ") = " + Math.round( i + 0.5 ) + "\n" );
		}
	}
}
