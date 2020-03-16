package users.isabell;

import java.util.regex.Pattern;

public class FilterLuxendo
{
	public static void main( String[] args )
	{
		System.out.println( Pattern.compile( ".*_df.*" + "|" + "^((?!_all).)*.h5$").matcher( "asfsdf_All_aaa.json" ).matches() );

		System.out.println( Pattern.compile( "Cam_Right_(\\d)+.h5$").matcher( "Cam_Right_All.h5" ).matches() );

	}
}
