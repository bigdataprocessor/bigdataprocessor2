package de.embl.cba.bdp2.saving;

import de.embl.cba.bdp2.ProjectionXYZ;
import de.embl.cba.bdp2.fileinfosource.FileInfoConstants;
import de.embl.cba.bdp2.logging.IJLazySwingLogger;
import de.embl.cba.bdp2.logging.Logger;
import de.embl.cba.bdp2.utils.Utils;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.io.FileSaver;
import ij.plugin.Binner;
import loci.common.services.ServiceFactory;
import loci.formats.ImageWriter;
import loci.formats.meta.IMetadata;
import loci.formats.out.TiffWriter;
import loci.formats.services.OMEXMLService;
import loci.formats.tiff.IFD;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.view.Views;
import ome.xml.model.enums.DimensionOrder;
import ome.xml.model.enums.PixelType;
import ome.xml.model.primitives.PositiveInteger;

import java.util.concurrent.atomic.AtomicInteger;

public class SaveImgAsTIFFStacks implements Runnable {
    private final int t;
    private final AtomicInteger counter;
    private final SavingSettings savingSettings;
    private final long startTime;

    private final Logger logger = new IJLazySwingLogger();


    public SaveImgAsTIFFStacks(int t,
                               SavingSettings savingSettings,
                               AtomicInteger counter,
                               final long startTime) {
        this.t = t;
        this.savingSettings = savingSettings;
        this.counter = counter;
        this.startTime = startTime;
    }

    @Override
    public void run() {

        // TODO:
        // - check whether enough RAM is available to execute current thread
        // - if not, run GC and wait until there is enough
        // - estimate 3x more RAM then actually necessary
        // - if waiting takes to long somehoe terminate in a nice way

        //long freeMemoryInBytes = IJ.maxMemory() - IJ.currentMemory();
        RandomAccessibleInterval image = savingSettings.image;
        final long totalCubes = image.dimension( FileInfoConstants.T ) * image.dimension(FileInfoConstants.C );
//            long numBytesOfImage = image.dimension(FileInfoConstants.X) *
//                    image.dimension(FileInfoConstants.Y) *
//                    image.dimension(FileInfoConstants.Z) *
//                    image.dimension(FileInfoConstants.C) *
//                    image.dimension(FileInfoConstants.T) *
//                    fileinfosource.bitDepth / 8;
//
//            if (numBytesOfImage > 1.5 * freeMemoryInBytes) {
//                // TODO: do something...
//            }
        int totalChannels = Math.toIntExact(savingSettings.image.dimension(FileInfoConstants.C ));
        for (int c = 0; c < totalChannels; c++) {
            if (SaveCentral.interruptSavingThreads) {
                logger.progress("Stopped saving thread: ", "" + t);
                return;
            }

            // Load
            //   ImagePlus impChannelTime = getDataCube( c );  May be faster???
            long[] minInterval = new long[]{
                    image.min(FileInfoConstants.X ),
                    image.min(FileInfoConstants.Y ),
                    image.min(FileInfoConstants.Z ),
                    c,
                    this.t};
            long[] maxInterval = new long[]{
                    image.max(FileInfoConstants.X ),
                    image.max(FileInfoConstants.Y ),
                    image.max(FileInfoConstants.Z ),
                    c,
                    this.t};

            RandomAccessibleInterval rai3D = Views.interval(image, minInterval, maxInterval);
            ImagePlus impChannelTime = ImageJFunctions.wrap( rai3D, "cube" );
            // Gate
            //
            if (savingSettings.gate) {
                gate(impChannelTime, savingSettings.gateMin, savingSettings.gateMax);
            }

            // Convert
            //
            if (savingSettings.convertTo8Bit) {
                IJ.setMinAndMax(impChannelTime, savingSettings.mapTo0, savingSettings.mapTo255);
                IJ.run(impChannelTime, "8-bit", "");
            }

            if (savingSettings.convertTo16Bit) {
                IJ.run(impChannelTime, "16-bit", "");
            }

            // Bin, project and save
            //
            String[] binnings = savingSettings.bin.split(";");
            for (String binning : binnings) {

                if (SaveCentral.interruptSavingThreads) {
                    logger.progress("Stopped saving thread: ", "" + t);
                    return;
                }

                String newPath = savingSettings.filePath;

                ImagePlus impBinned = impChannelTime;
                int[] binningA = Utils.delimitedStringToIntegerArray(binning, ",");
                if (binningA[0] > 1 || binningA[1] > 1 || binningA[2] > 1) {
                    Binner binner = new Binner();
                    impBinned = binner.shrink(impChannelTime, binningA[0], binningA[1], binningA[2], Binner.AVERAGE);
                    newPath = savingSettings.filePath + "--bin-" + binningA[0] + "-" + binningA[1] + "-" + binningA[2];
                }


                // Save volume
                //
                if (savingSettings.saveVolume) {
                    saveAsTiff(impBinned, c, t, savingSettings.compression, savingSettings.rowsPerStrip, newPath);

                }
                // Save projections
                // TODO: save into one single file
                if (savingSettings.saveProjection) {
                    saveAsTiffXYZMaxProjection(impBinned, c, t, newPath);
                }

            }
            if (!SaveCentral.interruptSavingThreads) {
                SaveImgHelper.documentProgress(totalCubes, counter, logger, startTime);
            }
        }
    }

    private void saveAsTiff(ImagePlus imp, int c, int t, String compression, int rowsPerStrip, String path) {

        if (compression.equals("LZW")) // Use BioFormats
        {

            String sC = String.format("%1$02d", c);
            String sT = String.format("%1$05d", t);
            String pathCT = path + "--C" + sC + "--T" + sT + ".ome.tif";

            try {
                ServiceFactory factory = new ServiceFactory();
                OMEXMLService service = factory.getInstance(OMEXMLService.class);
                IMetadata omexml = service.createOMEXMLMetadata();
                omexml.setImageID("Image:0", 0);
                omexml.setPixelsID("Pixels:0", 0);
                omexml.setPixelsBinDataBigEndian(Boolean.TRUE, 0, 0);
                omexml.setPixelsDimensionOrder(DimensionOrder.XYZCT, 0);
                if (imp.getBytesPerPixel() == 2) {
                    omexml.setPixelsType(PixelType.UINT16, 0);
                } else if (imp.getBytesPerPixel() == 1) {
                    omexml.setPixelsType(PixelType.UINT8, 0);
                }
                omexml.setPixelsSizeX(new PositiveInteger(imp.getWidth()), 0);
                omexml.setPixelsSizeY(new PositiveInteger(imp.getHeight()), 0);
                omexml.setPixelsSizeZ(new PositiveInteger(imp.getNSlices()), 0);
                omexml.setPixelsSizeC(new PositiveInteger(1), 0);
                omexml.setPixelsSizeT(new PositiveInteger(1), 0);

                int channel = 0;
                omexml.setChannelID("Channel:0:" + channel, 0, channel);
                omexml.setChannelSamplesPerPixel(new PositiveInteger(1), 0, channel);

                ImageWriter writer = new ImageWriter();
                writer.setCompression(TiffWriter.COMPRESSION_LZW);
                writer.setValidBitsPerPixel(imp.getBytesPerPixel() * 8);
                writer.setMetadataRetrieve(omexml);
                writer.setId(pathCT);
                writer.setWriteSequentially(true); // ? is this necessary
                TiffWriter tiffWriter = (TiffWriter) writer.getWriter();
                long[] rowsPerStripArray = new long[1];
                rowsPerStripArray[0] = rowsPerStrip;

                for (int z = 0; z < imp.getNSlices(); z++) {
                    if (SaveCentral.interruptSavingThreads) {
                        logger.progress("Stopped saving thread: ", "" + t);
                        savingSettings.saveProjection = false;
                        return;
                    }

                    IFD ifd = new IFD();
                    ifd.put(IFD.ROWS_PER_STRIP, rowsPerStripArray);
                    //tiffWriter.saveBytes(z, Bytes.fromShorts((short[])image.getStack().getProcessor(z+1).getPixels(), false), ifd);
                    if (imp.getBytesPerPixel() == 2) {
                        tiffWriter.saveBytes(z, ShortToByteBigEndian((short[]) imp.getStack().getProcessor(z + 1).getPixels()), ifd);
                    } else if (imp.getBytesPerPixel() == 1) {
                        tiffWriter.saveBytes(z, (byte[]) (imp.getStack().getProcessor(z + 1).getPixels()), ifd);

                    }
                }
                writer.close();

            } catch (Exception e) {
                logger.error(e.toString());
            }
        } else{  // no compression: use ImageJ's FileSaver, as it is faster than BioFormats
            if (SaveCentral.interruptSavingThreads) {
                savingSettings.saveProjection = false;
                logger.progress("Stopped saving thread: ", "" + t);
                return;
            }
            FileSaver fileSaver = new FileSaver(imp);
            String sC = String.format("%1$02d", c);
            String sT = String.format("%1$05d", t);
            String pathCT = path + "--C" + sC + "--T" + sT + ".tif";
            //logger.info("Saving " + pathCT);
            fileSaver.saveAsTiffStack(pathCT);
        }
    }

    private static byte[] ShortToByteBigEndian(short[] input) { //TODO: May be this can goto a new SaveHelper class
        int short_index, byte_index;
        int iterations = input.length;

        byte[] buffer = new byte[input.length * 2];

        short_index = byte_index = 0;

        for (/*NOP*/; short_index != iterations; /*NOP*/) {
            // Big Endian: store higher byte first
            buffer[byte_index] = (byte) ((input[short_index] & 0xFF00) >> 8);
            buffer[byte_index + 1] = (byte) (input[short_index] & 0x00FF);

            ++short_index;
            byte_index += 2;
        }
        return buffer;
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

    public static void saveAsTiffXYZMaxProjection(ImagePlus imp, int c, int t, String path) {
        ProjectionXYZ projectionXYZ = new ProjectionXYZ(imp);
        projectionXYZ.setDoscale(false);
        ImagePlus projection = projectionXYZ.createProjection();

        FileSaver fileSaver = new FileSaver(projection);
        String sC = String.format("%1$02d", c);
        String sT = String.format("%1$05d", t);
        String pathCT = path + "--xyz-max-projection" + "--C" + sC + "--T" + sT + ".tif";
        fileSaver.saveAsTiff(pathCT);
    }

}
