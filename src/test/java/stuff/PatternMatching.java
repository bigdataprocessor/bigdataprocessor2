package stuff;

import java.util.regex.Pattern;

public class PatternMatching
{
	public static void main( String[] args )
	{
		Pattern pattern = Pattern.compile( "(?<C2>Cam_.*)_(?<T>\\d+).h5" );
		boolean matches = pattern.matcher( "Cam_Long_00002.h5" ).matches();
		System.out.println( matches );
	}
}
