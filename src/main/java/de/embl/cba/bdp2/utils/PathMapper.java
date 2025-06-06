/*-
 * #%L
 * Various Java code for ImageJ
 * %%
 * Copyright (C) 2018 - 2024 EMBL
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
package de.embl.cba.bdp2.utils;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public abstract class PathMapper
{

    public static List< String > asEMBLClusterMounted( List< Path > paths )
    {
        ArrayList< String > newPaths = new ArrayList<>();

        for ( Path path : paths )
        {
            newPaths.add( asEMBLClusterMounted( path.toString() ) );
        }

        return newPaths;
    }

    public static String asEMBLClusterMounted( String string )
    {
        return asEMBLClusterMounted( Paths.get( string ) );
    }

    public static String asEMBLClusterMounted( File file )
    {
        return asEMBLClusterMounted( file.toPath() );
    }

    public static String asEMBLClusterMounted( Path path )
    {

        String newPathString = path.toString();

        if ( newPathString.length() < 3 )
        {
            return newPathString;
        }

        if ( OSUtils.isMac() )
        {
            newPathString = newPathString.replace( "/Volumes/", "/g/" );
        }
        else if ( OSUtils.isWindows() )
        {
            try
            {
                Runtime runTime = Runtime.getRuntime();
                Process process = null;
                process = runTime.exec( "net use" );

                InputStream inStream = process.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader( inStream );
                BufferedReader bufferedReader = new BufferedReader( inputStreamReader );
                String line = null;
                String[] components = null;

                System.out.println( "Input path string: " + newPathString );
                while ( null != ( line = bufferedReader.readLine() ) )
                {
                    System.out.println( line );
                    components = line.split( "\\s+" );
                    if ( components.length > 2 )
                    {
                        System.out.println( components[ 1 ] + ": " + components[2] );

                        if ( components[ 1 ].equals( newPathString.substring( 0, 2 )  ) )
                        {
                            newPathString = newPathString.replace( components[ 1 ], components[ 2 ] );
                        }

                    }

                }

                newPathString = newPathString.replace( "\\", "/" );
                newPathString = newPathString.replaceFirst( "//[^/]*/", "/g/" );

            }
            catch ( IOException e )
            {
                System.out.println( e.toString() );
            }
        }

        return newPathString;

    }

}
