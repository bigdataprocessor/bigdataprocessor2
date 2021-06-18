package develop;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DevelopTerraStitcherParsing
{
	public static void main( String[] args )
	{
		String regExp = ".*\\/t(?<T>\\d+)\\/c(?<C>\\d+)\\/.*_(?<Z>\\d+).tif";
		regExp = regExp.replaceAll("/", Matcher.quoteReplacement( File.separator ) );

		final String directory = "/Users/tischer/Desktop/Benjamin/";

		try
		{
			String finalRegExp = regExp;
			Pattern pattern = Pattern.compile( finalRegExp );
			Files.walk( Paths.get( directory ) )
					.forEach(f -> {
						System.out.println( "RegExp: " + finalRegExp );
						final boolean matches = pattern.matcher( f.toString() ).matches();
						System.out.println( "toString:       " + f.toString() );
						System.out.println( "toAbsolutePath: " + f.toAbsolutePath().toString() );
						System.out.println( f.toString() + ": " + matches );
					} );
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}
	}
}
