package de.embl.cba.bdp2.fileinfosource;


import ch.systemsx.cisd.hdf5.HDF5DataSetInformation;
import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5Reader;
import de.embl.cba.bdp2.FastTiffDecoder;
import de.embl.cba.bdp2.logging.IJLazySwingLogger;
import de.embl.cba.bdp2.logging.Logger;
import de.embl.cba.bdp2.utils.DimensionOrder;
import de.embl.cba.bdp2.utils.Utils;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class FileInfoSource {
    private final SerializableFileInfo[][][] infos;
    private final long[] dimensions;
    public int bitDepth;
    public int nC;
    public int nT;
    public int nX;
    public int nY;
    public int nZ;
    public String unit;
    public double[] voxelSize;
    public String fileType;
    public String h5DataSetName;
    public String[] channelFolders;
    public String[][][] ctzFileList;
    public final String directory;
    private final Logger logger = new IJLazySwingLogger();
    public double max_pixel_val;
    public double min_pixel_val;

    public FileInfoSource(
            String directory,
            String namingScheme,
            String filterPattern,
            String h5DataSetName){
        this.directory = directory;
        if ( namingScheme.contains("<Z") ){// TODO: change below logic somehow (maybe via GUI?)
            FileInfoSourceHelper.setFileSourceInfos(this, directory, namingScheme );
        }else{
            this.h5DataSetName = h5DataSetName;
            FileInfoSourceHelper.setFileSourceInfos(
                    this, directory, namingScheme, filterPattern );
        }
        infos = new SerializableFileInfo[nC][nT][nZ];
        dimensions = new long[ 5 ];
        dimensions[ DimensionOrder.X ] = nX;
        dimensions[ DimensionOrder.Y ] = nY;
        dimensions[ DimensionOrder.Z ] = nZ;
        dimensions[ DimensionOrder.C ] = nC;
        dimensions[ DimensionOrder.T ] = nT;
        if ( unit == null || Objects.equals(unit, "")) unit = "pixel";

        logger.info( this.toString() );
    }

    @Override
    public String toString()
    {
        String info = "";
        info += "Folder: " + directory + "\n";
        info += "FileType: " + fileType + "\n";
        info += "BitDepth: " + bitDepth + "\n";
        info += "nX: " + nX + "\n";
        info += "nY: " + nY + "\n";
        info += "nZ: " + nZ + "\n";
        info += "nC: " + nC + "\n";
        info += "nT: " + nT + "\n";
        info += "GB: " + getSizeInGB() + "\n";

        return info;
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
        } else if (fileType.equals(Utils.FileType.HDF5.toString())) {     //TODO: If TIFF and HDF5_STACKS have the same code then merge it after verification. -- ashis
            setInfosFromFile(channel, time, z, true);
        } else if (fileType.equals(Utils.FileType.SINGLE_PLANE_TIFF.toString())) {
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
                    logger.error("Error parsing: "+ directory + channelFolders[c] + "/" + ctzFileList[c][t][z]);
                    logger.warning("setInfoFromFile: " + e.toString());
                }

                if( info.length != nZ ) {// TODO : Handle exceptions properly --ashis
                    logger.error("Inconsistent number of z-planes in: "+ directory + channelFolders[c] + "/" + ctzFileList[c][t][z]);
                }

                // add missing information to first IFD
                info[0].fileName = ctzFileList[c][t][0];
                info[0].directory = channelFolders[c] + "/"; // relative path to main directory
                info[0].fileTypeString = fileType;

                infoCT = new SerializableFileInfo[nZ];
                for ( int z2 = 0; z2 < nZ; z2++ ) {
                    infoCT[z2] = new SerializableFileInfo( info[0] ); // copy constructor
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
                String dsTypeString = FileInfoSourceHDF5Helper.dsInfoToTypeString(dsInfo); //TODO: Check if OpenerExtension.hdf5InfoToString can be made public and called.

                if (dsTypeString.equals("int16") || dsTypeString.equals("uint16")){
                    bytesPerPixel = 2;
                }
                else if (dsTypeString.equals("int8") || dsTypeString.equals("uint8")){
                    bytesPerPixel = 1;
                }
                else{
                    logger.error( "Unsupported bit depth " + dsTypeString );
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
                logger.error("Error opening: " + directory + channelFolders[c] + "/" + ctzFileList[c][t][z]);
            }
        }

    }
}
