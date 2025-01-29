/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2025 EMBL
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
