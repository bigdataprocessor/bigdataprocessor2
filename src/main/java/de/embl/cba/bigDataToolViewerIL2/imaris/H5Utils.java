package de.embl.cba.bigDataToolViewerIL2.imaris;

import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.HDF5Constants;
import ncsa.hdf.hdf5lib.exceptions.HDF5Exception;
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;

import java.io.File;
import java.util.ArrayList;

public abstract class H5Utils
{


    public static void writeIntegerAttribute( int dataset_id, String attrName, int[] attrValue ) throws HDF5Exception
    {

        long[] attrDims = { attrValue.length };

        // Create the data space for the attribute.
        int dataspace_id = H5.H5Screate_simple(attrDims.length, attrDims, null);

        // Create a dataset attribute.
//        int attribute_id = H5.H5Acreate(dataset_id, attrName,
//                HDF5Constants.H5T_STD_I32BE, dataspace_id,
//                HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT);

        int attribute_id = H5.H5Acreate(dataset_id, attrName,
                HDF5Constants.H5T_STD_I32BE, dataspace_id,
                HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT );

        // Write the attribute data.
        H5.H5Awrite(attribute_id, HDF5Constants.H5T_NATIVE_INT, attrValue);

        // Close the attribute.
        H5.H5Aclose(attribute_id);
    }

    public static void writeDoubleAttribute( int dataset_id, String attrName, double[] attrValue ) throws HDF5Exception
    {

        long[] attrDims = { attrValue.length };

        // Create the data space for the attribute.
        int dataspace_id = H5.H5Screate_simple(attrDims.length, attrDims, null);

        // Create a dataset attribute.
        int attribute_id = H5.H5Acreate(dataset_id, attrName,
                HDF5Constants.H5T_IEEE_F64BE, dataspace_id,
                HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT );

        // Write the attribute data.
        H5.H5Awrite(attribute_id, HDF5Constants.H5T_NATIVE_DOUBLE, attrValue);

        // Close the attribute.
        H5.H5Aclose(attribute_id);
    }

    public static void writeStringAttribute( int dataset_id, String attrName, String attrValue )
    {

        long[] attrDims = { attrValue.getBytes().length };

        // Create the data space for the attribute.
        int dataspace_id = H5.H5Screate_simple(attrDims.length, attrDims, null);

        // Create the data space for the attribute.
        //int dataspace_id = H5.H5Screate( HDF5Constants.H5S_SCALAR );

        // Create attribute type
        //int type_id = H5.H5Tcopy( HDF5Constants.H5T_C_S1 );
        //H5.H5Tset_size(type_id, attrValue.length());

        int type_id = HDF5Constants.H5T_C_S1;

        // Create a dataset attribute.
        int attribute_id = H5.H5Acreate( dataset_id, attrName,
                type_id, dataspace_id,
                HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT);

        // Write the attribute
        H5.H5Awrite(attribute_id, type_id, attrValue.getBytes());

        // Close the attribute.
        H5.H5Aclose(attribute_id);
    }

    public static void writeLongArrayListAs32IntArray( int group_id, ArrayList< long[] > list, String name ) throws
            HDF5Exception
    {

        int[][] data = new int[ list.size() ][ list.get(0).length ];

        for (int i = 0; i < list.size(); i++)
        {
            for (int j = 0; j < list.get(i).length; j++)
            {
                data[i][j] = (int) list.get(i)[j];
            }
        }

        long[] data_dims = { data.length, data[0].length };

        int dataspace_id = H5.H5Screate_simple( data_dims.length, data_dims, null );


        int dataset_id = H5.H5Dcreate(group_id, name,
                HDF5Constants.H5T_STD_I32LE, dataspace_id,
                HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT );

        H5.H5Dwrite( dataset_id, HDF5Constants.H5T_NATIVE_INT,
                HDF5Constants.H5S_ALL, HDF5Constants.H5S_ALL, HDF5Constants.H5P_DEFAULT, data );

        H5.H5Dclose(dataset_id);

        H5.H5Sclose(dataspace_id);

    }

    public static void writeLongArrayListAsDoubleArray( int group_id, ArrayList < long[] > list, String name ) throws HDF5Exception
    {

        double[] data = new double[list.size() * list.get(0).length];

        int p = 0;
        for (int i = 0; i < list.size(); i++)
        {
            for (int j = 0; j < list.get(i).length; j++)
            {
                data[ p++ ] = list.get(i)[j];
            }
        }

        long[] data_dims = { list.size(), list.get(0).length };

        int dataspace_id = H5.H5Screate_simple( data_dims.length, data_dims, null );

        int dataset_id = H5.H5Dcreate( group_id, name,
                HDF5Constants.H5T_IEEE_F64BE, dataspace_id,
                HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT );

        H5.H5Dwrite( dataset_id, HDF5Constants.H5T_NATIVE_DOUBLE,
                HDF5Constants.H5S_ALL, HDF5Constants.H5S_ALL, HDF5Constants.H5P_DEFAULT, data );

        H5.H5Dclose(dataset_id);

        H5.H5Sclose(dataspace_id);

    }

    public static void writeIntArrayListAsDoubleArray( int group_id, ArrayList < int[] > list, String name ) throws
            HDF5Exception
    {

        double[] data = new double[list.size() * list.get(0).length];

        int p = 0;
        for (int i = 0; i < list.size(); i++)
        {
            for (int j = 0; j < list.get(i).length; j++)
            {
                data[ p++ ] = list.get(i)[j];
            }
        }

        long[] data_dims = { list.size(), list.get(0).length };

        int dataspace_id = H5.H5Screate_simple( data_dims.length, data_dims, null );

        int dataset_id = H5.H5Dcreate( group_id, name,
                HDF5Constants.H5T_IEEE_F64BE, dataspace_id,
                HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT );

        H5.H5Dwrite( dataset_id, HDF5Constants.H5T_NATIVE_DOUBLE,
                HDF5Constants.H5S_ALL, HDF5Constants.H5S_ALL, HDF5Constants.H5P_DEFAULT, data );

        H5.H5Dclose(dataset_id);

        H5.H5Sclose(dataspace_id);

    }

    public static int createGroup( int file_id, String groupName ) throws HDF5LibraryException
    {
        int group_id;

        try
        {
            group_id = H5.H5Gopen( file_id, groupName, HDF5Constants.H5P_DEFAULT );

        }
        catch ( Exception e )
        {

            // create group (and intermediate groups)
            int gcpl_id = H5.H5Pcreate( HDF5Constants.H5P_LINK_CREATE );
            H5.H5Pset_create_intermediate_group( gcpl_id, true );
            group_id = H5.H5Gcreate(file_id, groupName, gcpl_id, HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT );
        }

        return ( group_id );

    }

    public static int openGroup( int file_id, String groupName )
    {
        int group_id;

        try
        {
            group_id = H5.H5Gopen( file_id, groupName, HDF5Constants.H5P_DEFAULT );
        }
        catch ( Exception e )
        {
            return ( -1 );
        }

        return ( group_id );

    }


    public static int openFile( String directory, String filename )
    {
        String path = directory + File.separator + filename;
        File file = new File( path );

        int file_id = -1;

        if ( file.exists() )
        {
            try
            {
                file_id = H5.H5Fopen( path, HDF5Constants.H5F_ACC_RDONLY, HDF5Constants.H5P_DEFAULT);
            }
            catch ( Exception e )
            {
                file_id = -1;
            }
        }

        return ( file_id );

    }


    public static int createFile( String directory, String filename )
    {
        String path = directory + File.separator + filename;

        File file = new File( path );

        if ( file.exists() )
        {
            file.delete();
        }

        int file_id = H5.H5Fcreate(path,
                HDF5Constants.H5F_ACC_TRUNC, HDF5Constants.H5P_DEFAULT,
                HDF5Constants.H5P_DEFAULT);

        return ( file_id );

    }

    public static long[] getDataDimensions( int object_id, String dataName )
    {
        try
        {
            int dataset_id = H5.H5Dopen( object_id, dataName, HDF5Constants.H5P_DEFAULT );
            int dataspace_id = H5.H5Dget_space( dataset_id );

            int ndims = H5.H5Sget_simple_extent_ndims( dataspace_id );

            long[] dimensions = new long[ ndims ];

            H5.H5Sget_simple_extent_dims( dataspace_id, dimensions, null );

            return ( dimensions );
        }
        catch ( Exception e )
        {
            return null;
        }
    }

    public static String readStringAttribute( int object_id,
                                              String objectName,
                                              String attributeName )
    {
        String attributeString = "";

        try
        {
            int attribute_id = H5.H5Aopen_by_name( object_id, objectName, attributeName,
                    HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT );

            if ( attribute_id < 0 )
            {
                return null;
            }

            int filetype_id = H5.H5Aget_type( attribute_id );
            int sdim = H5.H5Tget_size( filetype_id );
            sdim++; // Make room for null terminator
            int dataspace_id = H5.H5Aget_space( attribute_id );
            long[] dims = { 4 };
            H5.H5Sget_simple_extent_dims( dataspace_id, dims, null );
            byte[][] dset_data = new byte[ ( int ) dims[ 0 ] ][ ( int ) sdim ];
            StringBuffer[] str_data = new StringBuffer[ ( int ) dims[ 0 ] ];

            // Create the memory datatype.
            int memtype_id = H5.H5Tcopy( HDF5Constants.H5T_C_S1 );
            H5.H5Tset_size( memtype_id, sdim );

            // Read data.
            H5.H5Aread( attribute_id, memtype_id, dset_data );
            byte[] tempbuf = new byte[ ( int ) sdim ];
            for ( int indx = 0; indx < ( int ) dims[ 0 ]; indx++ )
            {
                for ( int jndx = 0; jndx < sdim; jndx++ )
                {
                    tempbuf[ jndx ] = dset_data[ indx ][ jndx ];
                }
                str_data[ indx ] = new StringBuffer( new String( tempbuf ) );
            }

            for ( int i = 0; i < str_data.length; i++ )
            {
                attributeString += str_data[ i ];
            }

            // remove null chars
            attributeString = attributeString.replace( "\u0000", "" );

        }
        catch ( Exception e )
        {
            attributeString = null;
        }


        return ( attributeString );


    }




}
