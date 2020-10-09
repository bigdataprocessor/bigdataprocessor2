package de.embl.cba.bdp2.open;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class UnZipper
{
	public static File unzip( File input )
	{
		try
		{
			File imageDataDirectory = null;

			String fileZip = input.getAbsolutePath();
			File destDir = new File( input.getParent() );
			byte[] buffer = new byte[ 1024 ];
			ZipInputStream zis = new ZipInputStream( new FileInputStream( fileZip ) );
			ZipEntry zipEntry = zis.getNextEntry();
			while ( zipEntry != null )
			{
				if ( zipEntry.getName().endsWith( ".DS_Store" ) || zipEntry.getName().contains("__MACOSX/") )
				{
					zipEntry = zis.getNextEntry();
					continue;
				}

				if ( zipEntry.isDirectory() )
				{
					final File newDirectory = new File( destDir, zipEntry.getName() );
					newDirectory.mkdirs();
					if ( newDirectory.getName().equals( "image-data" ) )
					{
						imageDataDirectory = newDirectory;
					}
					zipEntry = zis.getNextEntry();
					continue;
				}

				final File file = new File( destDir, zipEntry.getName() );
				new File( file.getParent() ).mkdirs();
				FileOutputStream fos = new FileOutputStream( file );
				int len;
				while ( ( len = zis.read( buffer ) ) > 0 )
				{
					fos.write( buffer, 0, len );
				}
				fos.close();
				zipEntry = zis.getNextEntry();
			}
			zis.closeEntry();
			zis.close();
			return imageDataDirectory;
		}
		catch ( Exception e )
		{
			throw new RuntimeException( e );
		}
	}
}
