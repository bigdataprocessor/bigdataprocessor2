package de.embl.cba.bdp2.loading.files;

import de.embl.cba.bdp2.loading.FastTiffDecoder;
import de.embl.cba.bdp2.logging.Logger;
import de.embl.cba.bdp2.utils.Utils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileInfosHelper
{

    public static boolean setFileInfos(
            FileInfos infoSource,
            String directory,
            String namingPattern)
    { // previously, setMissingInfos
        int[] ctzMin = new int[3];
        int[] ctzMax = new int[3];
        int[] ctzPad = new int[3];
        int[] ctzSize = new int[3];
        boolean hasC = false;
        boolean hasT = false;
        boolean hasZ = false;

        Matcher matcher;

        Logger.info("Importing/creating file information from pre-defined naming scheme.");
        // channels
        matcher = Pattern.compile(".*<C(\\d+)-(\\d+)>.*").matcher(namingPattern);
        if (matcher.matches()) {
            hasC = true;
            ctzMin[0] = Integer.parseInt(matcher.group(1));
            ctzMax[0] = Integer.parseInt(matcher.group(2));
            ctzPad[0] = matcher.group(1).length();
        } else {
            ctzMin[0] = ctzMax[0] = ctzPad[0] = 0;
        }

        infoSource.channelFolders = new String[ctzMax[0] - ctzMin[0] + 1];
        Arrays.fill(infoSource.channelFolders, "");

        // frames
        matcher = Pattern.compile(".*<T(\\d+)-(\\d+)>.*").matcher(namingPattern);
        if (matcher.matches()) {
            hasT = true;
            ctzMin[1] = Integer.parseInt(matcher.group(1));
            ctzMax[1] = Integer.parseInt(matcher.group(2));
            ctzPad[1] = matcher.group(1).length();
        } else {
            ctzMin[1] = ctzMax[1] = ctzPad[1] = 0;
        }

        // slices
        matcher = Pattern.compile(".*<Z(\\d+)-(\\d+)>.*").matcher(namingPattern);
        if (matcher.matches()) {
            hasZ = true;
            ctzMin[2] = Integer.parseInt(matcher.group(1));
            ctzMax[2] = Integer.parseInt(matcher.group(2));
            ctzPad[2] = matcher.group(1).length();
        } else {
            // determine number of slices from a file...
            Logger.error("Please provide a z range as well.");
            return false;
        }

        for (int i = 0; i < 3; ++i){
            ctzSize[i] = ctzMax[i] - ctzMin[i] + 1;
        }
        infoSource.nC = ctzSize[0];
        infoSource.nT = ctzSize[1];
        infoSource.nZ = ctzSize[2];

        infoSource.ctzFileList = new String[ctzSize[0]][ctzSize[1]][ctzSize[2]];

        if (namingPattern.contains("<Z") && namingPattern.contains(".tif")) {
            infoSource.fileType = Utils.FileType.SINGLE_PLANE_TIFF.toString();
        } else {
            Logger.error("Sorry, currently only single tiff planes supported");
            return false;
        }

        boolean isObtainedImageDataInfo = false;

        for (int c = ctzMin[0]; c <= ctzMax[0]; c++) {
            for (int t = ctzMin[1]; t <= ctzMax[1]; t++) {
                for (int z = ctzMin[2]; z <= ctzMax[2]; z++) {

                    String fileName = "";

                    if (infoSource.fileType.equals(Utils.FileType.SINGLE_PLANE_TIFF.toString())) {
                        fileName = namingPattern.replaceFirst("<Z(\\d+)-(\\d+)>",String.format("%1$0" + ctzPad[2] + "d", z));
                    } else {
                        Logger.error("BigDataProcessor:setMissingInfos:unsupported file type");
                    }
                    if (hasC) {
                        fileName = fileName.replaceFirst("<C(\\d+)-(\\d+)>",String.format("%1$0" + ctzPad[0] + "d", c));
                    }

                    if (hasT) {
                        fileName = fileName.replaceFirst("<T(\\d+)-(\\d+)>",String.format("%1$0" + ctzPad[1] + "d", t));
                    }

                    infoSource.ctzFileList[c - ctzMin[0]][t - ctzMin[1]][z - ctzMin[2]] = fileName;

                    if (!isObtainedImageDataInfo) {
                        File f = new File(directory + infoSource.channelFolders[c - ctzMin[0]] + "/" + fileName);

                        if (f.exists() && !f.isDirectory()) {
                            setImageMetadataFromTiff(infoSource,directory + infoSource.channelFolders[c - ctzMin[0]],fileName);

                            if (infoSource.fileType.equals(Utils.FileType.SINGLE_PLANE_TIFF.toString()))
                                infoSource.nZ = ctzSize[2];

                            Logger.info("Found one file; setting nx,ny,nz and bit-depth from this file: "+ fileName);
                            isObtainedImageDataInfo = true;
                        }
                    }
                }
            }
        }
        if (!isObtainedImageDataInfo) {
            Logger.error("Could not open data set. There needs to be at least one file matching the naming scheme.");
        }

        return isObtainedImageDataInfo;
    }

    public static void setImageMetadataFromTiff(
            FileInfos fileInfos,
            String directory,
            String fileName)
    {
        SerializableFileInfo[] info;

        try
        {
            FastTiffDecoder ftd = new FastTiffDecoder(directory, fileName);
            info = ftd.getTiffInfo();
        }
        catch (Exception e)
        {
            info = null; // TODO : Handle exceptions properly --ashis
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

        fileInfos.voxelSpacing = new double[]{
                info[0].pixelWidth,
                info[0].pixelHeight,
                info[0].pixelDepth };

        fileInfos.voxelUnit = info[0].unit;

        if ( fileInfos.voxelUnit != null )
            fileInfos.voxelUnit = fileInfos.voxelUnit.trim();
    }


    public static void setFileInfos(
            FileInfos fileInfos,
            String directory,
            String namingScheme,
            String filterPattern)
    {

        String[][] fileLists =
                getFilesInFolders( fileInfos, directory, namingScheme, filterPattern );

        if ( fileLists == null )
        {
            Logger.error( "Error during file parsing..." );
            return;
        }

        if ( namingScheme.equals( FileInfos.LEICA_LIGHT_SHEET_TIFF ) )
        {
            fileInfos.fileType = Utils.FileType.SINGLE_PLANE_TIFF.toString();

            String dataDirectory = getFirstChannelDirectory( fileInfos, directory );

            FileInfosLeicaHelper.initLeicaSinglePlaneTiffData(
                    fileInfos, dataDirectory, filterPattern, fileLists[ 0 ], fileInfos.nC, fileInfos.nZ );

        }
        else // tiff or h5
        {
            setFileInfos( fileInfos, namingScheme, fileLists );
        }

    }

    private static void  setImageMetadata( FileInfos fileInfos, String directory, String namingScheme, String[] fileList )
    {
        if ( fileList[ 0 ].endsWith(".tif") )
        {
            setImageMetadataFromTiff(
                    fileInfos,
                    directory,
                    fileList[ 0 ] );

            if ( namingScheme.equals( FileInfos.TIFF_SLICES ) )
            {
                fileInfos.fileType = Utils.FileType.SINGLE_PLANE_TIFF.toString();
                fileInfos.nZ = fileList.length;
            }
            else
            {
                fileInfos.fileType = Utils.FileType.TIFF_STACKS.toString();
            }
        }
        else if ( fileList[0].endsWith(".h5") )
        {
            FileInfosHDF5Helper.setImageDataInfoFromH5(
                    fileInfos,
                    directory,
                    fileList[0]);
            fileInfos.fileType = Utils.FileType.HDF5.toString();
        }
        else
        {
            Logger.error("Unsupported file type: " + fileList[0]);
        }
    }

    private static void fixChannelFolders( FileInfos fileInfos, String namingScheme )
    {
        //
        // Create dummy channel folders, if no real ones exist
        //
        if ( ! namingScheme.equals( FileInfos.LOAD_CHANNELS_FROM_FOLDERS) )
        {
            fileInfos.channelFolders = new String[ fileInfos.nC ];
            for ( int c = 0; c < fileInfos.nC; c++) fileInfos.channelFolders[ c ] = "";
        }
    }

    private static void setFileInfos( FileInfos fileInfos, String namingScheme, String[][] fileLists )
    {
        if ( namingScheme.equals( FileInfos.LOAD_CHANNELS_FROM_FOLDERS )
            || namingScheme.equalsIgnoreCase( FileInfos.SINGLE_CHANNEL_TIMELAPSE )
            || namingScheme.equals( FileInfos.TIFF_SLICES ) )
        {
            if ( namingScheme.equals( FileInfos.LOAD_CHANNELS_FROM_FOLDERS ) )
            {
                fileInfos.nC = fileInfos.channelFolders.length;
                fileInfos.nT = fileLists[ 0 ].length;
            }
            else if ( namingScheme.equalsIgnoreCase( FileInfos.SINGLE_CHANNEL_TIMELAPSE ) )
            {
                fileInfos.nC = 1;
                fileInfos.nT = fileLists[ 0 ].length;

            }
            else if ( namingScheme.equals( FileInfos.TIFF_SLICES ) )
            {
                fileInfos.nC = 1;
                fileInfos.nT = 1;
                fileInfos.nZ = fileLists[ 0 ].length;
                fileInfos.fileType = Utils.FileType.SINGLE_PLANE_TIFF.toString();
            }

            fixChannelFolders( fileInfos, namingScheme );
            setImageMetadata( fileInfos, fileInfos.directory + fileInfos.channelFolders[ 0 ], namingScheme, fileLists[ 0 ] );
            populateFileList( fileInfos, namingScheme, fileLists );
        }
        else
        {
            if ( ! ( namingScheme.contains("<c>") && namingScheme.contains("<t>") ) )
            {
                Logger.warning("The pattern for multi-channel loading must"
                        + "contain <c> and <t> to match channels and time in the filenames.");
                fileInfos = null;
            }

            // replace shortcuts by actual regexp
            String namingSchemeRegExp = namingScheme.replace("<c>", "(?<C>.*)");
            namingSchemeRegExp = namingSchemeRegExp.replace("<t>", "(?<T>.*)");

            fileInfos.channelFolders = new String[]{""};

            HashSet<String> channelsHS = new HashSet();
            HashSet<String> timepointsHS = new HashSet();

            Pattern patternCT = Pattern.compile( namingSchemeRegExp );

            for ( String fileName : fileLists[0] )
            {
                Matcher matcherCT = patternCT.matcher(fileName);
                if (matcherCT.matches())
                {
                    channelsHS.add(matcherCT.group("C"));
                    timepointsHS.add(matcherCT.group("T"));
                }
            }

            // convert HashLists to sorted Lists
            List< String > channels = new ArrayList< >( channelsHS );
            Collections.sort( channels );
            fileInfos.nC = channels.size();

            List< String > timepoints = new ArrayList< >( timepointsHS );
            Collections.sort(timepoints);
            fileInfos.nT = timepoints.size() ;

            fixChannelFolders( fileInfos, namingScheme );
            setImageMetadata( fileInfos, fileInfos.directory + fileInfos.channelFolders[ 0 ], namingScheme, fileLists[ 0 ] );
            populateFileInfosFromChannelTimePattern( fileInfos, namingSchemeRegExp, fileLists[ 0 ], channels, timepoints );
        }

    }

    private static void populateFileList(
            FileInfos fileInfos,
            String namingScheme,
            String[][] fileLists )
    {
        fileInfos.ctzFileList = new String[ fileInfos.nC ][ fileInfos.nT ][ fileInfos.nZ ];

        if ( namingScheme.equals( FileInfos.TIFF_SLICES ) )
        {
            for ( int z = 0; z < fileInfos.nZ; z++ )
                fileInfos.ctzFileList[ 0 ][ 0 ][ z ] = fileLists[ 0 ][ z ];
        }
        else
        {
            for ( int c = 0; c < fileInfos.nC; c++ )
                for ( int t = 0; t < fileInfos.nT; t++ )
                    for ( int z = 0; z < fileInfos.nZ; z++ )
                        // all z with same file-name, because it is stacks
                        fileInfos.ctzFileList[ c ][ t ][ z ] = fileLists[ c ][ t ];
        }
    }

    private static void populateFileInfosFromChannelTimePattern(
            FileInfos fileInfos,
            String namingScheme,
            String[] fileList,
            List< String > channels,
            List< String > timepoints )
    {
        fileInfos.ctzFileList = new String[ fileInfos.nC ][ fileInfos.nT ][ fileInfos.nZ ];

        Pattern patternCT = Pattern.compile( namingScheme );

        for ( String fileName : fileList )
        {
            Matcher matcherCT = patternCT.matcher( fileName );
            if ( matcherCT.matches() ) {
                try {
                    int c = channels.indexOf( matcherCT.group("C") );
                    int t = timepoints.indexOf( matcherCT.group("T") );
                    for ( int z = 0; z < fileInfos.nZ; z++) {
                        fileInfos.ctzFileList[c][t][z] = fileName; // all z with same file-name, because it is stacks
                    }
                }
                catch (Exception e)
                {
                    Logger.error("The multi-channel loading did not match the filenames.\n" +
                            "Please change the pattern.\n\n" +
                            "The Java error message was:\n" +
                            e.toString());
                    fileInfos = null;
                }
            }
        }
    }

    private static String[][] getFilesInFolders(
            FileInfos infoSource,
            String directory,
            String namingScheme,
            String filterPattern )
    {
        if ( ! new File( directory ).exists() )
        {
            Logger.error("Directory not found: " + directory );
            return null;
        }

        String[][] fileLists;

        if ( namingScheme.equals( FileInfos.LOAD_CHANNELS_FROM_FOLDERS ) )
        {
            //
            // Check for sub-folders
            //
            Logger.info("Checking for sub-folders...");

            infoSource.channelFolders = getFoldersInFolder( directory, getFolderFilter( filterPattern ) );

            if ( infoSource.channelFolders != null )
            {
                fileLists = new String[infoSource.channelFolders.length][];
                for (int i = 0; i < infoSource.channelFolders.length; i++)
                {
                    fileLists[i] = getFilesInFolder(
                            directory + infoSource.channelFolders[ i ],
                            getFileFilter( filterPattern ));

                    if ( fileLists[i] == null )
                    {
                        Logger.error("No files found in folder: " + directory + infoSource.channelFolders[ i ]);
                        fileLists = null;
                        break;
                    }
                }
                Logger.info( "Found sub-folders => loading channels from sub-folders." );
            }
            else
            {
                Logger.error("No sub-folders found; " +
                        "please specify different options for loading " +
                        "the channels");
                fileLists = null;
            }

        }
        else
        {   //
            // Get files in main directory
            //
            Logger.info("Searching files in folder: " + directory);
            fileLists = new String[ 1 ][ ];
            fileLists[ 0 ] = getFilesInFolder( directory, filterPattern );
            Logger.info("Number of files in main folder matching the filter pattern: " + fileLists[0].length );

            if ( fileLists[0] == null || fileLists[0].length == 0 )
            {
                Logger.warning("No files matching this pattern were found: " + filterPattern);
                fileLists = null;
            }

        }
        return fileLists;
    }

    public static String getFolderFilter( String filterPattern )
    {
        String filter;
        if ( filterPattern != null )
        {
            final String[] split = filterPattern.split( Pattern.quote( File.separator ) );
            if ( split.length > 1 )
                filter = split[ 0 ];
            else
                filter = ".*";
        }
        else
        {
            filter = ".*";
        }
        return filter;
    }

    public static String getFileFilter( String filterPattern )
    {
        String filter;
        if ( filterPattern != null )
        {
            final String[] split = filterPattern.split( Pattern.quote( File.separator )  );
            if ( split.length > 1 )
                filter = split[ 1 ];
            else
                filter = split[ 0 ];
        }
        else
        {
            filter = ".*";
        }
        return filter;
    }

    public static String getFirstChannelDirectory( FileInfos infoSource, String directory )
    {
        String dataDirectory;
        if ( infoSource.channelFolders == null )
            dataDirectory = directory;
        else
            dataDirectory = directory + infoSource.channelFolders[ 0 ];

        return dataDirectory;
    }

    private static String[] sortAndFilterFileList(String[] rawlist, String filterPattern)
    {
        int count = 0;

        Pattern patternFilter = Pattern.compile( filterPattern );

        for (int i = 0; i < rawlist.length; i++)
        {
            String name = rawlist[i];
            if (! patternFilter.matcher( name ).matches() )
                rawlist[i] = null;
            else if (name.endsWith(".tif") || name.endsWith(".h5"))
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
        // TODO: can getting the file-list be faster?

//        Path folder = Paths.get( directory );
//        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder)) {
//            for (Path entry : stream) {
//                 Process the entry
//            }
//        } catch (IOException ex) {
//             An I/O problem has occurred
//        }

        String[] list = new File( directory ).list();

        if (list == null || list.length == 0)
            return null;

        //Logger.info( "Sorting and filtering file list..." );
        list = sortAndFilterFileList( list, filterPattern );

        if (list == null) return null;

        else return ( list );
    }

    private static String[] getFoldersInFolder( String directory, String folderFilter )
    {
        //info("# getFoldersInFolder: " + directory);

        String[] list = new File(directory).list(new FilenameFilter()
        {
            @Override
            public boolean accept(File current, String name)
            {
                boolean isValid = true;
                isValid &= new File(current, name).isDirectory();

                Pattern.compile(folderFilter).matcher(name);
                isValid &= Pattern.compile(folderFilter).matcher(name).matches();

                return isValid;
            }
        });

        if (list == null || list.length == 0)
            return null;

        Arrays.sort( list );

        return (list);

    }



}
