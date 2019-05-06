package de.embl.cba.bdp2.saving;

import ch.systemsx.cisd.hdf5.hdf5lib.HDF5Constants;
import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.logging.Logger;
import de.embl.cba.bdp2.utils.DimensionOrder;
import de.embl.cba.bdp2.utils.Utils;
import ij.ImagePlus;
import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.exceptions.HDF5Exception;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.ImgView;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedIntType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static ch.systemsx.cisd.hdf5.hdf5lib.HDF5Constants.*;
import static java.lang.Long.min;

@Deprecated
public class SaveImgAsHDF5Stacks<T extends RealType<T> & NativeType<T>> implements Runnable {
    private final ImgPlus<T> image;
    private static final int RANK = 3;
    private final int nFrames;
    private final int nChannels;
    private final int nZ;
    private int nRows;
    private int nCols;
    private String dataset;
    private int compressionLevel;
    private int fileId = -1;
    private int dataspaceId = -1;
    private int datasetId = -1;
    private int dcplId = -1;
    private final long[] maxDims = {
            HDF5Constants.H5S_UNLIMITED,
            HDF5Constants.H5S_UNLIMITED,
            HDF5Constants.H5S_UNLIMITED
    };
    private final int current_t;
    private int current_c;
    private boolean gate;
    private int gateMin;
    private int gateMax;
    private SavingSettings savingSettings;
    private AtomicInteger counter;
    private final long startTime;
    private final T nativeType;
    private AtomicBoolean stop;

    public SaveImgAsHDF5Stacks(String dataset, SavingSettings savingSettings, int t, AtomicInteger counter, long startTime, AtomicBoolean stop) {
        this.nativeType = (T) Util.getTypeFromInterval(savingSettings.rai );
        Img imgTemp = ImgView.wrap(savingSettings.rai, new CellImgFactory<>(nativeType));
        this.image = new ImgPlus<>(imgTemp, "", FileInfos.AXES_ORDER);

        if (this.image.dimensionIndex(Axes.TIME) >= 0) {
            this.nFrames = Math.toIntExact(image.dimension(this.image.dimensionIndex(Axes.TIME)));
        } else {
            this.nFrames = 1;
        }
        if (this.image.dimensionIndex(Axes.CHANNEL) >= 0) {
            this.nChannels = Math.toIntExact(image.dimension(this.image.dimensionIndex(Axes.CHANNEL)));
        } else {
            this.nChannels = 1;
        }
        if (this.image.dimensionIndex(Axes.Z) >= 0) {
            this.nZ = Math.toIntExact(image.dimension(this.image.dimensionIndex(Axes.Z)));
        } else {
            this.nZ = 1;
        }
        if (this.image.dimensionIndex(Axes.X) < 0 || this.image.dimensionIndex(Axes.Y) < 0) {
            throw new IllegalArgumentException("image must have X and Y dimensions!");
        }
        this.gate = savingSettings.gate;
        this.nRows = Math.toIntExact(image.dimension(this.image.dimensionIndex(Axes.Y)));
        this.nCols = Math.toIntExact(image.dimension(this.image.dimensionIndex(Axes.X)));
        this.dataset = dataset;
        this.compressionLevel = savingSettings.compressionLevel;
        this.current_t = t;
        this.savingSettings = savingSettings;
        this.gateMax = savingSettings.gateMax;
        this.gateMin = savingSettings.gateMin;
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

//        long freeMemoryInBytes = IJ.maxMemory() - IJ.currentMemory();
//        long numBytesOfImage = image.dimension(FileInfoConstants.X) *
//                image.dimension(FileInfoConstants.Y) *
//                image.dimension(FileInfoConstants.Z) *
//                image.dimension(FileInfoConstants.C) *
//                image.dimension(FileInfoConstants.T) *
//                files.bitDepth/8;
//
//        if (numBytesOfImage > 1.5 * freeMemoryInBytes) {
//            // TODO: do something...
//        }
        final long totalSlices = nFrames * nChannels;
        RandomAccessibleInterval image = savingSettings.rai;
        for (int c = 0; c < this.nChannels; c++) {
            if (stop.get()) {
                Logger.progress("Stopped saving thread: ", "" + this.current_t);
                return;
            }
            // Load
            //   ImagePlus impChannelTime = getDataCube( c );  May be faster???
            long[] minInterval = new long[]{
                    image.min( DimensionOrder.X ),
                    image.min( DimensionOrder.Y ),
                    image.min( DimensionOrder.Z ),
                    c,
                    this.current_t};
            long[] maxInterval = new long[]{
                    image.max( DimensionOrder.X ),
                    image.max( DimensionOrder.Y ),
                    image.max( DimensionOrder.Z ),
                    c,
                    this.current_t};
            RandomAccessibleInterval newRai = Views.interval(image, minInterval, maxInterval);
            // Convert
            newRai = SaveImgHelper.convertor(newRai, this.savingSettings);
            Img<T> imgChannelTime;
            imgChannelTime = ImgView.wrap(newRai, new CellImgFactory(this.nativeType));

            // Bin, project and save
            //
            String[] binnings = savingSettings.bin.split(";");
            for (String binning : binnings) {

                if (stop.get()) {
                    Logger.progress("Stopped saving thread @ merge: ", "" + current_t);
                    return;
                }
                String newPath = savingSettings.volumesFilePath;
                // Binning
                // - not for imarisH5 saving format as there will be a resolution pyramid anyway
                //
                Img<T> imgBinned = imgChannelTime;
                ImgPlus<T> impBinned = new ImgPlus<>(imgBinned, "", FileInfos.AXES_ORDER);
                int[] binningA = Utils.delimitedStringToIntegerArray(binning, ",");
                if (binningA[0] > 1 || binningA[1] > 1 || binningA[2] > 1) {
                    newPath = SaveImgHelper.doBinning(impBinned, binningA, newPath, null);
                }
                String sC = String.format("%1$02d", c);
                String sT = String.format("%1$05d", current_t);
                newPath = newPath + "--C" + sC + "--T" + sT + ".h5";

                if (savingSettings.saveVolumes ) {
                    this.current_c = c;
                    writeHDF5(impBinned, newPath);
                }

                // Save projections
                if (savingSettings.saveProjections ) {
                    ImagePlus imagePlusImage = ImageJFunctions.wrap(newRai, "", null);
                    SaveImgAsTIFFStacks.saveAsTiffXYZMaxProjection(imagePlusImage, c, this.current_t, newPath);
                }

                counter.incrementAndGet();
            }
            if (!stop.get()) {
                SaveImgHelper.documentProgress(totalSlices, counter, startTime);
            }
        }
    }


    private void writeHDF5(ImgPlus<T> imgBinned, String filename) {
        long[] chunk_dims = {min(nZ, 256),
                min(nRows, 256),
                min(nCols, 256)
        };
        Logger.info("Export Dimensions in xyczt: " + String.valueOf(nCols) + "x" + String.valueOf(nRows) + "x" + String.valueOf(nChannels) + "x" +
                String.valueOf(nFrames) + "x" + String.valueOf(nZ));

        try {
            fileId = H5.H5Fcreate(filename, H5F_ACC_TRUNC, H5P_DEFAULT, H5P_DEFAULT);
            dcplId = H5.H5Pcreate(H5P_DATASET_CREATE);
            H5.H5Pset_chunk(dcplId, RANK, chunk_dims);
            H5.H5Pset_deflate(dcplId, compressionLevel);

            T val = imgBinned.firstElement();
            if (val instanceof UnsignedByteType) {
                Logger.info("Writing uint 8.");
                writeIndividualChannels(imgBinned, H5T_NATIVE_UINT8);
            } else if (val instanceof UnsignedShortType) {
                Logger.info("Writing uint 16.");
                writeIndividualChannels(imgBinned, H5T_NATIVE_UINT16);
            } else if (val instanceof UnsignedIntType) {
                Logger.info("Writing uint 32.");
                writeIndividualChannels(imgBinned, H5T_NATIVE_UINT32);
            } else if (val instanceof FloatType) {
                Logger.info("Writing float 32.");
                writeIndividualChannels(imgBinned, H5T_NATIVE_FLOAT);
            } else {
                Logger.error("Type Not handled yet!" + val.getClass());
                throw new IllegalArgumentException("Unsupported Type: " + val.getClass());
            }
        } catch (HDF5Exception err) {
            Logger.error("HDF5_STACKS API error occurred while creating '" + filename + "'." + err.getMessage());
            throw new RuntimeException(err);
        } catch (Exception err) {
            Logger.error("An unexpected error occurred while creating '" + filename + "'." + err.getMessage());
            throw new RuntimeException(err);
        } catch (OutOfMemoryError o) {
            Logger.error("Out of Memory Error while creating '" + filename + "'." + o.getMessage());
            throw new RuntimeException(o);
        } finally {
            H5.H5Sclose(dataspaceId);
            H5.H5Pclose(dcplId);
            H5.H5Dclose(datasetId);
            H5.H5Fclose(fileId);
        }
    }

    private void writeIndividualChannels(ImgPlus<T> imgBinned, int hdf5DataType) {

        long[] channelDims = new long[RANK];
        channelDims[0] = nZ; //z
        channelDims[1] = nRows; // y
        channelDims[2] = nCols; // x

        long[] iniDims = new long[RANK];
        iniDims[0] = 1;
        iniDims[1] = nRows;
        iniDims[2] = nCols;

        try {
            dataspaceId = H5.H5Screate_simple(RANK, iniDims, maxDims);
            datasetId = H5.H5Dcreate(fileId, dataset, hdf5DataType, dataspaceId, H5P_DEFAULT, dcplId, H5P_DEFAULT);
        } catch (HDF5Exception ex) {
            Logger.error("H5D dataspace creation failed." + ex.getMessage());
            throw new RuntimeException(ex);
        } catch (Exception err) {
            Logger.error("An error occurred at writeIndividualChannels method." + err.getMessage());
            throw new RuntimeException(err);
        }

        RandomAccess<T> rai = imgBinned.randomAccess();
        Object[][] pixelSlice;
        H5.H5Dset_extent(datasetId, channelDims);

        if (image.dimensionIndex(Axes.TIME) >= 0)
            rai.setPosition(this.current_t, image.dimensionIndex(Axes.TIME));
        if (image.dimensionIndex(Axes.CHANNEL) >= 0)
            rai.setPosition(this.current_c, image.dimensionIndex(Axes.CHANNEL));

        for (int z = 0; z < nZ; z++) {
            if (image.dimensionIndex(Axes.Z) >= 0) {
                rai.setPosition(z, image.dimensionIndex(Axes.Z));
            }
            // Construct 2D array of appropriate data type.
            if (hdf5DataType == H5T_NATIVE_UINT8) {
                pixelSlice = new Byte[nRows][nCols];
            } else if (hdf5DataType == H5T_NATIVE_UINT16) {
                pixelSlice = new Short[nRows][nCols];
            } else if (hdf5DataType == H5T_NATIVE_UINT32) {
                pixelSlice = new Integer[nRows][nCols];
            } else if (hdf5DataType == H5T_NATIVE_FLOAT) {
                pixelSlice = new Float[nRows][nCols];
            } else {
                throw new IllegalArgumentException("Trying to save dataset of unknown datatype.");
            }

            if (stop.get()) {
                savingSettings.saveProjections = false;
                Logger.progress("Stopped saving thread @ writeIndividualChannels: ", "" + current_t);
                return;
            }

            fillStackSlice(rai, pixelSlice);
            long[] start = {z, 0, 0};
            writeHyperslabs(hdf5DataType, pixelSlice, start, iniDims);
        }
        Logger.info("compressionLevel: " + String.valueOf(compressionLevel));
        Logger.info("Finished writing the HDF5_STACKS.");
    }

    private <E> void writeHyperslabs(int hdf5DataType, E[][] pixelsSlice, long[] start, long[] colorIniDims) {
        if (stop.get()) {
            savingSettings.saveProjections = false;
            Logger.progress("Stopped saving thread @ writeHyperslabs: ", "" + current_t);
            return;
        }

        try {
            dataspaceId = H5.H5Dget_space(datasetId);
            H5.H5Sselect_hyperslab(dataspaceId, HDF5Constants.H5S_SELECT_SET, start, null, colorIniDims, null);
            int memSpace = H5.H5Screate_simple(RANK, colorIniDims, null);
            H5.H5Dwrite(datasetId, hdf5DataType, memSpace, dataspaceId, H5P_DEFAULT, pixelsSlice);
        } catch (HDF5Exception e) {
            Logger.error("Error while writing extended hyperslabs." + e.getMessage());
            throw new RuntimeException(e);
        } catch (Exception e) {
            Logger.error("An error occurred at writeHyperslabs method." + e.getMessage());
            throw new RuntimeException(e);
        }

    }

    @SuppressWarnings({"unchecked", "TypeParameterHidesVisibleType"})
    private <E, T> void fillStackSlice(RandomAccess<T> rai, E[][] pixelArray) {
        for (int x = 0; x < nCols; x++) {
            rai.setPosition(x, image.dimensionIndex(Axes.X));
            for (int y = 0; y < nRows; y++) {
                rai.setPosition(y, image.dimensionIndex(Axes.Y));
                T value = rai.get();

                if (stop.get()) {
                    savingSettings.saveProjections = false;
                    Logger.progress("Stopped saving thread @ fillStackSlice: ", "" + current_t);
                    return;
                }

                if (value instanceof UnsignedByteType) {
                    if (this.gate) {
                        int v = (Integer.valueOf(((UnsignedByteType) value).get()).byteValue()) & 0xff;
                        pixelArray[y][x] = ((v < this.gateMin) || (v > this.gateMax)) ? (E) (Byte) (Integer.valueOf(0).byteValue()) : (E) (Byte) (Integer.valueOf(((UnsignedByteType) value).get()).byteValue());
                    } else {
                        pixelArray[y][x] = (E) (Byte) (Integer.valueOf(((UnsignedByteType) value).get()).byteValue());
                    }
                } else if (value instanceof UnsignedShortType) {
                    if (this.gate) {
                        int v = (Integer.valueOf((((UnsignedShortType) value).get())).shortValue()) & 0xffff;
                        pixelArray[y][x] = ((v < this.gateMin) || (v > this.gateMax)) ? (E) (Short) (Integer.valueOf(0).shortValue()) : (E) (Short) (Integer.valueOf((((UnsignedShortType) value).get())).shortValue());
                    } else {
                        pixelArray[y][x] = (E) (Short) (Integer.valueOf((((UnsignedShortType) value).get())).shortValue());
                    }
                } else if (value instanceof FloatType) {
                    pixelArray[y][x] = (E) (Float.valueOf((((FloatType) value).get())));
                } else {
                    Logger.error("Type Not handled yet!");
                }
            }
        }
    }
}
