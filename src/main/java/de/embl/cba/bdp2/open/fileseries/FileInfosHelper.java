/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2021 EMBL
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
package de.embl.cba.bdp2.open.fileseries;

import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.open.NamingSchemes;
import de.embl.cba.bdp2.open.fileseries.hdf5.HDF5Helper;
import de.embl.cba.bdp2.utils.BioFormatsCalibrationReader;
import de.embl.cba.bdp2.utils.DimensionOrder;
import ij.IJ;

import javax.crypto.spec.PSource;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FileInfosHelper
{
    public static void setFileInfos5D( FileInfos fileInfos, String regExp, String[] channelSubset )
    {
        initFileInfos5D( fileInfos, regExp, channelSubset );

        fileInfos.ctzFileInfos = new BDP2FileInfo[fileInfos.nC][fileInfos.nT][fileInfos.nZ];
        fileInfos.dimensions = new long[ 5 ];
        fileInfos.dimensions[ DimensionOrder.X ] = fileInfos.nX;
        fileInfos.dimensions[ DimensionOrder.Y ] = fileInfos.nY;
        fileInfos.dimensions[ DimensionOrder.Z ] = fileInfos.nZ;
        fileInfos.dimensions[ DimensionOrder.C ] = fileInfos.nC;
        fileInfos.dimensions[ DimensionOrder.T ] = fileInfos.nT;

        if ( fileInfos.voxelUnit == null || fileInfos.voxelUnit.equals( "" ) )
            fileInfos.voxelUnit = "pixel";
    }

    public static void setImageMetadataFromTIFF(
            FileInfos fileInfos,
            String path)
    {
        BDP2FileInfo[] info;

        FastTIFFDecoder ftd = new FastTIFFDecoder( path );
        try
        {
            info = ftd.getTIFFInfo();
        }
        catch ( IOException e )
        {
            throw new RuntimeException( e );
        }

        if ( info[0].nImages > 1 )
        {
            fileInfos.nZ = info[0].nImages;
            info[0].nImages = 1;
        }
        else
        {
            fileInfos.nZ = info.length;
        }

        fileInfos.nX = info[0].width;
        fileInfos.nY = info[0].height;
        fileInfos.bitDepth = info[0].bytesPerPixel * 8;
        fileInfos.compression =  info[0].compression;
        fileInfos.numTIFFStrips = info[0].stripLengths.length;

        fileInfos.voxelSize = new double[]{
                info[0].pixelWidth,
                info[0].pixelHeight,
                info[0].pixelDepth };

        fileInfos.voxelUnit = info[0].unit;

        if ( fileInfos.voxelUnit != null )
            fileInfos.voxelUnit = fileInfos.voxelUnit.trim();
    }

    private static void fetchMetadata( FileInfos fileInfos, String regExp )
    {
        String firstPath = fileInfos.paths[ 0 ];

        if ( NamingSchemes.isTIFF( firstPath ) )
        {
            fetchTIFFMetadata( fileInfos, regExp, firstPath );
        }
        else if ( NamingSchemes.isLuxendoNamingScheme( regExp ) )
        {
            fileInfos.fileType = FileSeriesFileType.LUXENDO;
            HDF5Helper.setMetadataFromHDF5( fileInfos, firstPath );
        }
        else if ( NamingSchemes.isHDF5( firstPath ) )
        {
            fileInfos.fileType = FileSeriesFileType.HDF5_VOLUMES;
            HDF5Helper.setMetadataFromHDF5( fileInfos, firstPath );
        }
        else
        {
            Logger.error("Unsupported file type: " + firstPath );
        }
    }

    private static void fetchTIFFMetadata( FileInfos fileInfos, String regExp, String firstPath )
    {
        int nZ = fileInfos.nZ;

        setImageMetadataFromTIFF( fileInfos, firstPath );

        if ( regExp.contains( NamingSchemes.Z ) )
        {
            fileInfos.fileType = FileSeriesFileType.TIFF_PLANES;
            fileInfos.nZ = nZ; // correct back for single plane files
        }
        else // volumes
        {
            fileInfos.fileType = FileSeriesFileType.TIFF_STACKS;
        }

        final File omeCompanion = new File( fileInfos.directory, "ome-tiff.companion.ome" );
        if ( omeCompanion.exists() )
        {
            final BioFormatsCalibrationReader calibrationReader = new BioFormatsCalibrationReader();
            if ( calibrationReader.readCalibration( omeCompanion ) )
            {
                fileInfos.voxelSize = calibrationReader.getVoxelSize();
                fileInfos.voxelUnit = calibrationReader.getUnit();
            }
            else
            {
                Logger.error( "Error reading image calibration from: " + omeCompanion + "\n" +
                        "See Console window for more information.\n" +
                        "The voxel sizes of the image may be incorrect!" );
            }
        }
    }

    private static void initFileInfos5D( FileInfos fileInfos, String namingScheme, String[] channelSubset )
    {
        HashSet<String> channels = new HashSet();
        HashSet<String> timepoints = new HashSet();
        HashSet<String> slices = new HashSet();

        Pattern pattern = Pattern.compile( namingScheme );

        final Map< String, Integer > groupIndexToGroupName = getGroupIndexToGroupName( pattern );
        final ArrayList< Integer > channelGroups = new ArrayList<>();
        final ArrayList< Integer > timeGroups = new ArrayList<>();
        final ArrayList< Integer > sliceGroups = new ArrayList<>();

        for ( Map.Entry< String, Integer > entry : groupIndexToGroupName.entrySet() )
        {
            if ( entry.getKey().contains( "C" ) )
            {
                channelGroups.add( entry.getValue() );
            }
            else if ( entry.getKey().contains( "T" ) )
            {
                timeGroups.add( entry.getValue() );
            }
            else if ( entry.getKey().contains( "Z" ) )
            {
                sliceGroups.add( entry.getValue() );
            }
        }

        for ( String path : fileInfos.paths )
        {
            Matcher matcher = pattern.matcher( path );
            if ( matcher.matches() )
            {
                channels.add( getId( channelGroups, matcher ) );
                timepoints.add( getId( timeGroups, matcher ) );
                slices.add( getId( sliceGroups, matcher ) );
            }
        }

        List< String > sortedChannels = sort( channels );

        // TODO: can I do this via regExp? Probably difficult due to Luxendo,
        //  where the channel information is distributed across folder and filename..
        sortedChannels = subSetChannelsIfNecessary( channelSubset, sortedChannels );
        fileInfos.nC = sortedChannels.size();
        fileInfos.channelNames = sortedChannels.stream().toArray( String[]::new );

        List< String > sortedTimepoints = sort( timepoints );
        fileInfos.nT = sortedTimepoints.size();

        List< String > sortedSlices = sort( slices );
        fileInfos.nZ = sortedSlices.size();

        fetchMetadata( fileInfos, namingScheme );

        populateFileInfos5D(
                fileInfos,
                namingScheme,
                sortedChannels,
                sortedTimepoints,
                sortedSlices,
                channelGroups,
                timeGroups,
                sliceGroups);
    }

    private static List< String > subSetChannelsIfNecessary( String[] channelSubset, List< String > allChannels )
    {
        if ( channelSubset != null )
        {
            List< String > sortedSubset = sort( Arrays.asList( channelSubset ) );
            return sortedSubset;
        }
        else
        {
            return allChannels;
        }
    }

    public static List< String > sort( Collection< String > strings )
    {
        try
        {
            List< String > sorted = new ArrayList< >( strings );
            Collections.sort( sorted, new Comparator< String >()
            {
                @Override
                public int compare( String o1, String o2 )
                {
                    final Integer i1 = Integer.parseInt( o1 );
                    final Integer i2 = Integer.parseInt( o2 );
                    return i1.compareTo( i2 );
                }
            } );

            return sorted;
        }
        catch ( Exception e )
        {
            List< String > sorted = new ArrayList< >( strings );
            Collections.sort( sorted );
            return sorted;
        }
    }

    private static String getId( ArrayList< Integer > groups, Matcher matcher )
    {
        if ( groups.size() == 0 )
            return "0";

        ArrayList< String > ids = new ArrayList<>(  );
        for ( Integer group : groups )
        {
            ids.add( matcher.group( group ) );
        }

        return String.join( NamingSchemes.CHANNEL_ID_DELIMITER, ids );
    }

    public static Map< String, Integer > getGroupIndexToGroupName( Pattern pattern )
    {
        final Field namedGroups;
        try
        {
            namedGroups = pattern.getClass().getDeclaredField("namedGroups");
            namedGroups.setAccessible(true);
            return (Map<String, Integer>) namedGroups.get( pattern );
        } catch ( Exception e )
        {
            e.printStackTrace();
            throw new UnsupportedOperationException( "Could not extract group names from pattern: " + pattern );
        }
    }

    private static void populateFileInfos5D(
            FileInfos fileInfos,
            String regExp,
            List< String > channels,
            List< String > timepoints,
            List< String > slices,
            ArrayList< Integer > channelGroups,
            ArrayList< Integer > timeGroups,
            ArrayList< Integer > sliceGroups )
    {
        fileInfos.ctzFiles = new String[ fileInfos.nC ][ fileInfos.nT ][ fileInfos.nZ ];

        Pattern pattern = Pattern.compile( regExp );

        for ( String path : fileInfos.paths )
        {
            Matcher matcher = pattern.matcher( path );
            if ( matcher.matches() )
            {
                int c = channels.indexOf( getId( channelGroups, matcher ) );
                if ( c == -1 )
                    continue; // channels may have been subset => not all fileNames are matching

                int t = timepoints.indexOf( getId( timeGroups, matcher ) );
                if ( t == -1 )
                    throw new RuntimeException( "Could get time index for " + path );

                if ( regExp.contains( NamingSchemes.Z ) )
                {
                    int z = slices.indexOf( getId( sliceGroups, matcher ) );
                    if ( z == -1 )
                        throw new RuntimeException( "Could get slice index for " + path );
                    fileInfos.ctzFiles[ c ][ t ][ z ] = path;
                }
                else
                {
                    for ( int z = 0; z < fileInfos.nZ; z++ )
                    {
                        // all z with same file-name, because each file contains the whole volume
                        fileInfos.ctzFiles[ c ][ t ][ z ] = path;
                    }
                }
            }
            else
            {
                throw new UnsupportedOperationException( "Could not match file: " + path + "\nNaming scheme: " + regExp );
            }
        }
    }

    public static String[] fetchFiles( String directory, String patternString )
    {
        if ( ! new File( directory ).exists() )
        {
            throw new RuntimeException( "Directory not found: " + directory );
        }

        Pattern pattern = Pattern.compile( patternString );
        System.out.println( patternString );

        String[] files = null;
        try
        {
            //Files.walk( Paths.get( directory ) )
            //        .forEach( f -> IJ.log( f.toString() ) );
            files = Files.walk( Paths.get( directory ) )
                    .map( f -> f.toString() )
                    .filter( s -> pattern.matcher( s ).matches() )
                    .collect( Collectors.toList() )
                    .toArray( new String[ 0 ]);
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }

        if ( files == null || files.length == 0)
        {
            throw new RuntimeException( "No matching files found in " + directory );
        }

        return files;
    }

    public static String extractFolderPattern( String filterPattern )
    {
        if ( filterPattern != null )
        {
            //final String savePattern = toWindowsSplitSavePattern( filterPattern );
            //final String[] split = savePattern.split( Pattern.quote( File.separator ) );
            //final String[] split = filterPattern.split( Pattern.quote( File.separator ) + "(?!d\\))" );
            final String[] split = filterPattern.split( "/" );

            if ( split.length > 1 )
            {
                final String folderPattern = Arrays.stream( split ).limit( split.length - 1 ).collect( Collectors.joining("/") );
                return folderPattern;
            }
            else
            {
                return ".*";
            }
        }
        else
        {
            return ".*";
        }
    }

    public static String extractFilePattern( String filterPattern )
    {
        if ( filterPattern != null )
        {
            final String[] split = filterPattern.split( "/" );
            return split[ split.length - 1 ];
        }
        else
        {
            return ".*";
        }
    }

    // TODO: Do I need the filterPattern?
    private static String[] sortAndFilterFileList( String[] inputs, String regExp )
    {
        int count = 0;

        Pattern pattern = Pattern.compile( regExp );

        for (int i = 0; i < inputs.length; i++)
        {
            String name = inputs[i];
            if ( ! pattern.matcher( name ).matches() )
                inputs[i] = null;
            else
                count++;
        }

        if (count == 0) return null;
        String[] list = inputs;
        if (count < inputs.length)
        {
            list = new String[count];
            int index = 0;
            for (int i = 0; i < inputs.length; i++)
            {
                if (inputs[i] != null)
                    list[index++] = inputs[i];
            }
        }
        int listLength = list.length;
        boolean allSameLength = true;
        int len0 = list[0].length();
        for (int i = 0; i < listLength; i++)
        {
            if (list[i].length() != len0)
            {
                allSameLength = false;
                break;
            }
        }
        if (allSameLength)
        {
            ij.util.StringSorter.sort(list);
            return list;
        }
        int maxDigits = 15;
        String[] list2 = null;
        char ch;
        for (int i = 0; i < listLength; i++)
        {
            int len = list[i].length();
            String num = "";
            for (int j = 0; j < len; j++)
            {
                ch = list[i].charAt(j);
                if (ch >= 48 && ch <= 57) num += ch;
            }
            if (list2 == null) list2 = new String[listLength];
            if (num.length() == 0) num = "aaaaaa";
            num = "000000000000000" + num; // prepend maxDigits leading zeroes
            num = num.substring(num.length() - maxDigits);
            list2[i] = num + list[i];
        }
        if (list2 != null)
        {
            ij.util.StringSorter.sort(list2);
            for (int i = 0; i < listLength; i++)
                list2[i] = list2[i].substring(maxDigits);
            return list2;
        }
        else
        {
            ij.util.StringSorter.sort(list);
            return list;
        }
    }

    private static String[] getSubFolders( String parentFolder, String subFolderPattern )
    {
        String[] list = new File( parentFolder ).list( new FilenameFilter()
        {
            @Override
            public boolean accept( File parentFolder, String subFolder )
            {
                Pattern.compile( subFolderPattern ).matcher( subFolder );

                if ( ! new File( parentFolder, subFolder ).isDirectory() ) return false;

                if ( ! Pattern.compile( subFolderPattern ).matcher( subFolder ).matches() ) return false;

                return true;
            }
        });

        if ( list == null || list.length == 0 )
            return new String[]{""};


        for ( int i = 0; i < list.length; i++ )
        {
            list[ i ] = ensureDirectoryEndsWithFileSeparator( list[ i ] );
        }

        Arrays.sort( list );

        return list;
    }

    public static String captureRegExp( String subFolderName, String regExp )
    {
        Pattern pattern = Pattern.compile( regExp );
        Matcher matcher = pattern.matcher( subFolderName );

        if ( matcher.matches() )
        {
            return matcher.group( 1 );
        }
        else
        {
            return null;
        }
    }

    public static Set< String > captureMatchesInSubFolders( File directory, String regExp )
    {
        assert directory.isDirectory();

        Set< String > captures = new HashSet<>();
        String[] list = directory.list();
        for ( String item : list )
        {
            File file = new File( directory, item );
            if ( file.isDirectory() )
            {
                String stackIndex = captureRegExp( file.getName(), regExp );
                if ( stackIndex != null ) captures.add( stackIndex );
            }
        }
        return captures;
    }

    public static String ensureDirectoryEndsWithFileSeparator( String directory )
    {
        directory = directory.trim();
        char last = directory.charAt(directory.length()-1);
        if( last != File.separatorChar )
        {
            directory= directory + File.separator;
        }

        return directory;
    }
}
