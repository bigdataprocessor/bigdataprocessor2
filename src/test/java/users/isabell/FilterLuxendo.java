package users.isabell;

import de.embl.cba.bdp2.loading.files.FileInfos;

import java.util.regex.Pattern;

public class FilterLuxendo
{
	public static void main( String[] args )
	{
		Pattern patternFilter = Pattern.compile( FileInfos.PATTERN_LUXENDO );

		String name = "_All_aaa.h5";
		final boolean matches = patternFilter.matcher( name ).matches();
		int a = 1;
	}
}
