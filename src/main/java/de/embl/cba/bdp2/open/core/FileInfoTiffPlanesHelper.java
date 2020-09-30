package de.embl.cba.bdp2.open.core;

import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.open.OpenFileType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileInfoTiffPlanesHelper
{
    public static boolean initFileInfos( FileInfos fileInfos, String directory, String filterPattern, String[] fileList, String namingScheme )
    {
        boolean isLeicaDSL = namingScheme.equals( NamingSchemes.LEICA_LIGHT_SHEET_TIFF );

        if ( fileList.length == 0 )
        {
            Logger.error( "No file matching this pattern were found: " + filterPattern );
            return false;
        }

        fileInfos.fileType = OpenFileType.TIFF_PLANES;

        int z,t,c;
        Matcher matcherZ, matcherC, matcherT, matcherID;
        Pattern patternZ = null, patternC = null, patternT = null, patternID = null;

        if ( isLeicaDSL )
        {
            patternC = Pattern.compile( ".*--C(\\d+).*" );
            patternZ = Pattern.compile( ".*--Z(\\d+).*" );
            patternT = Pattern.compile( ".*--t(\\d+).*" );
            patternID = Pattern.compile( ".*?_(\\d+).*" );
        }
        else
        {
            String[] split = namingScheme.split( "\\)" );
            for ( String s : split )
            {
                if ( s.contains( NamingSchemes.T ) )
                    patternT = Pattern.compile( completeRegex( s ) );
                if ( s.contains( NamingSchemes.C ) )
                    patternC = Pattern.compile( completeRegex( s ) );
                if ( s.contains( NamingSchemes.Z ) )
                    patternZ = Pattern.compile( completeRegex( s ) );
            }
            patternID = Pattern.compile( ".*" );
            int a = 1;
        }

        if ( patternC == null || patternT == null || patternZ == null )
        {
            Logger.error( "Could not parse naming scheme: " + namingScheme );
            return false;
        }

        Set< String > fileIDset = getFileIDs( fileList, isLeicaDSL, patternID );
        String[] fileIDs = fileIDset.toArray( new String[fileIDset.size()] );

        // check which different C, T and Z there are for each FileID
        ArrayList<HashSet<String>> channels = new ArrayList();
        ArrayList<HashSet<String>> timepoints = new ArrayList();
        ArrayList<HashSet<String>> slices = new ArrayList();

        // Deal with different file-names (fileIDs) due to
        // series being restarted during the imaging
        //
        for ( String fileID : fileIDs )
        {
            channels.add( new LinkedHashSet<>() );
            timepoints.add( new LinkedHashSet() );
            slices.add( new LinkedHashSet() );
        }

        for (int iFileID = 0; iFileID < fileIDs.length; iFileID++)
        {
            Pattern patternFileID;

            if ( fileIDs[ iFileID ].equals( ".*" ) )
            {
                patternFileID = Pattern.compile( ".*" );
            }
            else
            {
                patternFileID = Pattern.compile(".*?_" + fileIDs[ iFileID ] + ".*");
            }

            for ( String fileName : fileList )
            {
                if ( patternFileID.matcher( fileName ).matches() )
                {
                    matcherC = patternC.matcher( fileName );

                    if ( matcherC.matches() ) // has multi-channels
                    {
                        channels.get( iFileID ).add( matcherC.group(1) );
                        matcherZ = patternZ.matcher( fileName );
                        if ( matcherZ.matches() )
                        {
                            slices.get( iFileID ).add( matcherZ.group(1) );
                        }
                        else
                        {
                            slices.get( iFileID ).add( "Z00" ); // Leica DSL
                        }
                    }
                    else
                    {
                        // has only one channel
                        matcherZ = patternZ.matcher(fileName);

                        if ( matcherZ.matches() )
                        {
                            slices.get( iFileID ).add( matcherZ.group(1) );
                        }
                        else
                        {
                            slices.get( iFileID ).add( "Z00" ); // Leica DSL
                        }
                    }

                    matcherT = patternT.matcher( fileName );

                    if ( matcherT.matches() )
                    {
                        timepoints.get( iFileID ).add( matcherT.group( 1 ) );
                    }
                    else
                    {
                        // has only one timepoint
                        timepoints.get( iFileID ).add( "T00" ); // Leica DSL
                    }
                }
            }
        }

        fileInfos.nT = 0;
        int[] tOffsets = new int[ fileIDs.length + 1 ]; // last offset is not used, but added anyway
        tOffsets[0] = 0;

        for (int iFileID = 0; iFileID < fileIDs.length; iFileID++)
        {
            fileInfos.nC = Math.max( 1, channels.get( iFileID ).size()) ;
            fileInfos.nZ = slices.get( iFileID ).size(); // must be the same for all fileIDs
            fileInfos.nT += timepoints.get( iFileID ).size();

            Logger.info("FileID: " + fileIDs[iFileID]);
            Logger.info("  Channels: " + fileInfos.nC);
            Logger.info("  TimePoints: " + timepoints.get( iFileID ).size() );
            Logger.info("  Slices: " + fileInfos.nZ);

            tOffsets[iFileID + 1] = fileInfos.nT;
        }

        //
        // sort into  final file list
        //

        fileInfos.ctzFiles = new String[fileInfos.nC][fileInfos.nT][fileInfos.nZ];

        for (int iFileID = 0; iFileID < fileIDs.length; iFileID++)
        {
            Pattern patternFileID = Pattern.compile(".*" + fileIDs[iFileID] + ".*");

            ArrayList zList = new ArrayList( slices.get( iFileID ) );
            ArrayList cList = new ArrayList( channels.get( iFileID ) );
            ArrayList tList = new ArrayList( timepoints.get( iFileID ) );


            for ( String fileName : fileList )
            {
                if ( patternFileID.matcher(fileName).matches() )
                {
                    // figure out which C,Z,T the file is
                    matcherC = patternC.matcher( fileName );
                    matcherT = patternT.matcher( fileName );
                    matcherZ = patternZ.matcher( fileName );

                    if ( matcherZ.matches() )
                    {
                        z = zList.indexOf( matcherZ.group( 1 ) );
                    }
                    else
                    {
                        z = 0;
                    }

                    if ( matcherT.matches() )
                    {
                        t = tList.indexOf( matcherT.group( 1 ) );
                        t += tOffsets[iFileID];
                    }
                    else
                    {
                        t = 0;
                    }

                    if ( matcherC.matches() )
                    {
                        if ( fileInfos.nC == 1 )
                        {
                            c = 0; // in case the channel string is not "C00", but e.g. "C01"
                        }
                        else
                        {
                            c = cList.indexOf( matcherC.group(1) );
                        }
                    }
                    else
                    {
                        c = 0;
                    }

                    fileInfos.ctzFiles[ c ][ t ][ z ] = fileName;
                }
            }
        }

        int nZ = fileInfos.nZ; // because this will be set to 1 by below function for planar files
        FileInfosHelper.setImageMetadataFromTiff( fileInfos, directory, fileInfos.ctzFiles[0][0][0] );
        fileInfos.nZ = nZ;

        fileInfos.channelNames = new String[ fileInfos.nC ];
        for ( int channel = 0; channel < fileInfos.nC; channel++ )
        {
            fileInfos.channelNames[ channel ] = "channel " + channel;
        }

        return true;
    }

    @NotNull
    public static String completeRegex( String s )
    {
        return ".*" + s + ").*";
    }

    @NotNull
    public static Set< String > getFileIDs( String[] fileList, boolean isLeicaDSL, Pattern patternID )
    {
        // a fileID is the base file name, which can change during imaging for Leica naming scheme
        Set< String > fileIDset;
        if ( isLeicaDSL )
        {
            fileIDset = getLeicaFileIDs( fileList, patternID );
        }
        else
        {
            fileIDset = new HashSet<>();
            fileIDset.add( ".*" );
        }
        return fileIDset;
    }

    @NotNull
    public static Set< String > getLeicaFileIDs( String[] fileList, Pattern patternID )
    {
        Matcher matcherID;// check which different fileIDs there are
        // those are three numbers after the first _
        // this happens due to restarting the imaging
        Set<String> fileIDset = new HashSet();

        for ( String fileName : fileList )
        {
            matcherID = patternID.matcher( fileName );
            if (matcherID.matches())
            {
                fileIDset.add( matcherID.group(1) );
            }
        }

        if( fileIDset.size() == 0 )
        {
            fileIDset.add( ".*" );
        }

        return fileIDset;
    }
}
