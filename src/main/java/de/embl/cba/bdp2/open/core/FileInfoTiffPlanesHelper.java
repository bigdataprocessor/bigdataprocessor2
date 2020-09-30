package de.embl.cba.bdp2.open.core;

import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.open.OpenFileType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileInfoTiffPlanesHelper
{
    public static boolean initFileInfos( FileInfos fileInfos, String directory, String filterPattern, String[] fileList, int nC, int nZ, boolean isLeicaDSL, String namingScheme )
    {
        if ( fileList.length == 0 )
        {
            Logger.error( "No file matching this pattern were found: " + filterPattern );
            return false;
        }

        fileInfos.fileType = OpenFileType.TIFF_PLANES;

        int nT;
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
        ArrayList<HashSet<String>> channelsHS = new ArrayList();
        ArrayList<HashSet<String>> timepointsHS = new ArrayList();
        ArrayList<HashSet<String>> slicesHS = new ArrayList();

        // Deal with different file-names (fileIDs) due to
        // series being restarted during the imaging
        //
        for ( String fileID : fileIDs )
        {
            channelsHS.add( new HashSet() );
            timepointsHS.add( new HashSet() );
            slicesHS.add( new HashSet() );
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

                    if ( matcherC.matches() )
                    {
                        // has multi-channels
                        channelsHS.get( iFileID ).add( matcherC.group(1) );
                        matcherZ = patternZ.matcher( fileName );
                        if ( matcherZ.matches() )
                        {
                            slicesHS.get( iFileID ).add( matcherZ.group(1) );
                        }
                        else
                        {
                            slicesHS.get( iFileID ).add( "Z00" ); // Leica DSL
                        }
                    }
                    else
                    {
                        // has only one channel
                        matcherZ = patternZ.matcher(fileName);

                        if ( matcherZ.matches() )
                        {
                            slicesHS.get( iFileID ).add( matcherZ.group(1) );
                        }
                        else
                        {
                            slicesHS.get( iFileID ).add( "Z00" ); // Leica DSL
                        }
                    }

                    matcherT = patternT.matcher( fileName );

                    if ( matcherT.matches() )
                    {
                        timepointsHS.get( iFileID ).add( matcherT.group( 1 ) );
                    }
                    else
                    {
                        // has only one timepoint
                        timepointsHS.get( iFileID ).add( "T00" ); // Leica DSL
                    }
                }
            }
        }

        nT = 0;
        int[] tOffsets = new int[ fileIDs.length + 1 ]; // last offset is not used, but added anyway
        tOffsets[0] = 0;

        for (int iFileID = 0; iFileID < fileIDs.length; iFileID++)
        {
            nC = Math.max( 1, channelsHS.get(iFileID).size()) ;
            nZ = slicesHS.get( iFileID ).size(); // must be the same for all fileIDs

            Logger.info("FileID: " + fileIDs[iFileID]);
            Logger.info("  Channels: " + nC);
            Logger.info("  TimePoints: " + timepointsHS.get( iFileID ).size());
            Logger.info("  Slices: " + nZ);

            nT += timepointsHS.get( iFileID ).size();
            tOffsets[iFileID + 1] = nT;
        }

        //
        // sort into  final file list
        //

        fileInfos.ctzFiles = new String[nC][nT][nZ];

        for (int iFileID = 0; iFileID < fileIDs.length; iFileID++)
        {
            Pattern patternFileID = Pattern.compile(".*" + fileIDs[iFileID] + ".*");

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
                        z = Integer.parseInt( matcherZ.group(1) );
                    }
                    else
                    {
                        z = 0;
                    }

                    if ( matcherT.matches() )
                    {
                        t = Integer.parseInt( matcherT.group(1) );
                        t += tOffsets[iFileID];
                    }
                    else
                    {
                        t = 0;
                    }

                    if ( matcherC.matches() )
                    {
                        if ( nC == 1 )
                        {
                            c = 0; // in case the channel string is not "C00", but e.g. "C01"
                        }
                        else
                        {
                            c = Integer.parseInt(  matcherC.group(1) );
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

        FileInfosHelper.setImageMetadataFromTiff(fileInfos, directory, fileInfos.ctzFiles[0][0][0]);
        fileInfos.nZ = nZ;
        fileInfos.nC = nC;
        fileInfos.nT = nT;
        fileInfos.channelNames = new String[ nC ];
        for ( int channel = 0; channel < nC; channel++ )
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
