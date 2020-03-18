package de.embl.cba.bdp2.load.files;

import ch.systemsx.cisd.hdf5.HDF5DataSetInformation;
import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5Reader;
import de.embl.cba.bdp2.load.FastTiffDecoder;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.utils.DimensionOrder;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.imaris.ImarisUtils;
import ij.io.FileInfo;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class FileInfos
{
	public static final String LOAD_CHANNELS_FROM_FOLDERS = "Channels from Subfolders";
	public static final String TIFF_SLICES = "Tiff Slices";
	public static final String LEICA_LIGHT_SHEET_TIFF = "Leica Light Sheet Tiff";
	public static final String SINGLE_CHANNEL_TIMELAPSE = "Single Channel Movie";
    public static final String PATTERN_LUXENDO = "^((?!_All).)*.h5$";
    public static final String PATTERN_LUXENDO_LEFT_CAM = "Cam_Left_(\\d)+.h5$";
    public static final String PATTERN_LUXENDO_RIGHT_CAM = "Cam_Right_(\\d)+.h5$";
    public static final String PATTERN_LUXENDO_LONG_CAM = "Cam_long_(\\d)+.h5$";
    public static final String PATTERN_LUXENDO_SHORT_CAM = "Cam_short_(\\d)+.h5$";
    public static final String PATTERN_LUXENDO_2 = "Cam_.*_(\\d)+.h5$";
    public static final String PATTERN_ALL= ".*";

    public static final String PATTERN_1= "<Z0000-0009>.tif"; // make pattern class
	public static final String PATTERN_2= ".*--C<c>--T<t>.tif";
	public static final String PATTERN_3= ".*--C<c>--T<t>.h5";
	public static final String PATTERN_4= ".*_C<c>_T<t>.tif";
	public static final String PATTERN_5= ".*--t<t>--Z<z>--C<c>.tif";
	public static final String PATTERN_6= "Cam_<c>_<t>.h5";
	public static final int PROGRESS_UPDATE_MILLISECONDS = 100;
	public static final int TOTAL_AXES = 5;
	public static final AxisType[] AXES_ORDER = { Axes.X, Axes.Y, Axes.Z, Axes.CHANNEL, Axes.TIME};
	public static final int MAX_ALLOWED_IMAGE_DIMS = AXES_ORDER.length;
	public static final String UNSIGNED_BYTE_VIEW_NAME = "8-bit converted";
	public static final String TRACKED_IMAGE_NAME = "tracked";
	public static final String IMAGE_NAME = "image";
	public static final long HDF5_BLOCK_SIZE_2D = 128;
	public static final long HDF5_BLOCK_SIZE_3D = 32;
	public static final String[] POSSIBLE_HDF5_DATASETNAMES = new String[] {"None",
			"Data","Data111",
			ImarisUtils.RESOLUTION_LEVEL +"0/Data",
			ImarisUtils.RESOLUTION_LEVEL +"1/Data",
			ImarisUtils.RESOLUTION_LEVEL +"2/Data",
			ImarisUtils.RESOLUTION_LEVEL +"3/Data",
			"ITKImage/0/VoxelData", "Data222", "Data444"};
	private final SerializableFileInfo[][][] infos;
    private final long[] dimensions;
    public int bitDepth;
    public int nC;
    public int nT;
    public int nX;
    public int nY;
    public int nZ;
    public String voxelUnit;
    public double[] voxelSpacing;
    public String fileType;
    public String h5DataSetName;
    public String[] channelFolders;
    public String[][][] ctzFileList;
    public final String directory;
    public double max_pixel_val;
    public double min_pixel_val;
    public int compression;
    public int numTiffStrips;

    public FileInfos(
            String directory,
            String loadingScheme,
            String filterPattern)
    {
        this( directory, loadingScheme, filterPattern, null);
    }

    public FileInfos(
            String directory,
            String loadingScheme,
            String filterPattern,
            String h5DataSetName)
    {
        Logger.info( "" );
        Logger.info( "" );
        Logger.info( "Configuring file load from: " + directory );
        directory = Utils.fixDirectoryFormat( directory );

        this.directory = directory;

        if ( loadingScheme.contains("<Z") )
        {
            FileInfosHelper.setFileInfos(this, directory, loadingScheme );
        }
        else
        {
            this.h5DataSetName = h5DataSetName;
            FileInfosHelper.setFileInfos(
                    this,
                    directory,
                    loadingScheme,
                    filterPattern );
        }

        infos = new SerializableFileInfo[nC][nT][nZ];
        dimensions = new long[ 5 ];
        dimensions[ DimensionOrder.X ] = nX;
        dimensions[ DimensionOrder.Y ] = nY;
        dimensions[ DimensionOrder.Z ] = nZ;
        dimensions[ DimensionOrder.C ] = nC;
        dimensions[ DimensionOrder.T ] = nT;

        if ( voxelUnit == null || Objects.equals( voxelUnit, "")) voxelUnit = "pixel";

        Logger.info( this.toString() );
    }

    @Override
    public String toString()
    {
        String info = "";
        info += "Folder: " + directory + "\n";
        info += "FileType: " + fileType + "\n";
        info += "BitDepth: " + bitDepth + "\n";

        if ( fileType.toLowerCase().contains( "tif" ) )
        {
            info += "Tiff Compression: " + getCompressionString() + "\n";
            info += "Tiff Strips: " + numTiffStrips + "\n";
        }
        info += "nX: " + nX + "\n";
        info += "nY: " + nY + "\n";
        info += "nZ: " + nZ + "\n";
        info += "nC: " + nC + "\n";
        info += "nT: " + nT + "\n";
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
        if (fileType.equals(Utils.FileType.TIFF_STACKS.toString())) {
            setInfosFromFile(channel, time, z, true);
        }
        else if (fileType.equals(Utils.FileType.HDF5.toString())) {     //TODO: If TIFF and HDF5_STACKS have the same code then merge it after verification. -- ashis
            setInfosFromFile(channel, time, z, true);
        }
        else if (fileType.equals(Utils.FileType.SINGLE_PLANE_TIFF.toString())) {
            int nZ = ctzFileList[channel][time].length;
            for (; z < nZ; ++z) {
                setInfosFromFile(channel, time, z, true);
            }
        }
        return infos[channel][time];
    }

    private void setInfosFromFile( final int c, final int t, final int z, boolean throwError ) {
        SerializableFileInfo[] info = null;
        SerializableFileInfo[] infoCT;
        FastTiffDecoder ftd;
        File f = new File(directory + channelFolders[c] + "/" + ctzFileList[c][t][z]);
        if ( f.exists() )
        {
            if ( fileType.equals(Utils.FileType.TIFF_STACKS.toString() ) ) {
                ftd = new FastTiffDecoder(directory + channelFolders[c], ctzFileList[c][t][0]);
                try {
                    info = ftd.getTiffInfo();
                }
                catch (Exception e) {
                    Logger.error("Error parsing: "+ directory + channelFolders[c] + "/" + ctzFileList[c][t][z]);
                    Logger.warning("setInfoFromFile: " + e.toString());
                }

                if( info.length != nZ ) {// TODO : Handle exceptions properly --ashis
                    Logger.error("Inconsistent number of z-planes in: "+ directory + channelFolders[c] + "/" + ctzFileList[c][t][z]);
                }

                // add missing information to first IFD
                info[0].fileName = ctzFileList[c][t][0];
                info[0].directory = channelFolders[c] + "/"; // relative path to main directory
                info[0].fileTypeString = fileType;

                infoCT = new SerializableFileInfo[nZ];
                for ( int z2 = 0; z2 < nZ; z2++ ) {
                    infoCT[z2] = new SerializableFileInfo( info[0] ); // copyVolumeRAI constructor
                    // adapt information related to where the data is stored in this plane
                    infoCT[z2].offset = info[z2].offset;
                    infoCT[z2].stripLengths = info[z2].stripLengths;
                    infoCT[z2].stripOffsets = info[z2].stripOffsets;
                    //infoCT[z].rowsPerStrip = info[z].rowsPerStrip; // only read for first IFD!
                }

                infos[c][t] = infoCT;
            }
            else if ( fileType.equals(Utils.FileType.HDF5.toString() ) ) {
                //
                // construct a FileInfoSer
                // todo: this could be much leaner
                // e.g. the nX, nY and bit depth
                //

                int bytesPerPixel = 0;

                IHDF5Reader reader = HDF5Factory.openForReading(directory + channelFolders[c] + "/" + ctzFileList[c][t][0]);
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
                for ( int z2 = 0; z2 < nZ; z2++){
                    infoCT[z2] = new SerializableFileInfo();
                    infoCT[z2].fileName = ctzFileList[c][t][z2];
                    infoCT[z2].directory = channelFolders[c] + "/";
                    infoCT[z2].width = nX;
                    infoCT[z2].height = nY;
                    infoCT[z2].bytesPerPixel = bytesPerPixel; // todo: how to get the bit-depth from the info?
                    infoCT[z2].h5DataSet = h5DataSetName;
                    infoCT[z2].fileTypeString = fileType;
                }
                infos[c][t] = infoCT;
            }
            else if ( fileType.equals(Utils.FileType.SINGLE_PLANE_TIFF.toString())){
                ftd = new FastTiffDecoder(directory + channelFolders[c], ctzFileList[c][t][z]);
                try{
                    infos[c][t][z] = ftd.getTiffInfo()[0];
                }
                catch ( IOException e ){// TODO : Handle exceptions properly --ashis
                    System.out.print( e.toString() );
                }
                infos[c][t][z].directory = channelFolders[c] + "/"; // relative path to main directory
                infos[c][t][z].fileName = ctzFileList[c][t][z];
                infos[c][t][z].fileTypeString = fileType;
            }
        }
        else
        {
            if( throwError ){// TODO : Handle exceptions properly --ashis
                Logger.error("Error opening: " + directory + channelFolders[c] + "/" + ctzFileList[c][t][z]);
            }
        }

    }
}
