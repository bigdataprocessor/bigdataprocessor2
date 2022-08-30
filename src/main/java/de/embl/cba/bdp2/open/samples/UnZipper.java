/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2022 EMBL
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package de.embl.cba.bdp2.open.samples;

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
