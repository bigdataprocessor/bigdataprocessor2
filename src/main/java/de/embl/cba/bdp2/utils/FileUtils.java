package de.embl.cba.bdp2.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileUtils
{
	public static void createOrEmptyDirectory( String directory )
	{
		if ( ! new File( directory ).exists() )
		{
			try
			{
				Files.createDirectories( Paths.get( directory ) );
				return;
			} catch ( IOException e )
			{
				e.printStackTrace();
			}
		}

		try
		{
			org.apache.commons.io.FileUtils.cleanDirectory( new File(directory) );
		} catch ( IOException e )
		{
			e.printStackTrace();
		}
	}
}
