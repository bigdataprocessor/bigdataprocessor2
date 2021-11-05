/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2021 EMBL
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package de.embl.cba.bdp2.save.tiff;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.save.Projector;
import de.embl.cba.bdp2.save.SavingSettings;
import de.embl.cba.bdp2.utils.RAISlicer;
import de.embl.cba.bdp2.utils.Utils;
import ij.ImagePlus;
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
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import ome.units.quantity.Length;
import ome.xml.model.enums.DimensionOrder;
import ome.xml.model.enums.PixelType;
import ome.xml.model.primitives.PositiveInteger;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static de.embl.cba.bdp2.save.tiff.TIFFUtils.ShortToByteBigEndian;
import static de.embl.cba.bdp2.utils.DimensionOrder.*;

public class TIFFFrameSaver< R extends RealType< R > & NativeType< R > > implements Runnable {
    private final int t;
    private final AtomicInteger counter;
    private final Image image;
    private final SavingSettings settings;
    private final long startTime;
    private final AtomicBoolean stop;
    private RandomAccessibleInterval rai;

    public TIFFFrameSaver( int t,
						   Image< R > image,
                           SavingSettings settings,
						   AtomicInteger counter,
						   final long startTime,
						   AtomicBoolean stop ) {
        this.t = t;
        this.image = image;
        this.settings = settings;
        this.counter = counter;
        this.startTime = startTime;
        this.stop = stop;
        this.rai = image.getRai();
    }

    @Override
    public void run()
    {
        // TODO:
        // checkMemoryRequirements();

        int numChannels = Math.toIntExact( rai.dimension( C ) );

        for ( int c = 0; c < numChannels; c++ )
        {
            Logger.debug( "Saving" + Utils.getChannelTimepointLog( c, t ) );

            if ( stop.get() )
            {
                Logger.progress( "Stopped save thread: ", "" + t );
                return;
            }

            // Note: below call will both
            // (i) load the raw image into RAM
            // (ii) make a copy in RAM with all processing done
            Logger.debug( "Fetching" + Utils.getChannelTimepointLog( c, t ) );
            final int numProcessingThreads = settings.numProcessingThreads;
            final R type = ( R ) image.getType();
            final RandomAccess randomAccess = rai.randomAccess();
            RandomAccessibleInterval< R > raiXYZ = RAISlicer.createVolumeCopy( rai, c, t, numProcessingThreads, type );

            // TODO: this again does a getType call internally, which can be costly => fix this if possible
            ImagePlus imp = Utils.asImagePlus( raiXYZ, image, c );

            if ( imp.getBitDepth() > 16 && ! settings.compression.equals( SavingSettings.COMPRESSION_NONE ))
            {
                throw new UnsupportedOperationException( "Cannot yet save a compressed TIFF with a bit-depth of " + imp.getBytesPerPixel() * 8 + "; please try saving uncompressed." );
            }

            String channelName = SavingSettings.getChannelName( c, settings, image );

            if ( settings.saveVolumes )
            {
                Logger.debug( "Saving volume" + Utils.getChannelTimepointLog( c, t ) + " as (partial file name) " + settings.volumesFilePathStump );
                saveAsTIFF( imp, t, settings.compression, settings.rowsPerStrip, settings.volumesFilePathStump, channelName );
            }

            if ( settings.saveProjections )
            {
                Logger.debug( "Saving projections" + Utils.getChannelTimepointLog( c, t ) + " as (partial file name) " + settings.projectionsFilePathStump );
                saveProjections( imp, channelName );
            }

            counter.incrementAndGet();

            System.gc(); // TODO: test whether this could be removed
        }

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

    private void saveProjections( ImagePlus imp3D, String channelName )
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

        Projector.saveProjections( imp3D, channelName, t, settings.projectionsFilePathStump, settings.projectionMode );

        Logger.benchmark( "Computed and saved projections [ ms ]: " + ( System.currentTimeMillis() - start ) );
    }

    private void saveAsTIFF(
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
            // very slow even for uncompressed data: https://forum.image.sc/t/bioformats-saving-with-compression-is-too-slow/20522/10
            saveWithBioFormats( imp, t, rowsPerStrip, path, sC, sT );
        }

        Logger.benchmark( "Saved volume in [ ms ]: " + ( System.currentTimeMillis() - start ) );
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

            if ( settings.compression.equals( SavingSettings.COMPRESSION_ZLIB ) )
                writer.setCompression( TiffWriter.COMPRESSION_ZLIB );
            else if ( settings.compression.equals( SavingSettings.COMPRESSION_LZW ) )
                writer.setCompression( TiffWriter.COMPRESSION_LZW );

            TiffWriter tiffWriter = (TiffWriter) writer.getWriter();

            if ( rowsPerStrip == -1 )
                rowsPerStrip = imp.getHeight(); // use all rows

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
                if (imp.getBytesPerPixel() == 1)
                {
                    byte[] bytes = ( byte[] ) ( imp.getStack().getProcessor( z + 1 ).getPixels() );
                    tiffWriter.saveBytes(z, bytes, ifd);
                }
                else if (imp.getBytesPerPixel() == 2)
                {
                    long start = System.currentTimeMillis();
                    byte[] bytes = ShortToByteBigEndian( ( short[] ) imp.getStack().getProcessor( z + 1 ).getPixels() );
                    //System.out.println( "convert: " + (System.currentTimeMillis() - start) );

                    start = System.currentTimeMillis();
                    tiffWriter.saveBytes(z, bytes, ifd);
                    //System.out.println( "save: " + (System.currentTimeMillis() - start) );
                }
                else
                {
                    throw new UnsupportedOperationException( "Cannot save using Bio-Formats to TIFF with a bit-depth of " + imp.getBytesPerPixel() * 8 );
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

        // The FileTIFFSaverFromImageJ has a minimal modification
        // compared to the FileSaver, which that makes it much faster
        // for virtual stacks; see commented line in the saveAsTIFFStack()
        // function
        FileTIFFSaverFromImageJ fileSaver = new FileTIFFSaverFromImageJ( imp );
        String pathCT = getFullPath( path, sC, sT, ".tif" );

        Logger.debug( "Saving " + pathCT );
        fileSaver.saveAsTIFFStack( pathCT );
    }

    private String getFullPath( String path, String sC, String sT, String suffix )
    {
        return path + "--" + sC + "--" + sT + suffix;
    }
}
