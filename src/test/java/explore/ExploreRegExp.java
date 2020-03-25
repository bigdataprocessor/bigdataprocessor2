package explore;

import java.io.File;
import java.util.regex.Pattern;

public class ExploreRegExp
{
	public static void main( String[] args )
	{
		final String s = "adfsdfsd\\asdf(\\d)sa";
		final String replace = s.replace( "(\\d)", "_" );
		final String[] split = s.split( Pattern.quote( "\\" ) );

		final String[] split1 = s.split( Pattern.quote( "\\" ) + "(?!d\\))" );
	}
}
