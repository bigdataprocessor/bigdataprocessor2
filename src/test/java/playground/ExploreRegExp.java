package playground;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExploreRegExp
{
	public static void main( String[] args )
	{
		final String s = "fsdf/sfdsf/stack_17_channel_2/Cam_Long_00000.h5";


		String regExp = ".*_channel_(\\d+)/Cam_(.*)_(\\d+).h5";
		Pattern pattern = Pattern.compile( regExp );

		Matcher matcher = pattern.matcher(s);
		final boolean matches = matcher.matches();
		final String group = matcher.group( 0 );
		final String group1 = matcher.group( 1 );
		final String group2 = matcher.group( 2 );
		final String group3 = matcher.group( 3 );

	}
}
