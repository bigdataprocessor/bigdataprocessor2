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

    public static boolean setFileSourceInfos(
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
                            setImageDataInfoFromTiff(infoSource,directory + infoSource.channelFolders[c - ctzMin[0]],fileName);

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

    public static void setImageDataInfoFromTiff(
            FileInfos infoSource,
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
            infoSource.nZ = info[0].nImages;
            info[0].nImages = 1;
        }
        else
        {
            infoSource.nZ = info.length;
        }

        infoSource.nX = info[0].width;
        infoSource.nY = info[0].height;
        infoSource.bitDepth = info[0].bytesPerPixel * 8;

        infoSource.voxelSpacing = new double[]{
                info[0].pixelWidth,
                info[0].pixelHeight,
                info[0].pixelDepth };

        infoSource.voxelUnit = info[0].unit;

    }


    public static boolean setFileSourceInfos(
            FileInfos infoSource,
            String directory,
            String namingScheme,
            String filterPattern) { //previously, setAllInfosByParsingFilesAndFolders

        String[][] fileLists;
        int t = 0, z = 0, c = 0;
        String fileType = "not determined";
        SerializableFileInfo[] info;
        SerializableFileInfo fi0;
        List<String> channels = null, timepoints = null;

        int nC = 0, nT = 0, nZ = 0, nX = 0, nY = 0, bitDepth = 16;

        if ( namingScheme.equals( FileInfos.LOAD_CHANNELS_FROM_FOLDERS) )
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
                        return false;
                    }
                }
                Logger.info( "Found sub-folders => loading channels from sub-folders." );
            }
            else
            {
                Logger.error("No sub-folders found; " +
                        "please specify a different options for loading " +
                        "the channels");
                return false;
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
                return false;
            }

        }

        if ( namingScheme.equals( FileInfos.LEICA_SINGLE_TIFF ) )
        {
            infoSource.fileType = Utils.FileType.SINGLE_PLANE_TIFF.toString();

            String dataDirectory = getFirstChannelDirectory( infoSource, directory );

            boolean success = FileInfosLeicaHelper.initLeicaSinglePlaneTiffData(
                    infoSource, dataDirectory, filterPattern, fileLists[ 0 ], t, z, nC, nZ);

            if ( ! success ) return false;
        }
        else // tiff stacks or h5 stacks
        {
            boolean hasCTPattern = false;

            if ( namingScheme.equals( FileInfos.LOAD_CHANNELS_FROM_FOLDERS) )
            {
                nC = infoSource.channelFolders.length;
                nT = fileLists[0].length;
            }
            else if ( namingScheme.equalsIgnoreCase( FileInfos.SINGLE_CHANNEL_TIMELAPSE ) )
            {
                nC = 1;
                nT = fileLists[0].length;
            }
            else if ( namingScheme.equals( FileInfos.EM_TIFF_SLICES ) )
            {
                nC = 1;
                nT = 1;
                infoSource.fileType = Utils.FileType.SINGLE_PLANE_TIFF.toString();
            }
            else
            {

                hasCTPattern = true;

                if (!(namingScheme.contains("<c>") && namingScheme.contains("<t>")))
                {
                    //IJ.showMessage("The pattern for multi-channel loading must" + "contain <c> and <t> to match channels and time in the filenames.");
                    Logger.warning("The pattern for multi-channel loading must" + "contain <c> and <t> to match channels and time in the filenames.");
                    return false;
                }

                // replace shortcuts by actual regexp
                namingScheme = namingScheme.replace("<c>", "(?<C>.*)");
                namingScheme = namingScheme.replace("<t>", "(?<T>.*)");

                infoSource.channelFolders = new String[]{""};

                HashSet<String> channelsHS = new HashSet();
                HashSet<String> timepointsHS = new HashSet();

                Pattern patternCT = Pattern.compile( namingScheme );

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
                channels = new ArrayList< >( channelsHS );
                Collections.sort( channels );
                nC = channels.size();

                timepoints = new ArrayList< >( timepointsHS );
                Collections.sort(timepoints);
                nT = timepoints.size();
            }

            //
            // Create dummy channel folders, if no real ones exist
            //
            if ( ! namingScheme.equals( FileInfos.LOAD_CHANNELS_FROM_FOLDERS) )
            {
                infoSource.channelFolders = new String[nC];
                for (int ic = 0; ic < nC; ic++) infoSource.channelFolders[ic] = "";
            }


            // read nX,nY,nZ and bitdepth from first image
            //

            if ( fileLists[0][0].endsWith(".tif") )
            {
                setImageDataInfoFromTiff( infoSource,
                        directory + infoSource.channelFolders[0], fileLists[0][0] );

                if ( namingScheme.equals( FileInfos.EM_TIFF_SLICES ) )
                {
                    infoSource.fileType = Utils.FileType.SINGLE_PLANE_TIFF.toString();
                    infoSource.nZ = fileLists[ 0 ].length;
                }
                else
                {
                    infoSource.fileType = Utils.FileType.TIFF_STACKS.toString();
                }
            }
            else if ( fileLists[0][0].endsWith(".h5") )
            {
                FileInfosHDF5Helper.setImageDataInfoFromH5(
                        infoSource,
                        directory + infoSource.channelFolders[0],
                        fileLists[0][0]);
                infoSource.fileType = Utils.FileType.HDF5.toString();
            }
            else
            {
                Logger.error("Unsupported file type: " + fileLists[0][0]);
                return false;
            }

            infoSource.nT = nT;
            infoSource.nC = nC;

            //
            // getCachedCellImg the final file list
            //

            infoSource.ctzFileList = new String[ infoSource.nC ][ infoSource.nT ][ infoSource.nZ ];

            if ( hasCTPattern )
            {

                // no sub-folders
                // channel and t determined by pattern matching

                Pattern patternCT = Pattern.compile( namingScheme );

                for ( String fileName : fileLists[0] )
                {

                    Matcher matcherCT = patternCT.matcher( fileName );
                    if (matcherCT.matches()) {
                        try {
                            c = channels.indexOf(matcherCT.group("C"));
                            t = timepoints.indexOf(matcherCT.group("T"));
                        } catch (Exception e) {
                            Logger.error("The multi-channel loading did not match the filenames.\n" +
                                    "Please change the pattern.\n\n" +
                                    "The Java error message was:\n" +
                                    e.toString());
                            return false;
                        }

                        for (z = 0; z < infoSource.nZ; z++) {
                            infoSource.ctzFileList[c][t][z] = fileName; // all z with same file-name, because it is stacks
                        }
                    }

                }

            }
            else
            {
                if ( namingScheme.equals( FileInfos.EM_TIFF_SLICES ) )
                {
                    for ( z = 0; z < infoSource.nZ; z++ )
                        infoSource.ctzFileList[ 0 ][ 0 ][ z ] = fileLists[ 0 ][ z ];
                }
                else
                {
                    for ( c = 0; c < infoSource.nC; c++ )
                    {
                        for ( t = 0; t < infoSource.nT; t++ )
                        {
                            for ( z = 0; z < infoSource.nZ; z++ )
                            {
                                infoSource.ctzFileList[ c ][ t ][ z ] = fileLists[ c ][ t ]; // all z with same file-name, because it is stacks
                            }
                        }
                    }
                }

            }

        }

        return true;

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

        Pattern patternFilter = Pattern.compile(filterPattern);

        for (int i = 0; i < rawlist.length; i++)
        {
            String name = rawlist[i];
            if (!patternFilter.matcher(name).matches())
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
