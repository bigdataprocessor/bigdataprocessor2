
package de.embl.cba.bigDataTools2.imaris;

import de.embl.cba.bigDataTools2.utils.Utils;
import ij.IJ;
import ij.ImagePlus;
import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.HDF5Constants;
import net.imglib2.RealInterval;
import org.scijava.log.LogService;

import java.io.File;
import java.util.ArrayList;

public class ImarisWriter {

    // TODO: make non abstract with Constructor

    final ImagePlus imp;
    final String directory;
    final String name;
    int[] binning;
    ArrayList< String > channelNames;
    LogService logService;

    public ImarisWriter(ImagePlus imp, String directory )
    {
        this.imp = imp;
        this.name = imp.getTitle();
        this.directory = directory;
        this.binning = new int[]{ 1, 1, 1 };
    }

    public void setBinning( int[] binning )
    {
        this.binning = binning;
    }

    public void setLogService( LogService logService )
    {
        this.logService = logService;
    }

    public void setChannelNames( ArrayList< String > channelNames )
    {
        this.channelNames = channelNames;
    }

    public void write()
    {
        ImarisDataSet imarisDataSet = getImarisDataSet();

        ImarisWriter.writeHeaderFile( imarisDataSet, directory, name + "-header" + ".ims" );

        H5DataCubeWriter writer = new H5DataCubeWriter();

        for ( int t = 0; t < imp.getNFrames(); ++t )
        {
            for ( int c = 0; c < imp.getNChannels(); ++c )
            {
                final String channelName = imarisDataSet.getChannelNames().get( c );

                final ImagePlus dataCube = Utils.getDataCube( imp, c, t, binning );

                log( "Writing: " + name + ", time-point: " + ( t + 1 ) + ", channel: " + ( c + 1 ) + " ..." );

                writer.writeImarisCompatibleResolutionPyramid( dataCube, imarisDataSet, c, t );
            }
        }

        log( "...done!" );
    }

    private ImarisDataSet getImarisDataSet()
    {
        ImarisDataSet imarisDataSet = new ImarisDataSet( imp, binning, directory, name );

        if ( channelNames != null && channelNames.size() == imarisDataSet.getNumChannels() )
        {
            imarisDataSet.setChannelNames( channelNames );
        }

        return imarisDataSet;
    }


    private void log( String text )
    {

        IJ.log( text );

        if ( logService != null )
        {
            logService.info( text );
        }
        else
        {
            System.out.println( text );
        }

    }


    public static void writeCombinedHeaderFile( ArrayList < File > masterFiles, String filename )
    {

        ImarisDataSet imarisDataSet = new ImarisDataSet( masterFiles.get( 0 ) );

        for ( int f = 1; f < masterFiles.size(); ++f )
        {
            imarisDataSet.addChannelsFromImaris( masterFiles.get( f ) );
        }

        writeHeaderFile( imarisDataSet, masterFiles.get( 0 ).getParent(), filename );

    }


    public static void writeHeaderFile( ImarisDataSet idp,
                                        String directory,
                                        String filename )
    {

        int file_id = H5Utils.createFile( directory, filename );

        setHeader( file_id );
        setImageInfos( file_id, idp.getDimensions(), idp.getInterval(), idp.getNumChannels() );
        setTimeInfos( file_id, idp.getTimePoints() );
        setChannelsInfos( file_id, idp  );
        setExternalDataSets( file_id, idp );

        H5.H5Fclose(file_id);
    }

    private static void setHeader( int file_id )
    {
        H5Utils.writeStringAttribute( file_id, "DataSetDirectoryName", ImarisUtils.DATA_SET );
        H5Utils.writeStringAttribute( file_id, "DataSetInfoDirectoryName", ImarisUtils.DATA_SET_INFO );
        H5Utils.writeStringAttribute( file_id, "ImarisDataSet", "ImarisDataSet");
        H5Utils.writeStringAttribute( file_id, "ImarisVersion", "5.5.0");  // file-format version
        H5Utils.writeStringAttribute( file_id, "NumberOfDataSets", "1");
        H5Utils.writeStringAttribute( file_id, "ThumbnailDirectoryName", "Thumbnail");
    }


    private static void setExternalDataSets( int file_id, ImarisDataSet idp)
    {
        for ( int t = 0; t < idp.getTimePoints().size(); ++t )
        {
            for ( int c = 0; c < idp.getChannelColors().size(); ++c )
            {
                setExternalDataSet( file_id, c, t, idp );
            }
        }
    }

    private static void setExternalDataSet( int file_id, int c, int t, ImarisDataSet imarisDataSet )
    {

        for (int r = 0; r < imarisDataSet.getDimensions().size(); ++r )
        {
            int group_id = H5Utils.createGroup( file_id,
                    ImarisUtils.DATA_SET
                            + "/" + ImarisUtils.RESOLUTION_LEVEL + r
                            + "/" + ImarisUtils.TIME_POINT + t );

            H5.H5Lcreate_external(
                    "./" + imarisDataSet.getDataSetFilename( c, t, r ),
                    imarisDataSet.getDataSetGroupName( c, t, r ),
                    group_id,
                    ImarisUtils.CHANNEL + c,
                    HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT );

            H5.H5Gclose( group_id );
        }

    }


    private static void setImageInfos( int file_id,
                                       ArrayList< long [] > dimensions,
                                       RealInterval interval,
                                       int numChannels )
    {

        int group_id = H5Utils.createGroup( file_id, ImarisUtils.DATA_SET_INFO + "/" +  ImarisUtils.IMAGE );

        // set attributes
        //
        H5Utils.writeStringAttribute(group_id, "Description", "description");

        H5Utils.writeStringAttribute(group_id, "Unit", "um");

        H5Utils.writeStringAttribute(group_id, "Noc", "" + numChannels );

        for ( int d = 0; d < 3; ++d )
        {
            // physical interval
            H5Utils.writeStringAttribute( group_id, "ExtMax" + d, String.valueOf( interval.realMax( d ) ) );
            H5Utils.writeStringAttribute( group_id, "ExtMin" + d, String.valueOf( interval.realMin( d ) ) );
            // number of pixels
            H5Utils.writeStringAttribute( group_id, ImarisUtils.XYZ[ d ], String.valueOf( dimensions.get( 0 )[ d ] ) );
        }


        // the following attributes are not needed by Imaris but by my code
        H5Utils.writeStringAttribute( group_id, ImarisUtils.RESOLUTION_LEVELS_ATTRIBUTE, String.valueOf( dimensions.size() ));

        for ( int r = 0; r < dimensions.size(); ++r )
        {
            for ( int d = 0; d < 3; ++d )
            {
                // number of pixels at different resolutions
                H5Utils.writeStringAttribute( group_id, ImarisUtils.XYZ[ d ] + r, String.valueOf( dimensions.get( r )[ d ] ) );
            }
        }


        H5.H5Gclose( group_id );

    }

    private static void setTimeInfos( int file_id, ArrayList < String > times)
    {

        int group_id = H5Utils.createGroup( file_id, ImarisUtils.DATA_SET_INFO + "/" + ImarisUtils.TIME_INFO );

        // Set attributes
        //
        H5Utils.writeStringAttribute(group_id, "DataSetTimePoints",
                String.valueOf( times.size() ) );

        H5Utils.writeStringAttribute(group_id, "FileTimePoints",
                String.valueOf( times.size() ) );

        for ( int t = 0; t < times.size(); ++t )
        {
            H5Utils.writeStringAttribute(group_id, "TimePoint"+(t+1), times.get( t ) );
        }

        H5.H5Gclose( group_id );

    }

    private static void setChannelInfos( int file_id, int c, ImarisDataSet imarisDataSet )
    {

        int group_id = H5Utils.createGroup( file_id,
                ImarisUtils.DATA_SET_INFO + "/" + ImarisUtils.CHANNEL + c );

        H5Utils.writeStringAttribute(group_id,
                "ColorMode", "BaseColor");

        H5Utils.writeStringAttribute(group_id,
                "ColorOpacity", "1");

        H5Utils.writeStringAttribute(group_id, "Name", imarisDataSet.getChannelNames().get( c ) );

        H5Utils.writeStringAttribute(group_id, "Color", imarisDataSet.getChannelColors().get( c ) );


        H5.H5Gclose( group_id );
    }

    private static void setChannelsInfos( int file_id, ImarisDataSet imarisDataSet )
    {
        for ( int c = 0; c < imarisDataSet.getChannelNames().size(); ++c )
        {
            setChannelInfos( file_id, c, imarisDataSet );
        }
    }

}
