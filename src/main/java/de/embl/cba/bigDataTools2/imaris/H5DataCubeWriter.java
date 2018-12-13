package de.embl.cba.bigDataTools2.imaris;

import de.embl.cba.bigDataTools2.utils.Utils;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.process.ImageStatistics;
import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.HDF5Constants;
import ncsa.hdf.hdf5lib.exceptions.HDF5Exception;
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;
import static de.embl.cba.bigDataTools2.imaris.H5Utils.writeDoubleAttribute;
import static de.embl.cba.bigDataTools2.imaris.H5Utils.writeStringAttribute;
import static de.embl.cba.bigDataTools2.imaris.ImarisUtils.*;

public class H5DataCubeWriter
{

    int file_id;
    int memory_type;
    int file_type;

    public void writeImarisCompatibleResolutionPyramid( ImagePlus imp, ImarisDataSet idp, int c, int t ) throws HDF5LibraryException
    {

        file_id = createFile( idp.getDataSetDirectory( c, t, 0 ), idp.getDataSetFilename( c, t, 0 ) );

        setMemoryTypeAndFileType( imp );

        ImagePlus impResolutionLevel = imp;

        for ( int resolution = 0; resolution < idp.getDimensions().size(); resolution++ )
        {
            if ( resolution > 0 )
            {
                // bin further
                impResolutionLevel = Utils.bin( impResolutionLevel, idp.getRelativeBinnings().get( resolution ), "binned", "AVERAGE" );
            }

            writeDataCubeAndAttributes( impResolutionLevel, RESOLUTION_LEVEL + resolution, idp.getDimensions().get( resolution ), idp.getChunks().get( resolution ) );

            writeHistogramAndAttributes( impResolutionLevel, RESOLUTION_LEVEL + resolution );
        }

        H5.H5Fclose( file_id );
    }

    private void setMemoryTypeAndFileType( ImagePlus imp )
    {

        if ( imp.getBitDepth() == 8 )
        {
            memory_type = HDF5Constants.H5T_NATIVE_UCHAR;
            file_type = HDF5Constants.H5T_STD_U8BE;
        }
        else if ( imp.getBitDepth() == 16 )
        {
            memory_type = HDF5Constants.H5T_NATIVE_USHORT;
            file_type = HDF5Constants.H5T_STD_U16BE;
        }
        else if ( imp.getBitDepth() == 32 )
        {
            memory_type = HDF5Constants.H5T_NATIVE_FLOAT;
            file_type = HDF5Constants.H5T_IEEE_F32BE;
        }
        else
        {
            IJ.showMessage( "Image data type is not supported, " +
                    "only 8-bit, 16-bit and 32-bit floating point are possible." );
        }
    }

    private void writeDataCubeAndAttributes(ImagePlus imp, String group, long[] dimensionXYZ, long[] chunkXYZ ) throws HDF5Exception
    {

        // change dimension order to fit hdf5

        long[] dimension = new long[]{
                dimensionXYZ[ 2 ],
                dimensionXYZ[ 1 ],
                dimensionXYZ[ 0 ] };

        long[] chunk = new long[]{
                chunkXYZ[ 2 ],
                chunkXYZ[ 1 ],
                chunkXYZ[ 0 ] };


        int group_id = H5Utils.createGroup( file_id, group );

        int dataspace_id = H5.H5Screate_simple( dimension.length, dimension, null );

        // create "dataset creation property list" (dcpl)
        int dcpl_id = H5.H5Pcreate( HDF5Constants.H5P_DATASET_CREATE );

        // chunks
        H5.H5Pset_chunk( dcpl_id, chunk.length, chunk );

        // compression
        H5.H5Pset_deflate( dcpl_id, 2);

        // create dataset
        int dataset_id = -1;
        try
        {
            dataset_id = H5.H5Dcreate(
                    group_id,
                    DATA,
                    file_type,
                    dataspace_id,
                    HDF5Constants.H5P_DEFAULT,
                    dcpl_id,
                    HDF5Constants.H5P_DEFAULT );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }

        writeImagePlusData( dataset_id, imp );

        // Attributes
        writeSizeAttributes( group_id, dimensionXYZ );
        writeChunkAttributes( group_id, chunkXYZ  );
        writeCalibrationAttribute( dataset_id, imp.getCalibration() );

        H5.H5Sclose( dataspace_id );
        H5.H5Dclose( dataset_id );
        H5.H5Pclose( dcpl_id );
        H5.H5Gclose( group_id );

    }

    private void writeImagePlusData(  int dataset_id, ImagePlus imp ) throws HDF5Exception
    {

        int dataspace_id;

        if( imp.getBitDepth() == 8 )
        {
            byte[][] data = getByteData( imp, 0, 0 );

            long numVoxels = data.length * data[0].length;
            boolean javaIndexingIssue = ( numVoxels > Integer.MAX_VALUE - 100 );

            if ( ! javaIndexingIssue )
            {
                H5.H5Dwrite( dataset_id,
                        memory_type,
                        HDF5Constants.H5S_ALL,
                        HDF5Constants.H5S_ALL,
                        HDF5Constants.H5P_DEFAULT,
                        data );
            }
            else
            {
                System.out.println( "Very large data set => saving 8-bit hdf5 plane-wise to circumvent java indexing issues." );

                for ( int i = 0; i < imp.getNSlices(); ++i )
                {
                    byte[] slice = data[ i ];

                    long[] start = new long[]{ i, 0, 0 };
                    long[] count = new long[]{ 1, imp.getHeight(), imp.getWidth()};

                    dataspace_id = H5.H5Dget_space( dataset_id );

                    // Select hyperslab in file dataspace
                    H5.H5Sselect_hyperslab( dataspace_id,
                            HDF5Constants.H5S_SELECT_SET,
                            start,
                            null,
                            count,
                            null
                    );

                    // Create memspace
                    int memspace = H5.H5Screate_simple( 1, new long[]{slice.length}, null );

                    // write
                    H5.H5Dwrite( dataset_id,
                            memory_type,
                            memspace,
                            dataspace_id,
                            HDF5Constants.H5P_DEFAULT,
                            slice );

                }

            }
        }
        else if( imp.getBitDepth() == 16 )
        {
            short[][] data = getShortData( imp, 0, 0 );

            long numVoxels = data.length * data[ 0 ].length;
            boolean javaIndexingIssue = ( numVoxels > ( Integer.MAX_VALUE - 100 ) );

            if ( ! javaIndexingIssue )
            {
                H5.H5Dwrite( dataset_id,
                        memory_type,
                        HDF5Constants.H5S_ALL,
                        HDF5Constants.H5S_ALL,
                        HDF5Constants.H5P_DEFAULT,
                        data );
            }
            else
            {
                for ( int i = 0; i < imp.getNSlices(); ++i )
                {
                    short[] slice = data[ i ];

                    long[] start = new long[]{ i, 0, 0 };
                    long[] count = new long[]{ 1, imp.getHeight(), imp.getWidth()};

                    dataspace_id = H5.H5Dget_space( dataset_id );

                    // Select hyperslab in file dataspace
                    H5.H5Sselect_hyperslab( dataspace_id,
                            HDF5Constants.H5S_SELECT_SET,
                            start,
                            null,
                            count,
                            null
                    );

                    // Create memspace
                    int memspace = H5.H5Screate_simple( 1, new long[]{slice.length}, null );

                    // write
                    H5.H5Dwrite( dataset_id,
                            memory_type,
                            memspace,
                            dataspace_id,
                            HDF5Constants.H5P_DEFAULT,
                            slice );

                }

            }

        }
        else if( imp.getBitDepth()==32 )
        {
            float[][] data = getFloatData( imp, 0, 0 );

            H5.H5Dwrite( dataset_id,
                    memory_type,
                    HDF5Constants.H5S_ALL,
                    HDF5Constants.H5S_ALL,
                    HDF5Constants.H5P_DEFAULT,
                    data );
        }
        else
        {
            IJ.showMessage( "Image data type is not supported, " +
                    "only 8-bit, 16-bit and 32-bit are possible." );
        }

    }

    private void writeSizeAttributes( int group_id, long[] dimension )
    {
        for ( int d = 0; d < 3; ++d )
        {
            writeStringAttribute( group_id,
                    IMAGE_SIZE + XYZ[d],
                    String.valueOf( dimension[d]) );
        }
    }

    private void writeChunkAttributes( int group_id, long[] chunks )
    {
        for ( int d = 0; d < 3; ++d )
        {
            writeStringAttribute( group_id,
                    IMAGE_BLOCK_SIZE + XYZ[d],
                    String.valueOf( chunks[d]) );
        }
    }

    private void writeCalibrationAttribute( int object_id, Calibration calibration )
    {

        double[] calibrationXYZ = new double[]
                {
                        calibration.pixelWidth,
                        calibration.pixelHeight,
                        calibration.pixelDepth
                };

        writeDoubleAttribute(  object_id, "element_size_um", calibrationXYZ );

    }

    private void writeHistogramAndAttributes(ImagePlus imp, String group )
    {
        int group_id = H5Utils.createGroup( file_id, group );

        ImageStatistics imageStatistics = imp.getStatistics();

        /*
        imaris expects 64bit unsigned int values:
        - http://open.bitplane.com/Default.aspx?tabid=268
        thus, we are using as memory type: H5T_NATIVE_ULLONG
        and as the corresponding dataset type: H5T_STD_U64LE
        - https://support.hdfgroup.org/HDF5/release/dttable.html
        */
        long[] histogram = new long[ imageStatistics.histogram.length ];
        for ( int i = 0; i < imageStatistics.histogram.length; ++i )
        {
            histogram[i] = imageStatistics.histogram[i];
        }

        long[] histo_dims = { histogram.length };

        int histo_dataspace_id = H5.H5Screate_simple(
                histo_dims.length, histo_dims, null);

        int histo_dataset_id = H5.H5Dcreate( group_id, HISTOGRAM,
                HDF5Constants.H5T_STD_U64LE, histo_dataspace_id,
                HDF5Constants.H5P_DEFAULT,
                HDF5Constants.H5P_DEFAULT,
                HDF5Constants.H5P_DEFAULT);

        H5.H5Dwrite(histo_dataset_id,
                HDF5Constants.H5T_NATIVE_ULLONG,
                HDF5Constants.H5S_ALL, HDF5Constants.H5S_ALL,
                HDF5Constants.H5P_DEFAULT, histogram);


        writeStringAttribute( group_id,
                HISTOGRAM + "Min",
                String.valueOf( imageStatistics.min ) );

        writeStringAttribute( group_id,
                HISTOGRAM + "Max",
                String.valueOf( imageStatistics.max ) );

        H5.H5Dclose( histo_dataset_id );
        H5.H5Sclose( histo_dataspace_id );
        H5.H5Gclose( group_id );

    }

    private int createFile( String directory, String filename )
    {
        return ( H5Utils.createFile( directory, filename  ) );
    }

    private byte[][] getByteData(ImagePlus imp, int c, int t )
    {
        ImageStack stack = imp.getStack();

        int[] size = new int[]{
                imp.getWidth(),
                imp.getHeight(),
                imp.getNSlices()
        };

        byte[][] data = new byte[ size[2] ] [ size[1] * size[0] ];

        for (int z = 0; z < imp.getNSlices(); z++)
        {
            int n = imp.getStackIndex(c+1, z+1, t+1);
            data[z] = (byte[]) stack.getProcessor(n).getPixels();

            //System.arraycopy( stack.getProcessor(n).getPixels(), 0, data[z],
            //       0, size[0] * size[1] );

        }

        return ( data );

    }

    private short[][] getShortData(ImagePlus imp, int c, int t)
    {
        ImageStack stack = imp.getStack();

        int[] size = new int[]{
                imp.getWidth(),
                imp.getHeight(),
                imp.getNSlices()
        };

        short[][] data = new short[ size[2] ] [ size[1] * size[0] ];

        for (int z = 0; z < imp.getNSlices(); z++)
        {
            int n = imp.getStackIndex(c+1, z+1, t+1);
            data[z] = (short[]) stack.getProcessor(n).getPixels();

            //System.arraycopy( stack.getProcessor(n).getPixels(), 0, data[z],
            //        0, size[0] * size[1] );

        }

        return ( data );

    }

    private float[][] getFloatData(ImagePlus imp, int c, int t)
    {
        ImageStack stack = imp.getStack();

        int[] size = new int[]{
                imp.getWidth(),
                imp.getHeight(),
                imp.getNSlices()
        };

        float[][] data = new float[ size[2] ] [ size[1] * size[0] ];

        for (int z = 0; z < imp.getNSlices(); z++)
        {
            int n = imp.getStackIndex(c+1, z+1, t+1);
            data[z] = (float[]) stack.getProcessor(n).getPixels();

            //System.arraycopy( stack.getProcessor(n).getPixels(), 0, data[z],
            //        0, size[0] * size[1] );

        }
        return ( data );

    }


}
