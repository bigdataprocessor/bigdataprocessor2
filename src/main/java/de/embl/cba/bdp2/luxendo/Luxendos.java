package de.embl.cba.bdp2.luxendo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.embl.cba.bdp2.open.core.NamingScheme.LUXENDO_STACKINDEX_REGEXP;

public class Luxendos
{
	public static String extractStackIndex( String subFolderName )
	{
		Pattern pattern = Pattern.compile( LUXENDO_STACKINDEX_REGEXP );
		Matcher matcher = pattern.matcher( subFolderName );
		String stackIndex;
		if ( matcher.matches() )
		{
			stackIndex = matcher.group( "StackIndex" );
		}
		else
		{
			throw new RuntimeException( subFolderName + " does not match pattern " + LUXENDO_STACKINDEX_REGEXP );
		}
		return stackIndex;
	}
}
