package de.embl.cba.bdp2.saving;

import de.embl.cba.bdp2.logging.Logger;
import de.embl.cba.bdp2.process.IntervalImageViews;
import de.embl.cba.bdp2.progress.ProgressHelpers;
import de.embl.cba.bdp2.utils.Utils;
import ij.ImagePlus;
import ij.ImageStack;
import ij.io.FileSaver;
import loci.common.DebugTools;
import loci.common.services.ServiceFactory;
import loci.formats.ImageWriter;
import loci.formats.meta.IMetadata;
import loci.formats.out.TiffWriter;
import loci.formats.services.OMEXMLService;
import loci.formats.tiff.IFD;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import ome.xml.model.enums.DimensionOrder;
import ome.xml.model.enums.PixelType;
import ome.xml.model.primitives.PositiveInteger;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static de.embl.cba.bdp2.saving.SavingUtils.ShortToByteBigEndian;
import static de.embl.cba.bdp2.utils.DimensionOrder.*;

public class SaveFrameAsTIFFStacks< R extends RealType< R > & NativeType< R > > implements Runnable {
    private final int t;
    private final AtomicInteger counter;
    private final SavingSettings settings;
    private final long startTime;
    private final AtomicBoolean stop;

    // TODO: feed back to progress listener
    public SaveFrameAsTIFFStacks( int t,
                                  SavingSettings settings,
                                  AtomicInteger counter,
                                  final long startTime,
                                  AtomicBoolean stop) {
        this.t = t;
        this.settings = settings;
        this.counter = counter;
        this.startTime = startTime;
        this.stop = stop;
    }

    @Override
    public void run() {

        // TODO:
        // - check whether enough RAM is available to execute current thread
        // - if not, merge GC and wait until there is enough
        // - estimate 3x more RAM then actually necessary
        // - if waiting takes to long somehoe terminate in a nice way

        //long freeMemoryInBytes = IJ.maxMemory() - IJ.currentMemory();
        RandomAccessibleInterval image = settings.rai;

        final long totalCubes = image.dimension( T ) * image.dimension( C );

//        long numBytesOfImage = image.dimension(FileInfoConstants.X) *
//                    image.dimension(FileInfoConstants.Y) *
//                    image.dimension(FileInfoConstants.Z) *
//                    image.dimension(FileInfoConstants.C) *
//                    image.dimension(FileInfoConstants.T) *
//                    files.bitDepth / 8;
//
//            if (numBytesOfImage > 1.5 * freeMemoryInBytes) {
//                // TODO: do something...
//            }

        int totalChannels = Math.toIntExact( settings.rai.dimension( C ));

        for (int c = 0; c < totalChannels; c++)
        {
            if ( stop.get() )
            {
                Logger.progress( "Stopped saving thread: ", "" + t );
                return;
            }

            System.out.println( "Saving started: Frame " + t + ", Channel " + c );

//            RandomAccessibleInterval< R > raiXYZ =
//                    IntervalImageViews.getNonVolatileVolumeCopy(
//                            image,
//                            c, t,
//                            settings.numProcessingThreads );
//

            RandomAccessibleInterval< R > raiXYZ =
                    IntervalImageViews.getVolumeView( image, c, t );

            if ( settings.saveVolumes )
            {
                ImagePlus imp = Utils.wrap3DRaiToCalibratedImagePlus(
                        raiXYZ,
                        settings.voxelSpacing,
                        settings.voxelUnit,
                        "" );

                saveAsTiff( imp, c, t,
                        settings.compression, settings.rowsPerStrip, settings.volumesFilePath );
            }

            if ( settings.saveProjections )
                saveProjections( raiXYZ, c );

            counter.incrementAndGet();

//            if ( ! stop.get() ) {
//                ProgressHelpers.logProgress( totalCubes, counter, startTime, "Saved file " );
//            }

            System.out.println( "Saving finished: Frame " + t + ", Channel " + c );

        }

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
                settings.voxelSpacing,
                settings.voxelUnit,
                "" );

        ProjectionXYZ.saveAsTiffXYZMaxProjection( imp3D, c, t, settings.projectionsFilePath );

        Logger.debug( "Computing and saving projections  [ s ]: "
                + ( System.currentTimeMillis() - start ) / 1000);

    }

    private double[] getVoxelSpacingCopy()
    {
        final double[] voxelSpacing = new double[ settings.voxelSpacing.length ];
        for ( int d = 0; d < settings.voxelSpacing.length; d++ )
            voxelSpacing[ d ] = settings.voxelSpacing[ d ];
        return voxelSpacing;
    }

    private void saveAsTiff(
            ImagePlus imp,
            int c,
            int t,
            String compression,
            int rowsPerStrip,
            String path) {

        DebugTools.setRootLevel( "OFF" ); // Bio-Formats

        String sC = String.format( "%1$02d", c );
        String sT = String.format( "%1$05d", t );

        if ( compression.equals( SavingSettings.COMPRESSION_NONE ) )
        {
            // no compression: use ImageJ's FileSaver, as it is faster than BioFormats
            if ( stop.get() )
            {
                settings.saveProjections = false;
                Logger.progress( "Stopped saving thread: ", "" + t );
                return;
            }

            FileSaver fileSaver = new FileSaver( imp );
            String pathCT = path + "--C" + sC + "--T" + sT + ".tif";

            fileSaver.saveAsTiffStack( pathCT );
        }
        else
        {
            // Use Bio-Formats for compressing the data

            String pathCT = path + "--C" + sC + "--T" + sT + ".ome.tif";

            if ( new File( pathCT ).exists() ) new File( pathCT ).delete();

            try {

                ServiceFactory factory = new ServiceFactory();
                OMEXMLService service = factory.getInstance(OMEXMLService.class);
                IMetadata meta = service.createOMEXMLMetadata();
                meta.setImageID("Image:0", 0);
                meta.setPixelsID("Pixels:0", 0);
                meta.setPixelsBinDataBigEndian(Boolean.TRUE, 0, 0);
                meta.setPixelsDimensionOrder(DimensionOrder.XYZCT, 0);
                if (imp.getBytesPerPixel() == 2) {
                    meta.setPixelsType(PixelType.UINT16, 0);
                } else if (imp.getBytesPerPixel() == 1) {
                    meta.setPixelsType(PixelType.UINT8, 0);
                }
                meta.setPixelsSizeX(new PositiveInteger(imp.getWidth()), 0);
                meta.setPixelsSizeY(new PositiveInteger(imp.getHeight()), 0);
                meta.setPixelsSizeZ(new PositiveInteger(imp.getNSlices()), 0);
                meta.setPixelsSizeC(new PositiveInteger(1), 0);
                meta.setPixelsSizeT(new PositiveInteger(1), 0);

                int channel = 0;
                meta.setChannelID("Channel:0:" + channel, 0, channel);
                meta.setChannelSamplesPerPixel(new PositiveInteger(1), 0, channel);

                ImageWriter writer = new ImageWriter();
                writer.setValidBitsPerPixel( imp.getBytesPerPixel() * 8 );
                writer.setMetadataRetrieve( meta );
                writer.setId( pathCT );
                writer.setWriteSequentially( true ); // ? is this necessary

                if ( settings.compression.equals( SavingSettings.COMPRESSION_ZLIB ) )
                    writer.setCompression( TiffWriter.COMPRESSION_ZLIB );
                else if ( settings.compression.equals( SavingSettings.COMPRESSION_LZW ) )
                    writer.setCompression( TiffWriter.COMPRESSION_LZW );

                TiffWriter tiffWriter = (TiffWriter) writer.getWriter();

                long[] rowsPerStripArray = new long[]{ rowsPerStrip };

                for (int z = 0; z < imp.getNSlices(); z++)
                {
                    if ( stop.get() )
                    {
                        Logger.progress("Stopped saving thread: ", "" + t);
                        settings.saveProjections = false;
                        return;
                    }

                    // save using planes for compression
                    if (imp.getBytesPerPixel() == 2)
                        writer.saveBytes( z, ShortToByteBigEndian((short[]) imp.getStack().getProcessor(z + 1).getPixels() ) );
                    else if (imp.getBytesPerPixel() == 1)
                        writer.saveBytes( z, (byte[]) (imp.getStack().getProcessor(z + 1).getPixels() ) );

                    // save using strips for compression
//                    IFD ifd = new IFD();
//                    ifd.put( IFD.ROWS_PER_STRIP, rowsPerStripArray );
//                    if (imp.getBytesPerPixel() == 2) {
//                        tiffWriter.saveBytes(z, SavingUtils.ShortToByteBigEndian((short[]) imp.getStack().getProcessor(z + 1).getPixels()), ifd);
//                    } else if (imp.getBytesPerPixel() == 1) {
//                        tiffWriter.saveBytes(z, (byte[]) (imp.getStack().getProcessor(z + 1).getPixels()), ifd);
//                    }
                }
                writer.close();

            } catch (Exception e) {
                Logger.error(e.toString());
            }
        }
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
