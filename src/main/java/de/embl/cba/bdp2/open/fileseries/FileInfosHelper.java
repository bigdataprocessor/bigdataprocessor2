package de.embl.cba.bdp2.open.fileseries;

import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.open.ChannelSubsetter;
import de.embl.cba.bdp2.open.OpenFileType;
import de.embl.cba.bdp2.open.NamingSchemes;
import de.embl.cba.bdp2.utils.BioFormatsCalibrationReader;
import de.embl.cba.bdp2.utils.DimensionOrder;
import org.apache.commons.lang.ArrayUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileInfosHelper
{
    public static void configureFileInfos5D( FileInfos fileInfos, String namingScheme, String filterPattern, ChannelSubsetter channels )
    {
        String directory = fileInfos.directory;

        String[][] fileLists = getFilesInFolders( directory, filterPattern );

        if ( fileLists == null )
        {
            Logger.error( "Error during file parsing..." );
            return;
        }

        if ( NamingSchemes.isLuxendoNamingScheme( namingScheme ) )
        {
            fileInfos.fileType = OpenFileType.LUXENDO;
        }

        initFileInfos5D( fileInfos, namingScheme, fileLists, channels );

        fileInfos.ctzFileInfos = new SerializableFileInfo[fileInfos.nC][fileInfos.nT][fileInfos.nZ];
        fileInfos.dimensions = new long[ 5 ];
        fileInfos.dimensions[ DimensionOrder.X ] = fileInfos.nX;
        fileInfos.dimensions[ DimensionOrder.Y ] = fileInfos.nY;
        fileInfos.dimensions[ DimensionOrder.Z ] = fileInfos.nZ;
        fileInfos.dimensions[ DimensionOrder.C ] = fileInfos.nC;
        fileInfos.dimensions[ DimensionOrder.T ] = fileInfos.nT;

        if ( fileInfos.voxelUnit == null || fileInfos.voxelUnit.equals( "" ) ) fileInfos.voxelUnit = "pixel";
    }

    public static void setImageMetadataFromTiff(
            FileInfos fileInfos,
            String directory,
            String fileName)
    {
        SerializableFileInfo[] info;

        FastTiffDecoder ftd = new FastTiffDecoder( directory, fileName );
        try
        {
            info = ftd.getTiffInfo();
        } catch ( IOException e )
        {
            e.printStackTrace();
            return;
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
        fileInfos.numTiffStrips = info[0].stripLengths.length;

        fileInfos.voxelSize = new double[]{
                info[0].pixelWidth,
                info[0].pixelHeight,
                info[0].pixelDepth };

        fileInfos.voxelUnit = info[0].unit;

        if ( fileInfos.voxelUnit != null )
            fileInfos.voxelUnit = fileInfos.voxelUnit.trim();
    }

    private static void fetchAndSetImageMetadata( FileInfos fileInfos, String directory, String namingScheme, String[] fileList )
    {
        if ( fileList[ 0 ].endsWith(".tif") )
        {
            int nZ = fileInfos.nZ;
            setImageMetadataFromTiff( fileInfos, directory, fileList[ 0 ] );

            if ( namingScheme.contains( NamingSchemes.Z ) )
            {
                fileInfos.fileType = OpenFileType.TIFF_PLANES;
                fileInfos.nZ = nZ; // correct back for single plane files
            }
            else // volumes
            {
                fileInfos.fileType = OpenFileType.TIFF_STACKS;
            }

            final File omeCompanion = new File( directory, "ome-tiff.companion.ome" );
            if ( omeCompanion.exists() )
            {
                final BioFormatsCalibrationReader calibrationReader = new BioFormatsCalibrationReader( omeCompanion );
                fileInfos.voxelSize = calibrationReader.getVoxelSize();
                fileInfos.voxelUnit = calibrationReader.getUnit();
            }
        }
        else if ( fileList[0].endsWith(".h5") )
        {
            fileInfos.fileType = OpenFileType.HDF5;
            FileInfosHDF5Helper.setImageDataInfoFromH5( fileInfos, directory, fileList[ 0 ] );
        }
        else
        {
            Logger.error("Unsupported file type: " + fileList[0]);
        }
    }

    private static void initFileInfos5D( FileInfos fileInfos, String namingScheme, String[][] fileLists, ChannelSubsetter channelSubset )
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

        for ( String fileName : fileLists[ 0 ] )
        {
            Matcher matcher = pattern.matcher( fileName );
            if ( matcher.matches() )
            {
                channels.add( getId( channelGroups, matcher ) );
                timepoints.add( getId( timeGroups, matcher ) );
                slices.add( getId( sliceGroups, matcher ) );
            }
        }

        List< String > sortedChannels = sort( channels );
        sortedChannels = subSetChannelsIfNecessary( channelSubset, sortedChannels );
        fileInfos.nC = sortedChannels.size();
        fileInfos.channelNames = sortedChannels.stream().toArray( String[]::new );

        List< String > sortedTimepoints = sort( timepoints );
        fileInfos.nT = sortedTimepoints.size();

        List< String > sortedSlices = sort( slices );
        fileInfos.nZ = sortedSlices.size();

        fetchAndSetImageMetadata( fileInfos, fileInfos.directory, namingScheme, fileLists[ 0 ] );

        populateFileInfos5D(
                fileInfos,
                namingScheme,
                fileLists[ 0 ],
                sortedChannels,
                sortedTimepoints,
                sortedSlices,
                channelGroups,
                timeGroups,
                sliceGroups);
    }

    private static List< String > subSetChannelsIfNecessary( ChannelSubsetter channelSubsetter, List< String > sortedChannels )
    {
        if ( channelSubsetter != null )
        {
            sortedChannels = sort( channelSubsetter.getChannelSubset( sortedChannels ) );
        }

        return sortedChannels;
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
            String[] fileList,
            List< String > channels,
            List< String > timepoints,
            List< String > slices,
            ArrayList< Integer > channelGroups,
            ArrayList< Integer > timeGroups,
            ArrayList< Integer > sliceGroups )
    {
        fileInfos.ctzFiles = new String[ fileInfos.nC ][ fileInfos.nT ][ fileInfos.nZ ];

        Pattern pattern = Pattern.compile( regExp );

        for ( String fileName : fileList )
        {
            Matcher matcher = pattern.matcher( fileName );
            if ( matcher.matches() )
            {
                int c = channels.indexOf( getId( channelGroups, matcher ) );
                if ( c == -1 )
                    continue; // channels may have been subset => not all fileNames are matching

                int t = timepoints.indexOf( getId( timeGroups, matcher ) );
                if ( t == -1 )
                    throw new RuntimeException( "Could get time index for " + fileName );

                if ( regExp.contains( NamingSchemes.Z ) )
                {
                    int z = slices.indexOf( getId( sliceGroups, matcher ) );
                    if ( z == -1 )
                        throw new RuntimeException( "Could get slice index for " + fileName );
                    fileInfos.ctzFiles[ c ][ t ][ z ] = fileName;
                }
                else
                {
                    for ( int z = 0; z < fileInfos.nZ; z++ )
                    {
                        // all z with same file-name, because each file contains the whole volume
                        fileInfos.ctzFiles[ c ][ t ][ z ] = fileName;
                    }
                }
            }
            else
            {
                throw new UnsupportedOperationException( "Could not match file: " + fileName
                        + "\nNaming scheme: " + regExp
                        + "\nPattern: " + pattern.toString() );
            }
        }
    }

    private static String[][] getFilesInFolders( String directory, String filterPattern )
    {
        if ( ! new File( directory ).exists() )
        {
            Logger.error("Directory not found: " + directory );
            return null;
        }

        String[][] fileLists;

        final String filePattern = getFileFilter( filterPattern );
        final String folderPattern = getFolderPattern( filterPattern );

        Logger.info( "Sub-folder name pattern: " + folderPattern );
        Logger.info( "File name pattern: " + filePattern );

        final String[] subFolders = getSubFolders( directory, folderPattern );

        if ( subFolders == null )
        {
            throw new UnsupportedOperationException( "No sub-folders found; please make sure to select the stack's parent folder."
                    + "\nParent folder: " + directory
                    + "\nSub-folder pattern: " + folderPattern );
        }
        else
        {
            // TODO: Clean this up
            if ( subFolders.length > 1 && ! subFolders[ 0 ].equals( "" ) )
                Logger.info( "Found " + subFolders.length + " sub-folders" );
        }

        String[] files = new String[]{};
        for (int i = 0; i < subFolders.length; i++)
        {
            final String subFolder = directory + subFolders[ i ];
            Logger.info( "Fetching files in " + subFolder  );

            String[] filesInFolder = getFilesInFolder( subFolder, filePattern );

            if ( filesInFolder == null )
            {
                throw new UnsupportedOperationException( "No files found in folder: " + subFolder);
            }
            else
            {
                Logger.info( "Found " + filesInFolder.length + " files in folder: " + subFolder);
            }

            if ( ! subFolders[ i ].equals( "" ) )
            {
                // prepend subfolder
                final int j = i;
                filesInFolder = Arrays.stream( filesInFolder ).map( x -> subFolders[ j ] + File.separator + x ).toArray( String[]::new );
            }

            files = (String[]) ArrayUtils.addAll( files, filesInFolder );
        }

        fileLists = new String[1][];
        fileLists[ 0 ] = files;

        return fileLists;
    }

    public static String getFolderPattern( String filterPattern )
    {
        if ( filterPattern != null )
        {
            //final String savePattern = toWindowsSplitSavePattern( filterPattern );
            //final String[] split = savePattern.split( Pattern.quote( File.separator ) );
            //final String[] split = filterPattern.split( Pattern.quote( File.separator ) + "(?!d\\))" );
            final String[] split = filterPattern.split( "/" );

            if ( split.length > 1 )
            {
                final String folder = split[ 0 ];
                return folder;
                // return fromWindowsSplitSavePattern( folder );
            } else
                return ".*";
        }
        else
        {
            return ".*";
        }
    }

    public static String getFileFilter( String filterPattern )
    {
        if ( filterPattern != null )
        {
            //final String separator = Pattern.quote( File.separator );
            //final String[] split = filterPattern.split( separator + "(?!d\\))" );
            final String[] split = filterPattern.split( "/" );
            if ( split.length > 1 )
                return split[ 1 ];
            else
                return split[ 0 ];
        }
        else
        {
            return ".*";
        }
    }

    // TODO: Do I need the filterPattern?
    private static String[] sortAndFilterFileList( String[] rawlist, String filterPattern )
    {
        int count = 0;

        Pattern patternFilter = Pattern.compile( filterPattern );

        for (int i = 0; i < rawlist.length; i++)
        {
            String name = rawlist[i];
            if ( ! patternFilter.matcher( name ).matches() )
                rawlist[i] = null;
            else if ( name.endsWith(".tif") || name.endsWith(".h5") )
                count++;
            else
                rawlist[i] = null;
        }

        if (count == 0) return null;
        String[] list = rawlist;
        if (count < rawlist.length)
        {
            list = new String[count];
            int index = 0;
            for (int i = 0; i < rawlist.length; i++)
            {
                if (rawlist[i] != null)
                    list[index++] = rawlist[i];
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

    private static String[] getFilesInFolder(String directory, String filterPattern)
    {
        String[] list = new File( directory ).list();

        if (list == null || list.length == 0)
            return null;

        //Logger.info( "Sorting and filtering file list..." );
        list = sortAndFilterFileList( list, filterPattern );

        if (list == null) return null;

        else return ( list );
    }

    private static String[] getSubFolders( String parentFolder, String subFolderPattern )
    {
        String[] list = new File( parentFolder ).list( new FilenameFilter()
        {
            @Override
            public boolean accept( File parentFolder, String subFolder )
            {
                if ( ! new File( parentFolder, subFolder ).isDirectory() ) return false;

                Pattern.compile( subFolderPattern ).matcher( subFolder );

                if ( ! Pattern.compile( subFolderPattern ).matcher( subFolder ).matches() ) return false;

                return true;
            }
        });

        if ( list == null || list.length == 0 )
            return new String[]{""};

        Arrays.sort( list );

        return (list);
    }
}
