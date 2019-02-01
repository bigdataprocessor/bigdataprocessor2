package de.embl.cba.bigDataTools2.imaris;

import de.embl.cba.bigDataTools2.logging.IJLazySwingLogger;
import de.embl.cba.bigDataTools2.logging.Logger;
import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import net.imglib2.FinalRealInterval;
import net.imglib2.RealInterval;

import java.io.File;
import java.util.ArrayList;


public class ImarisDataSet {

    private ArrayList < long[] > dimensions;
    private ArrayList < int[] > relativeBinnings;
    private ArrayList < long[] > chunks;
    private ArrayList < String > channelColors;
    private ArrayList < String > channelNames;
    private RealInterval interval;
    private CTRDataSets ctrDataSets;
    private ArrayList < String > timePoints;

    Logger logger = new IJLazySwingLogger();

    // Trying to make blocks of about 8000 voxels in size (8-bit)
    // Because I read somewhere that the OS reads often anyway in blocks of around 8000 bytes...
    private static int CHUNKING_XY_HIGHEST_RESOLUTION = 256;
    private static int CHUNKING_Z_HIGHEST_RESOLUTION = 1;
    private static int CHUNKING_XYZ = 64;

    public ImarisDataSet( File file )
    {
        initFromImarisFile( file.getParent(), file.getName() );
    }

    public ImarisDataSet( String directory, String filename )
    {
        initFromImarisFile( directory, filename );
    }


    public ImarisDataSet( ImagePlus imp,
                          int[] binning,
                          String directory,
                          String filenameStump )
    {
        setDimensionsBinningsChunks( imp, binning );
        setTimePoints( imp );
        setChannels( imp );
        setInterval( imp );

        ctrDataSets = new CTRDataSets();

        for ( int c = 0; c < channelColors.size(); ++c )
        {
            for ( int t = 0; t < timePoints.size(); ++t )
            {
                for ( int r = 0; r < dimensions.size(); ++r )
                {
                    ctrDataSets.addExternal( c, t, r, directory, filenameStump );
                }
            }
        }
    }

    private void initFromImarisFile( String directory, String filename )
    {
        ImarisReader reader = new ImarisReader( directory, filename );

        channelColors = reader.getChannelColors();
        channelNames = reader.getChannelNames();
        timePoints = reader.getTimePoints();
        dimensions = reader.getDimensions();
        interval = reader.getCalibratedInterval();

        ctrDataSets = new CTRDataSets();

        for ( int c = 0; c < channelColors.size(); ++c )
        {
            for ( int t = 0; t < timePoints.size(); ++t )
            {
                for ( int r = 0; r < dimensions.size(); ++r )
                {
                    ctrDataSets.addImaris( c, c, t, r, directory, filename );
                }
            }
        }

        reader.closeFile();
    }

    public ArrayList< String > getChannelNames()
    {
        return channelNames;
    }

    public String getDataSetDirectory( int c, int t, int r)
    {
        return ctrDataSets.get( c, t, r ).directory;
    }

    public String getDataSetFilename( int c, int t, int r )
    {
        return ( ctrDataSets.get( c, t, r ).filename );
    }

    public String getDataSetGroupName( int c, int t, int r)
    {
        return ( ctrDataSets.get( c, t, r ).h5Group );
    }

    public ArrayList< int[] > getRelativeBinnings()
    {
        return relativeBinnings;
    }

    public RealInterval getInterval()
    {
        return interval;
    }

    public ArrayList< String > getChannelColors()
    {
        return channelColors;
    }

    public int getNumChannels()
    {
        return channelNames.size();
    }

    public ArrayList< String > getTimePoints()
    {
        return timePoints;
    }

    public ArrayList< long[] > getDimensions()
    {
        return dimensions;
    }

    public ArrayList< long[] > getChunks()
    {
        return chunks;
    }

    public void setLogger( Logger logger )
    {
        this.logger = logger;
    }

    private long[] getImageSize(ImagePlus imp, int[] primaryBinning )
    {

        long[] size = new long[3];

        // bin image to see how large it would be
        if ( primaryBinning[0] > 1 || primaryBinning[1] > 1 || primaryBinning[2] > 1 )
        {

            /*
            logger.info("Determining image size at " +
                    "highest resolution level after initial binning...");

            ImagePlus impBinned = null;
            if ( ! ( imp.getStack() instanceof VirtualStackOfStacks) )
            {
                logger.error( "This currently only works for streamed data." );
                return null;
            }
            VirtualStackOfStacks vss = (VirtualStackOfStacks) imp.getStack();
            impBinned = vss.getFullFrame( 0, 0, 1 );

            Binner binner = new Binner();
            impBinned = binner.shrink( impBinned, primaryBinning[0], primaryBinning[1], primaryBinning[2], binner.AVERAGE );
            size[0] = impBinned.getWidth();
            size[1] = impBinned.getHeight();
            size[2] = impBinned.getNSlices();

            */

            size[0] = imp.getWidth();
            size[1] = imp.getHeight();
            size[2] = imp.getNSlices();

            for ( int d = 0; d < 3; ++d )
            {
                size[d] /= primaryBinning[d];
            }

        }
        else
        {
            size[0] = imp.getWidth();
            size[1] = imp.getHeight();
            size[2] = imp.getNSlices();
        }

        return ( size );

    }

    private void setDimensionsBinningsChunks(ImagePlus imp, int[] primaryBinning )
    {

        dimensions = new ArrayList<>();
        relativeBinnings = new ArrayList<>();
        chunks = new ArrayList<>();

        int impByteDepth = imp.getBitDepth() / 8;

        long[] initialChunks = new long[]{ CHUNKING_XY_HIGHEST_RESOLUTION, CHUNKING_XY_HIGHEST_RESOLUTION, CHUNKING_Z_HIGHEST_RESOLUTION };
        int[] initialBinning = new int[]{ 1, 1, 1 };

        for ( int iResolution = 0; ; ++iResolution )
        {
            long currentVolume;
            long[] currentChunks;
            int[] currentRelativeBinning = new int[3];
            long[] currentDimensions = new long[3];

            if ( iResolution == 0 )
            {
                currentDimensions = getImageSize( imp, primaryBinning );

                currentChunks = initialChunks;

                ensureChunkSizesNotExceedingCurrentImageDimensions( currentDimensions, currentChunks );

                currentRelativeBinning = initialBinning;
            }
            else
            {

                long[] lastDimensions = dimensions.get( iResolution - 1 );
                long lastVolume = lastDimensions[ 0 ] * lastDimensions[ 1 ] * lastDimensions[ 2 ];

                setDimensionsAndBinningsForThisResolutionLayer( currentDimensions, currentRelativeBinning, lastDimensions, lastVolume );

                currentChunks = getChunksForThisResolutionLayer( currentDimensions );

            }

            currentVolume = currentDimensions[ 0 ] * currentDimensions[ 1 ] * currentDimensions[ 2 ];

            adaptZChunkingToAccomodateJavaIndexingLimitations( currentVolume, currentChunks );

            dimensions.add( currentDimensions );

            chunks.add( currentChunks );

            relativeBinnings.add( currentRelativeBinning );

            if ( currentVolume < ImarisUtils.MIN_VOXELS )
            {
                break;
            }

        }

        int a = 1; // debug

    }

    private void setDimensionsAndBinningsForThisResolutionLayer( long[] currentDimensions, int[] currentRelativeBinning, long[] lastDimensions, long lastVolume )
    {
        for ( int d = 0; d < 3; d++ )
        {
            long lastSizeThisDimensionSquared = lastDimensions[ d ] * lastDimensions[ d ];
            long lastPerpendicularPlaneSize = lastVolume / lastDimensions[ d ];

            if ( 100 * lastSizeThisDimensionSquared > lastPerpendicularPlaneSize )
            {
                currentDimensions[ d ] = lastDimensions[ d ] / 2;
                currentRelativeBinning[ d ] = 2;
            }
            else
            {
                currentDimensions[ d ] = lastDimensions[ d ];
                currentRelativeBinning[ d ] = 1;
            }
            currentDimensions[ d ] = Math.max( 1, currentDimensions[ d ] );
        }
    }

    private long[] getChunksForThisResolutionLayer( long[] currentDimensions )
    {
        long[] currentChunks;
        currentChunks = new long[]{ CHUNKING_XYZ, CHUNKING_XYZ, CHUNKING_XYZ };

        ensureChunkSizesNotExceedingCurrentImageDimensions( currentDimensions, currentChunks );

        return currentChunks;
    }

    private void ensureChunkSizesNotExceedingCurrentImageDimensions( long[] currentDimensions, long[] currentChunks )
    {
        for ( int d = 0; d < 3; d++ )
        {
            if ( currentChunks[ d ] > currentDimensions[ d ] )
            {
                currentChunks[ d ] = currentDimensions[ d ];
            }
        }
    }

    private void adaptZChunkingToAccomodateJavaIndexingLimitations( long currentVolume, long[] currentChunks )
    {
        if ( currentVolume > Integer.MAX_VALUE - 100 )
        {
            currentChunks[ 2 ] = 1;
            IJ.log( "Data set is larger than " + Integer.MAX_VALUE );
            // this forces plane wise writing and thus
            // avoids java indexing issues when saving the data to HDF5_STACKS
        }
    }

    private void setTimePoints( ImagePlus imp )
    {
        timePoints = new ArrayList<>();

        for ( int t = 0; t < imp.getNFrames(); ++t )
        {
            // TODO: extract real information from imp?
            timePoints.add("2000-01-01 00:00:0" + t);
        }
    }

    private void setChannels( ImagePlus imp )
    {
        channelColors = new ArrayList<>();
        channelNames = new ArrayList<>();

        for ( int c = 0; c < imp.getNChannels(); ++c )
        {
            channelColors.add( ImarisUtils.DEFAULT_COLOR );
            channelNames.add( "channel_" + c );
        }
    }

    public void setChannelNames( ArrayList< String > channelNames )
    {
        this.channelNames = channelNames;
    }

    private void setInterval( ImagePlus imp )
    {
        double[] min = new double[3];
        double[] max = new double[3];

        Calibration calibration = imp.getCalibration();

        double conversionToMicrometer = 1.0;

        if ( calibration.getUnit().equals( "nm" )
                || calibration.getUnit().equals( "nanometer" )
                || calibration.getUnit().equals( "nanometre" ) )
        {
            conversionToMicrometer = 1.0 / 1000.0;
        }

        max[ 0 ] = imp.getWidth() * calibration.pixelWidth * conversionToMicrometer;
        max[ 1 ] = imp.getHeight() * calibration.pixelHeight * conversionToMicrometer;
        max[ 2 ] = imp.getNSlices() * calibration.pixelDepth * conversionToMicrometer;

        interval = new FinalRealInterval( min, max );
    }





    public void addChannelsFromImaris( File file )
    {
        addChannelsFromImaris( file.getParent(), file.getName() );
    }

    public void addChannelsFromImaris( String directory, String filename )
    {
        ImarisReader reader = new ImarisReader( directory, filename );

        int nc = reader.getChannelColors().size();
        int nt = reader.getTimePoints().size();
        int nr = reader.getDimensions().size();

        int currentNumChannelsInMetaFile = channelColors.size();

        for ( int c = 0; c < nc; ++c )
        {
            channelColors.add( reader.getChannelColors().get( c ) );
            channelNames.add( reader.getChannelNames().get( c ) );

            for ( int t = 0; t < nt; ++t )
            {
                for ( int r = 0; r < nr; ++r )
                {
                    ctrDataSets.addImaris( c + currentNumChannelsInMetaFile, c, t, r, directory, filename);
                }
            }
        }

    }

}
