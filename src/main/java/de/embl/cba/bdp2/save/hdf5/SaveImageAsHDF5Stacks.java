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
package de.embl.cba.bdp2.save.hdf5;

import ch.systemsx.cisd.hdf5.hdf5lib.HDF5Constants;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.open.fileseries.FileInfos;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.save.Projector;
import de.embl.cba.bdp2.save.SaveImgHelper;
import de.embl.cba.bdp2.save.SavingSettings;
import de.embl.cba.bdp2.utils.DimensionOrder;
import de.embl.cba.bdp2.utils.Utils;
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
public class SaveImageAsHDF5Stacks < R extends RealType< R > & NativeType< R > > implements Runnable {
    private final ImgPlus< R > imgPlus;
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
    private final R nativeType;
    private final Image< R > image;
    private AtomicBoolean stop;

    public SaveImageAsHDF5Stacks( String dataset, Image< R > image, SavingSettings savingSettings, int t, AtomicInteger counter, long startTime, AtomicBoolean stop) {
        this.nativeType = ( R ) Util.getTypeFromInterval(image.getRai() );
        this.image = image;
        Img imgTemp = ImgView.wrap(image.getRai(), new CellImgFactory<>(nativeType));
        this.imgPlus = new ImgPlus<>(imgTemp, "", FileInfos.AXES_ORDER);

        if (this.imgPlus.dimensionIndex(Axes.TIME) >= 0) {
            this.nFrames = Math.toIntExact( imgPlus.dimension(this.imgPlus.dimensionIndex(Axes.TIME)));
        } else {
            this.nFrames = 1;
        }
        if (this.imgPlus.dimensionIndex(Axes.CHANNEL) >= 0) {
            this.nChannels = Math.toIntExact(imgPlus.dimension(this.imgPlus.dimensionIndex(Axes.CHANNEL)));
        } else {
            this.nChannels = 1;
        }
        if (this.imgPlus.dimensionIndex(Axes.Z) >= 0) {
            this.nZ = Math.toIntExact(imgPlus.dimension(this.imgPlus.dimensionIndex(Axes.Z)));
        } else {
            this.nZ = 1;
        }
        if (this.imgPlus.dimensionIndex(Axes.X) < 0 || this.imgPlus.dimensionIndex(Axes.Y) < 0) {
            throw new IllegalArgumentException("image must have X and Y dimensions!");
        }
        this.gate = savingSettings.gate;
        this.nRows = Math.toIntExact(imgPlus.dimension(this.imgPlus.dimensionIndex(Axes.Y)));
        this.nCols = Math.toIntExact(imgPlus.dimension(this.imgPlus.dimensionIndex(Axes.X)));
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
//                file.bitDepth/8;
//
//        if (numBytesOfImage > 1.5 * freeMemoryInBytes) {
//            // TODO: do something...
//        }
        final long totalSlices = nFrames * nChannels;
        RandomAccessibleInterval rai = image.getRai();
        for (int c = 0; c < this.nChannels; c++) {
            if (stop.get()) {
                Logger.progress("Stopped save thread: ", "" + this.current_t);
                return;
            }
            // Load
            //   ImagePlus impChannelTime = getDataCube( c );  May be faster???
            long[] minInterval = new long[]{
                    rai.min( DimensionOrder.X ),
                    rai.min( DimensionOrder.Y ),
                    rai.min( DimensionOrder.Z ),
                    c,
                    this.current_t};
            long[] maxInterval = new long[]{
                    rai.max( DimensionOrder.X ),
                    rai.max( DimensionOrder.Y ),
                    rai.max( DimensionOrder.Z ),
                    c,
                    this.current_t};
            RandomAccessibleInterval newRai = Views.interval(rai, minInterval, maxInterval);
            // Convert
            newRai = SaveImgHelper.converter(newRai, this.savingSettings);
            Img< R > imgChannelTime;
            imgChannelTime = ImgView.wrap(newRai, new CellImgFactory(this.nativeType));

            // Bin, project and save
            //
            String[] binnings = savingSettings.bin.split(";");
            for (String binning : binnings) {

                if (stop.get()) {
                    Logger.progress("Stopped save thread @ merge: ", "" + current_t);
                    return;
                }
                String newPath = savingSettings.volumesFilePathStump;
                // Binning
                // - not for imarisH5 save format as there will be a resolution pyramid anyway
                //
                Img< R > imgBinned = imgChannelTime;
                ImgPlus< R > impBinned = new ImgPlus<>(imgBinned, "", FileInfos.AXES_ORDER);
                int[] binningA = Utils.delimitedStringToIntegerArray(binning, ",");
                if (binningA[0] > 1 || binningA[1] > 1 || binningA[2] > 1) {
                    newPath = SaveImgHelper.doBinning(impBinned, binningA, newPath, null);
                }
                String sC = String.format("%1$02d", c);
                String sT = String.format("%1$05d", current_t);
                newPath = newPath + "--C" + sC + "--T" + sT + ".h5";

                if ( savingSettings.saveVolumes ) {
                    this.current_c = c;
                    writeHDF5(impBinned, newPath);
                }

                // Save projections
                if ( savingSettings.saveProjections )
                {
                    this.current_c = c;
                    Projector.saveProjections( ImageJFunctions.wrap( newRai, image.getName() ), this.current_c, this.current_t, newPath, savingSettings.projectionMode );
                }

                counter.incrementAndGet();
            }

//            if (!stop.get()) {
//                ProgressHelpers.logProgress( totalSlices, counter, startTime, "Saved file ");
//            }
        }
    }


    private void writeHDF5( ImgPlus< R > imgBinned, String filename) {
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

            R val = imgBinned.firstElement();
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

    private void writeIndividualChannels( ImgPlus< R > imgBinned, int hdf5DataType) {

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

        RandomAccess< R > rai = imgBinned.randomAccess();
        Object[][] pixelSlice;
        H5.H5Dset_extent(datasetId, channelDims);

        if ( imgPlus.dimensionIndex(Axes.TIME) >= 0)
            rai.setPosition(this.current_t, imgPlus.dimensionIndex(Axes.TIME));
        if ( imgPlus.dimensionIndex(Axes.CHANNEL) >= 0)
            rai.setPosition(this.current_c, imgPlus.dimensionIndex(Axes.CHANNEL));

        for (int z = 0; z < nZ; z++) {
            if ( imgPlus.dimensionIndex(Axes.Z) >= 0) {
                rai.setPosition(z, imgPlus.dimensionIndex(Axes.Z));
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
                Logger.progress("Stopped save thread @ writeIndividualChannels: ", "" + current_t);
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
            Logger.progress("Stopped save thread @ writeHyperslabs: ", "" + current_t);
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
            rai.setPosition(x, imgPlus.dimensionIndex(Axes.X));
            for (int y = 0; y < nRows; y++) {
                rai.setPosition(y, imgPlus.dimensionIndex(Axes.Y));
                T value = rai.get();

                if (stop.get()) {
                    savingSettings.saveProjections = false;
                    Logger.progress("Stopped save thread @ fillStackSlice: ", "" + current_t);
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
