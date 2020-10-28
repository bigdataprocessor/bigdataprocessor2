package de.embl.cba.bdp2.open.fileseries;

import ch.systemsx.cisd.hdf5.HDF5DataSetInformation;
import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5Reader;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.imaris.ImarisUtils;
import ij.io.FileInfo;
import loci.common.DebugTools;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

public class FileInfos
{
    public static final int PROGRESS_UPDATE_MILLISECONDS = 100;
	public static final int TOTAL_AXES = 5;

	// TODO: below must be in synch with DimensionOrder
	public static final AxisType[] AXES_ORDER = { Axes.X, Axes.Y, Axes.Z, Axes.CHANNEL, Axes.TIME};
    public static final String[] HDF5_DATASET_NAMES = new String[] {
            "None", "Data", "Data111", "Data222", "Data444", // Luxendo
			ImarisUtils.RESOLUTION_LEVEL +"0/Data",
			ImarisUtils.RESOLUTION_LEVEL +"1/Data",
			ImarisUtils.RESOLUTION_LEVEL +"2/Data",
			ImarisUtils.RESOLUTION_LEVEL +"3/Data",
			"ITKImage/0/VoxelData" // Elastix
    };
    public SerializableFileInfo[][][] ctzFileInfos;
    public long[] dimensions;
    @NotNull
    private String namingScheme;
    private String filter;
    public int bitDepth;
    public int nC;
    public int nT;
    public int nX;
    public int nY;
    public int nZ;
    public String voxelUnit;
    public double[] voxelSize;
    public FileSeriesFileType fileType;
    public String h5DataSetName;
    public String[][][] ctzFiles;
    public String directory;
    public double max_pixel_val;
    public double min_pixel_val;
    public int compression;
    public int numTiffStrips;
    public String[] channelNames;
    private String[][] filesInFolders;


    public FileInfos(
            String directory,
            String regExp )
    {
        this( directory, regExp, regExp, null, null );
    }

    public FileInfos(
            String directory,
            String namingScheme,
            String filterPattern )
    {
        this( directory, namingScheme, filterPattern, null, null );
    }

    public FileInfos(
            String directory,
            String namingScheme,
            String filterPattern,
            String h5DataSetName )
    {
        this( directory, namingScheme, filterPattern, h5DataSetName, null);
    }

    // TODO: often (always the namingScheme and the filter are the same now??)
    public FileInfos(
            String directory,
            String namingScheme,
            String filter,
            String h5DataSetName,
            final String[] channelSubset
    )
    {
        fetchFileInfos( directory, namingScheme, filter, h5DataSetName, channelSubset );
    }

    public FileInfos(
            String directory,
            String namingScheme,
            String filter,
            String h5DataSetName,
            String[] channelSubset,
            String[][] filesInFolders
    )
    {
        this.filesInFolders = filesInFolders;
        fetchFileInfos( directory, namingScheme, filter, h5DataSetName, channelSubset );
    }

    private void fetchFileInfos( String directory, String namingScheme, String filter, String h5DataSetName, String[] channelSubset )
    {
        DebugTools.setRootLevel( "OFF" ); // Bio-Formats

        Logger.info( "Directory: " + directory );
        Logger.info( "Regular expression: " +  namingScheme );

        this.namingScheme = namingScheme;
        this.filter = filter;
        this.directory  = Utils.ensureDirectoryEndsWithFileSeparator( directory );
        this.h5DataSetName = h5DataSetName;

        adaptDirectorySeparatorToOperatingSystem();

        if ( filesInFolders == null )
            filesInFolders = FileInfosHelper.getFilesInFolders( this.directory, this.filter );

        FileInfosHelper.configureFileInfos5D( this, this.namingScheme, channelSubset, filesInFolders );

        Logger.info( this.toString() );
    }

    public String[][] getFilesInFolders()
    {
        return filesInFolders;
    }

    private void adaptDirectorySeparatorToOperatingSystem( )
    {
        namingScheme = namingScheme.replace( "/", Pattern.quote( File.separator ) );
    }

    @Override
    public String toString()
    {
        String info = "";
        info += "Folder: " + directory + "\n";
        info += "FileType: " + fileType + "\n";
        info += "BitDepth: " + bitDepth + "\n";

        if ( fileType.toString().toLowerCase().contains( "tif" ) )
        {
            info += "Tiff Compression: " + getCompressionString() + "\n";
            info += "Tiff Strips: " + numTiffStrips + "\n";
        }
        info += "nX: " + nX + "\n";
        info += "nY: " + nY + "\n";
        info += "nZ: " + nZ + "\n";
        info += "nC: " + nC + "\n";
        info += "nT: " + nT + "\n";
        info += "voxelUnit: " + voxelUnit + "\n";
        info += "voxelSizeX: " + voxelSize[0] + "\n";
        info += "voxelSizeY: " + voxelSize[1] + "\n";
        info += "voxelSizeZ: " + voxelSize[2] + "\n";
        info += "GB: " + getSizeInGB() + "\n";
        return info;
    }

    private String getCompressionString()
    {
        String compressionString;
        switch ( compression )
        {
            case FileInfo.ZIP: compressionString = "ZIP"; break;
            case FileInfo.LZW: compressionString = "LZW"; break;

            default: compressionString = "None";
        }
        return compressionString;
    }

    private double getSizeInGB()
    {
        long bytes = (long) bitDepth / 8 * nX * nY * nZ * nT * nC;
        final double sizeGB = ( double ) bytes / ( 1000_000_000L );
        return sizeGB;
    }

    public long[] getDimensions() {
        return dimensions;
    }


    public NativeType getType() {
        NativeType type;
        try {
            if (bitDepth == Byte.SIZE) {
                type = new UnsignedByteType();
            } else if (bitDepth == Short.SIZE) {
                type = new UnsignedShortType();
            } else if (bitDepth == Float.SIZE) {
                type = new FloatType();
            } else {
                throw new TypeNotPresentException("Data Type Not Found", new Throwable());
            }
        }catch (TypeNotPresentException typeException){
            throw typeException;
        }
    return  type;
    }


    public SerializableFileInfo[] getSerializableFileStackInfo( int channel, int time ) {
        int z = 0;
        if ( fileType.equals( FileSeriesFileType.TIFF_STACKS ) ) {
            setInfosFromFile(channel, time, z, true);
        }
        else if ( fileType.equals( FileSeriesFileType.HDF5 ) ) {
            setInfosFromFile(channel, time, z, true);
        }
        else if ( fileType.equals( FileSeriesFileType.TIFF_PLANES ) ) {
            int nZ = ctzFiles[channel][time].length;
            for (; z < nZ; ++z) {
                setInfosFromFile(channel, time, z, true);
            }
        }
        return ctzFileInfos[channel][time];
    }

    private void setInfosFromFile( final int c, final int t, final int z, boolean throwError )
    {
        SerializableFileInfo[] info = null;
        SerializableFileInfo[] infoCT;
        FastTiffDecoder ftd;
        File file = new File( directory, ctzFiles[c][t][z] );
        if ( file.exists() )
        {
            if ( fileType.equals( FileSeriesFileType.TIFF_STACKS ) )
            {
                ftd = new FastTiffDecoder( directory, ctzFiles[c][t][0] );
                try {
                    info = ftd.getTiffInfo();
                }
                catch (Exception e) {
                    Logger.error("Error parsing: " + file.getAbsolutePath() );
                    Logger.warn("setInfoFromFile: " + e.toString());
                }

                if( info.length != nZ ) {// TODO : Handle exceptions properly --ashis
                    Logger.error("Inconsistent number of z-planes in: " + file.getAbsolutePath());
                }

                // add missing information to first IFD
                info[0].fileName = getName( c, t, 0 );
                info[0].directory = getDirectory( c, t, 0 );
                info[0].fileTypeString = fileType.toString();

                infoCT = new SerializableFileInfo[nZ];
                for ( int z2 = 0; z2 < nZ; z2++ ) {
                    infoCT[z2] = new SerializableFileInfo( info[0] ); // copyVolumeRAI constructor
                    // adapt information related to where the data is stored in this plane
                    infoCT[z2].offset = info[z2].offset;
                    infoCT[z2].stripLengths = info[z2].stripLengths;
                    infoCT[z2].stripOffsets = info[z2].stripOffsets;
                    //infoCT[z].rowsPerStrip = info[z].rowsPerStrip; // only core for first IFD!
                }

                ctzFileInfos[c][t] = infoCT;
            }
            else if ( fileType.equals( FileSeriesFileType.HDF5 ) )
            {
                //
                // construct a FileInfoSer
                // todo: this could be much leaner
                // e.g. the nX, nY and bit depth
                //

                int bytesPerPixel = 0;

                IHDF5Reader reader = HDF5Factory.openForReading( file.getAbsolutePath() );
                HDF5DataSetInformation dsInfo = reader.getDataSetInformation( h5DataSetName );
                //String dsTypeString = OpenerExtension.hdf5InfoToString(dsInfo);
                String dsTypeString = FileInfosHDF5Helper.dsInfoToTypeString(dsInfo); //TODO: Check if OpenerExtension.hdf5InfoToString can be made public and called.

                if (dsTypeString.equals("int16") || dsTypeString.equals("uint16")){
                    bytesPerPixel = 2;
                }
                else if (dsTypeString.equals("int8") || dsTypeString.equals("uint8")){
                    bytesPerPixel = 1;
                }
                else{
                    Logger.error( "Unsupported bit depth " + dsTypeString );
                }

                infoCT = new SerializableFileInfo[nZ];
                for ( int z2 = 0; z2 < nZ; z2++)
                {
                    infoCT[z2] = new SerializableFileInfo();
                    infoCT[z2].fileName = getName( c, t, z2 );
                    infoCT[z2].directory = getDirectory( c, t, z2 );
                    infoCT[z2].width = nX;
                    infoCT[z2].height = nY;
                    infoCT[z2].bytesPerPixel = bytesPerPixel;
                    infoCT[z2].h5DataSet = h5DataSetName;
                    infoCT[z2].fileTypeString = fileType.toString();
                }
                ctzFileInfos[c][t] = infoCT;
            }
            else if ( fileType.equals( FileSeriesFileType.TIFF_PLANES))
            {
                ftd = new FastTiffDecoder(directory, ctzFiles[c][t][z]);
                try{
                    ctzFileInfos[c][t][z] = ftd.getTiffInfo()[0];
                }
                catch ( IOException e ){// TODO : Handle exceptions properly --ashis
                    System.out.print( e.toString() );
                }
                ctzFileInfos[c][t][z].fileName = getName( c, t, z );
                ctzFileInfos[c][t][z].directory = getDirectory( c, t, z );
                ctzFileInfos[c][t][z].fileTypeString = fileType.toString();
            }
        }
        else
        {
            Logger.error( "File does not exist [ c, t, z ] : " + c + ", " + t + ", " + z+
                            "\npath:" + file.getAbsolutePath()  +
                            "\ndirectory: " + directory +
                            "\nfile: " + ctzFiles[c][t][z] );
            throw new UnsupportedOperationException( "File does not exist " + file.getAbsolutePath() );
        }

    }

    private String getName( int c, int t, int z )
    {
        return new File( ctzFiles[ c ][ t ][ z ] ).getName();
    }

    private String getDirectory( int c, int t, int z )
    {
        final String parent = new File( ctzFiles[ c ][ t ][ z ] ).getParent();

        if ( parent == null )
            return "";
        else
            return parent;
    }
}
