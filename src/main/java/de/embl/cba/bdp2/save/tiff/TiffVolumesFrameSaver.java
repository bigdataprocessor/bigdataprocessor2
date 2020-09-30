package de.embl.cba.bdp2.save.tiff;

import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.save.ProjectionXYZ;
import de.embl.cba.bdp2.save.SavingSettings;
import de.embl.cba.bdp2.utils.IntervalImageViews;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.bdv.utils.BdvUtils;
import ij.ImagePlus;
import ij.ImageStack;
import loci.common.DebugTools;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.common.services.ServiceFactory;
import loci.formats.FormatException;
import loci.formats.FormatTools;
import loci.formats.ImageWriter;
import loci.formats.meta.IMetadata;
import loci.formats.out.TiffWriter;
import loci.formats.services.OMEXMLService;
import loci.formats.tiff.IFD;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import ome.units.quantity.Length;
import ome.xml.model.enums.DimensionOrder;
import ome.xml.model.enums.PixelType;
import ome.xml.model.primitives.PositiveInteger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static de.embl.cba.bdp2.save.tiff.TiffUtils.ShortToByteBigEndian;
import static de.embl.cba.bdp2.utils.DimensionOrder.*;

public class TiffVolumesFrameSaver< R extends RealType< R > & NativeType< R > > implements Runnable {
    private final int t;
    private final AtomicInteger counter;
    private final SavingSettings settings;
    private final long startTime;
    private final AtomicBoolean stop;
    private RandomAccessibleInterval rai;

    public TiffVolumesFrameSaver( int t,
                                  SavingSettings settings,
                                  AtomicInteger counter,
                                  final long startTime,
                                  AtomicBoolean stop) {
        this.t = t;
        this.settings = settings;
        this.counter = counter;
        this.startTime = startTime;
        this.stop = stop;
        rai = settings.rai;
    }

    @Override
    public void run()
    {
        // TODO:
        // checkMemoryRequirements();

        int totalChannels = Math.toIntExact( rai.dimension( C ));

        for (int c = 0; c < totalChannels; c++)
        {
            if ( stop.get() )
            {
                Logger.progress( "Stopped save thread: ", "" + t );
                return;
            }

            // Note; below call will both
            // (i) load the raw image into RAM
            // (ii) make a copy in RAM with all processing done
            RandomAccessibleInterval< R > raiXYZ = IntervalImageViews.createNonVolatileVolumeCopy( rai, c, t, settings.numProcessingThreads, ( R )settings.type );

            if ( settings.saveVolumes )
            {
                ImagePlus imp = Utils.wrap3DRaiToCalibratedImagePlus(
                        raiXYZ,
                        settings.voxelSize,
                        settings.voxelUnit,
                        "" );

                String channelName = getChannelName( c );

                saveAsTiff( imp, t, settings.compression, settings.rowsPerStrip, settings.volumesFilePathStump, channelName );
            }

            if ( settings.saveProjections )
            {
                saveProjections( raiXYZ, c );
            }

            counter.incrementAndGet();

            System.gc(); // TODO: test whether this could be removed
        }

    }

    public String getChannelName( int c )
    {
        if ( settings.channelNamesInSavedImages.equals( SavingSettings.CHANNEL_INDEXING ) )
            return String.format( "C%1$02d", c );
        else if ( settings.channelNamesInSavedImages.equals( SavingSettings.CHANNEL_NAMES ) )
            return settings.channelNames[ c  ];
        else
            return String.format( "C%1$02d", c );
    }

    public void checkMemoryRequirements()
    {
        // - check whether enough RAM is available to execute current thread
        // - if not, merge GC and wait until there is enough
        // - estimate 3x more RAM then actually necessary
        // - if waiting takes to long somehoe terminate in a nice way

        // long freeMemoryInBytes = IJ.maxMemory() - IJ.currentMemory();

        final long totalCubes = rai.dimension( T ) * rai.dimension( C );

//        long numBytesOfImage = image.dimension(FileInfoConstants.X) *
//                    image.dimension(FileInfoConstants.Y) *
//                    image.dimension(FileInfoConstants.Z) *
//                    image.dimension(FileInfoConstants.C) *
//                    image.dimension(FileInfoConstants.T) *
//                    file.bitDepth / 8;
//
//            if (numBytesOfImage > 1.5 * freeMemoryInBytes) {
//                // TODO: handle this
//            }
    }

    private void saveProjections(
            RandomAccessibleInterval rai3D,
            int c )
    {

        long start = System.currentTimeMillis();

//        if ( settings.isotropicProjectionResampling )
//        {
//            final double[] scalingFactors =
//                    Transforms.getScalingFactors(
//                            voxelSpacing,
//                            settings.isotropicProjectionVoxelSize );
//
//            rai3D = Scalings.createRescaledArrayImg( rai3D, scalingFactors );
//
//            for ( int d = 0; d < 3; d++ )
//                voxelSpacing[ d ] /= scalingFactors[ d ];
//        }


        ImagePlus imp3D = Utils.wrap3DRaiToCalibratedImagePlus(
                rai3D,
                settings.voxelSize,
                settings.voxelUnit,
                "" );

        ProjectionXYZ.saveAsTiffXYZMaxProjection( imp3D, c, t, settings.projectionsFilePathStump );

        Logger.debug( "Computed and saved projections in [ s ]: " + ( System.currentTimeMillis() - start ) / 1000);

    }

    private double[] getVoxelSpacingCopy()
    {
        final double[] voxelSpacing = new double[ settings.voxelSize.length ];
        for ( int d = 0; d < settings.voxelSize.length; d++ )
            voxelSpacing[ d ] = settings.voxelSize[ d ];
        return voxelSpacing;
    }

    private void saveAsTiff(
            ImagePlus imp,
            int t,
            String compression,
            int rowsPerStrip,
            String path,
            String sC )
    {
        long start = System.currentTimeMillis();

        String sT = String.format( "T%1$05d", t );

        if ( compression.equals( SavingSettings.COMPRESSION_NONE ) )
        {
            saveWithImageJ( imp, t, path, sC, sT );
        }
        else
        {
//             super slow!
            saveWithBioFormats( imp, t, rowsPerStrip, path, sC, sT );
        }

        Logger.debug( "Saved volume in [ s ]: " + ( System.currentTimeMillis() - start ) / 1000.0);
    }

    private void saveWithBioFormats( ImagePlus imp, int t, int rowsPerStrip, String path, String sC, String sT )
    {
        // Use Bio-Formats for compressing the data

        DebugTools.setRootLevel( "OFF" ); // Bio-Formats

        String pathCT = getFullPath( path, sC, sT, ".ome.tif" );

        if ( new File( pathCT ).exists() ) new File( pathCT ).delete();

        try
        {
            ImageWriter writer = getImageWriter( imp, pathCT );

            final String compression = writer.getCompression();
            if ( settings.compression.equals( SavingSettings.COMPRESSION_ZLIB ) )
                writer.setCompression( TiffWriter.COMPRESSION_ZLIB );
            else if ( settings.compression.equals( SavingSettings.COMPRESSION_LZW ) )
                writer.setCompression( TiffWriter.COMPRESSION_LZW );

            TiffWriter tiffWriter = (TiffWriter) writer.getWriter();

            if ( rowsPerStrip == -1 )
                rowsPerStrip = imp.getHeight(); // use all rows

            //rowsPerStrip = 1;
            long[] rowsPerStripArray = new long[]{ rowsPerStrip };

            for (int z = 0; z < imp.getNSlices(); z++)
            {
                if ( stop.get() )
                {
                    Logger.progress("Stopped save thread: ", "" + t);
                    settings.saveProjections = false;
                    return;
                }

                // save, configuring myself how many strips to use for compression
                IFD ifd = new IFD();
                ifd.put( IFD.ROWS_PER_STRIP, rowsPerStripArray );
                if (imp.getBytesPerPixel() == 2)
                {
                    long start = System.currentTimeMillis();
                    byte[] bytes = ShortToByteBigEndian( ( short[] ) imp.getStack().getProcessor( z + 1 ).getPixels() );
                    System.out.println( "convert: " + (System.currentTimeMillis() - start) );

                    start = System.currentTimeMillis();
                    tiffWriter.saveBytes(z, bytes, ifd);
                    System.out.println( "save: " + (System.currentTimeMillis() - start) );

                }
                else if (imp.getBytesPerPixel() == 1)
                {
                    byte[] bytes = ( byte[] ) ( imp.getStack().getProcessor( z + 1 ).getPixels() );
                    tiffWriter.saveBytes(z, bytes, ifd);
                }
            }

            writer.close();
        }
        catch (Exception e)
        {
            Logger.error(e.toString());
        }
    }

    private ImageWriter getImageWriter( ImagePlus imp, String pathCT ) throws DependencyException, ServiceException, FormatException, IOException
    {
        ServiceFactory factory = new ServiceFactory();
        OMEXMLService service = factory.getInstance(OMEXMLService.class);
        IMetadata meta = service.createOMEXMLMetadata();
        meta.setImageID("Image:0", 0);
        meta.setPixelsID("Pixels:0", 0);
        meta.setPixelsBinDataBigEndian(Boolean.TRUE, 0, 0);
        meta.setPixelsDimensionOrder( DimensionOrder.XYZCT, 0);
        if (imp.getBytesPerPixel() == 2) {
            meta.setPixelsType( PixelType.UINT16, 0);
        } else if (imp.getBytesPerPixel() == 1) {
            meta.setPixelsType(PixelType.UINT8, 0);
        }
        meta.setPixelsSizeX(new PositiveInteger(imp.getWidth()), 0);
        meta.setPixelsSizeY(new PositiveInteger(imp.getHeight()), 0);
        meta.setPixelsSizeZ(new PositiveInteger(imp.getNSlices()), 0);
        meta.setPixelsSizeC(new PositiveInteger(1), 0);
        meta.setPixelsSizeT(new PositiveInteger(1), 0);

        Length physicalSizeX = FormatTools.getPhysicalSizeX(imp.getCalibration().pixelWidth, imp.getCalibration().getXUnit());
        Length physicalSizeY = FormatTools.getPhysicalSizeY(imp.getCalibration().pixelHeight, imp.getCalibration().getYUnit());
        Length physicalSizeZ = FormatTools.getPhysicalSizeZ(imp.getCalibration().pixelDepth, imp.getCalibration().getZUnit());

        meta.setPixelsPhysicalSizeX(physicalSizeX, 0);
        meta.setPixelsPhysicalSizeY(physicalSizeY, 0);
        meta.setPixelsPhysicalSizeZ(physicalSizeZ, 0);

        int channel = 0;
        meta.setChannelID("Channel:0:" + channel, 0, channel);
        meta.setChannelSamplesPerPixel(new PositiveInteger(1), 0, channel);

        ImageWriter writer = new ImageWriter();
        writer.setValidBitsPerPixel( imp.getBytesPerPixel() * 8 );
        writer.setMetadataRetrieve( meta );
        writer.setId( pathCT );
        writer.setWriteSequentially( true ); // ? is this necessary
        return writer;
    }

    private void saveWithImageJ( ImagePlus imp, int t, String path, String sC, String sT )
    {
        // no compression: use ImageJ's FileSaver, as it is faster than BioFormats
        if ( stop.get() )
        {
            settings.saveProjections = false;
            Logger.progress( "Stopped save thread: ", "" + t );
            return;
        }

        // The FileTiffSaverFromImageJ has a minimal modification
        // compared to the FileSaver, which that makes it much faster
        // for virtual stacks; see commented line in the saveAsTiffStack()
        // function
        FileTiffSaverFromImageJ fileSaver = new FileTiffSaverFromImageJ( imp );
        String pathCT = getFullPath( path, sC, sT, ".tif" );

        fileSaver.saveAsTiffStack( pathCT );
    }

    @NotNull
    private String getFullPath( String path, String sC, String sT, String suffix )
    {
        return path + "--" + sC + "--" + sT + suffix;
    }

    private static void gate(ImagePlus imp, int min, int max) //TODO: May be this can goto a new SaveHelper class
    {
        ImageStack stack = imp.getStack();

        for (int i = 1; i < stack.size(); ++i) {
            if (imp.getBitDepth() == 8) {
                byte[] pixels = (byte[]) stack.getPixels(i);
                for (int j = 0; j < pixels.length; j++) {
                    int v = pixels[j] & 0xff;
                    pixels[j] = ((v < min) || (v > max)) ? 0 : pixels[j];
                }
            }
            if (imp.getBitDepth() == 16) {
                short[] pixels = (short[]) stack.getPixels(i);
                for (int j = 0; j < pixels.length; j++) {
                    int v = pixels[j] & 0xffff;
                    pixels[j] = ((v < min) || (v > max)) ? 0 : pixels[j];
                }
            }

        }

    }

}
