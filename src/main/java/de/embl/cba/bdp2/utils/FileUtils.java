package de.embl.cba.bdp2.utils;

import java.io.File;
import java.io.IOException;

public class FileUtils
{
	public static void emptyDirectory( String directory )
	{
		try
		{
			org.apache.commons.io.FileUtils.cleanDirectory( new File(directory) );
		} catch ( IOException e )
		{
			e.printStackTrace();
		}
	}
}
