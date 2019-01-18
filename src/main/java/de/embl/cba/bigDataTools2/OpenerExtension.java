/* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as
* published by the Free Software Foundation, either version 3 of the
* License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*/

package de.embl.cba.bigDataTools2;

import ch.systemsx.cisd.base.mdarray.MDByteArray;
import ch.systemsx.cisd.base.mdarray.MDShortArray;
import ch.systemsx.cisd.hdf5.HDF5DataSetInformation;
import ch.systemsx.cisd.hdf5.HDF5DataTypeInformation;
import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5Reader;
import de.embl.cba.bigDataTools2.fileInfoSource.SerializableFileInfo;
import de.embl.cba.bigDataTools2.logging.IJLazySwingLogger;
import de.embl.cba.bigDataTools2.logging.Logger;
import de.embl.cba.bigDataTools2.utils.Utils;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.io.BitBuffer;
import ij.io.Opener;
import javafx.geometry.Point3D;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class OpenerExtension extends Opener {

    // TODO: make it NOT an extension

    Logger logger = new IJLazySwingLogger();

    public OpenerExtension()
    {

    }
/*
    public void loadDataIntoCell(SingleCellArrayImg cell, directory, SerializableFileInfo[] info, ExecutorService executorService) {
        // TODO: implement :-)
    }
*/
    public ImagePlus readDataCube(String directory, SerializableFileInfo[] info, int dz, Point3D po, Point3D ps, ExecutorService executorService)
    {

        // compute ranges to be loaded
        int xs = (int) (po.getX() + 0.5);
        int ys = (int) (po.getY() + 0.5);
        int zs = (int) (po.getZ() + 0.5);
        int xe = xs + (int) (ps.getX() + 0.5) - 1;
        int ye = ys + (int) (ps.getY() + 0.5) - 1;
        int ze = zs + (int) (ps.getZ() + 0.5) - 1;

        int nz = ze - zs + 1;

        if (dz > 1) {
            nz = (int) (1.0 * nz / dz + 0.5);
        }

        ImagePlus imp = null;

        if(info[zs].fileTypeString.equals(Utils.FileType.TIFF_STACKS.toString()))
        {
            imp = readDataCubeFromTiff(directory, info, executorService, zs, ze, nz, dz, xs, xe, ys, ye);
        }
        else if(info[zs].fileTypeString.equals(Utils.FileType.SINGLE_PLANE_TIFF.toString()))
        {
            imp = readDataCubeFromTiff(directory, info, executorService, zs, ze, nz, dz, xs, xe, ys, ye);
        }
        else if(info[zs].fileTypeString.equals(Utils.FileType.HDF5.toString()))
        {
            imp = readDataCubeFromHdf5(directory, info, zs, ze, nz, dz, xs, xe, ys, ye);
        }
        else
        {
            logger.error("unsupported file type: " + info[0].fileTypeString);
        }

        return(imp);

    }

    public ImagePlus readDataCubeFromHdf5(String directory, SerializableFileInfo[] info,
                                          int zs, int ze, int nz, int dz,
                                          int xs, int xe, int ys, int ye)
    {

        if (info == null) {
            logger.error("FileInfo was empty; could not load data.");
            return null;
        }

        SerializableFileInfo fi = info[0];

        ImagePlus imp;

        if ( fi.bytesPerPixel == 1 )
        {
            imp = read8bitDataCubeFromHdf5( directory, info,
                    zs,  ze,  nz,  dz, xs,  xe,  ys,  ye);
        }
        else
        {
            imp = read16bitDataCubeFromHdf5( directory, info,
                    zs,  ze,  nz,  dz, xs,  xe,  ys,  ye);
        }

        return ( imp );
    }


    public ImagePlus read16bitDataCubeFromHdf5(String directory, SerializableFileInfo[] info,
                                               int zs, int ze, int nz, int dz,
                                               int xs, int xe, int ys, int ye)
    {
        long settingTime = 0, readingPixelsTime = 0, readingInitTime = 0, totalTime = 0, allocationTime = 0;

        totalTime = System.currentTimeMillis();

        allocationTime = System.currentTimeMillis();

        SerializableFileInfo fi = info[0];

        int nx = xe - xs + 1;
        int ny = ye - ys + 1;

        if ( logger.isShowDebug() ) {
            logger.info("# readDataCubeFromHdf5");
            logger.info("root directory: " + directory);
            logger.info("fi.directory: " + fi.directory);
            logger.info("fi.filename: " + fi.fileName);
            logger.info("info.length: " + info.length);
            logger.info("zs,dz,ze,nz,xs,xe,ys,ye: " + zs + "," + dz + "," + ze + "," + nz + "," + xs + "," +
                    xe + "," + ys + "," + ye);
        }

        short[] asFlatArray = null, pixels;
        MDShortArray block = null;
        int imShortSize = nx * ny;

        // Allocate the stack
        ImageStack stack = ImageStack.create(nx, ny, nz, fi.bytesPerPixel * 8);
        ImagePlus imp = new ImagePlus("cropped", stack);

        long maxSize = (1L << 31) - 1;
        long nPixels = (long) nx * ny * nz;
        boolean readInOneGo = true;
        if (nPixels > maxSize) {
            logger.info("H5 Loader: nPixels > 2^31 => reading plane wise (=> slower!).");
            readInOneGo = false;
        }
        allocationTime = System.currentTimeMillis() - allocationTime;

        readingInitTime = System.currentTimeMillis();
        IHDF5Reader reader = HDF5Factory.openForReading(directory + fi.directory + fi.fileName);
        HDF5DataSetInformation dsInfo = reader.getDataSetInformation(fi.h5DataSet);
        String dsTypeString = hdf5InfoToString(dsInfo);
        readingInitTime = System.currentTimeMillis() - readingInitTime;

        if (dz == 1 && readInOneGo) {

            // read everything in one go
            //

            /*
            String filePath = directory + fi.directory + fi.fileName;
            String[] dsetNames = new String[] {fi.h5DataSet};
            int nFrames = 1;
            int nChannels = 1;
            */

            readingPixelsTime = System.currentTimeMillis();


            if ( dsTypeString.equals("int16") )
            {
                try
                {
                    block = reader.int16().readMDArrayBlockWithOffset(fi.h5DataSet, new int[]{nz, ny, nx}, new long[]{zs, ys, xs});
                }
                catch (Exception e)
                {
                    // 2-d data set
                    block = reader.int16().readMDArrayBlockWithOffset(fi.h5DataSet, new int[]{ny, nx}, new long[]{ys, xs});
                }
            }
            else if ( dsTypeString.equals("uint16") )
            {
                try
                {
                    block = reader.uint16().readMDArrayBlockWithOffset(fi.h5DataSet, new int[]{nz, ny, nx}, new long[]{zs, ys, xs});
                }
                catch (Exception e)
                {
                    // 2-d data set
                    block = reader.uint16().readMDArrayBlockWithOffset(fi.h5DataSet, new int[]{ny, nx}, new long[]{ys, xs});
                }
            }
            else
            {
                logger.error("Data type " + dsTypeString + " is currently not supported");
                return ( null );
            }
            readingPixelsTime = System.currentTimeMillis() - readingPixelsTime;

            // copy pixels plane-wise into stack
            settingTime = System.currentTimeMillis();
            asFlatArray = block.getAsFlatArray();
            for (int z = zs; z <= ze; z++) {
                pixels = (short[]) imp.getStack().getPixels(z - zs + 1);
                System.arraycopy(asFlatArray, (z - zs) * imShortSize, pixels, 0, imShortSize);
            }
            settingTime = System.currentTimeMillis() - settingTime;

        }
        else
        {
            // todo: make a fast version for too large data sets (check code from Ronnerberger)

            // read plane wise
            // - sub-sampling in z possible
            // - no java indexing issue for the asFlatArray
            int z = zs;
            for (int iz=1; iz<=nz; iz++, z+=dz)
            {
                if ( dsTypeString.equals("int16") )
                {
                    block = reader.int16().readMDArrayBlockWithOffset( fi.h5DataSet, new int[]{ 1, ny, nx }, new long[]{ z,
                            ys, xs } );
                }
                else if ( dsTypeString.equals("uint16") )
                {
                    block = reader.uint16().readMDArrayBlockWithOffset( fi.h5DataSet, new int[]{ 1, ny, nx }, new long[]{ z,
                            ys, xs } );
                }
                asFlatArray = block.getAsFlatArray();
                imp.getStack().setPixels(asFlatArray, iz);
                //pixels = (short[]) image.getStack().getPixels(iz);
                //System.arraycopy(asFlatArray, 0, pixels, 0, imShortSize);

            }
        }

        totalTime = (System.currentTimeMillis() - totalTime);

        if( logger.isShowDebug() ) {
            logger.info("h5 allocationTime [ms]: " + allocationTime);
            logger.info("h5 readingInitTime [ms]: " + readingInitTime);
            logger.info("h5 readingPixelsTime [ms]: " + readingPixelsTime);
            logger.info("h5 settingPixelsIntoImageStackTime [ms]: " + settingTime);
            logger.info("h5 totalTime [ms]: " + totalTime);
            logger.info("pixels read: " + asFlatArray.length);
            logger.info("effective reading speed [MB/s]: " + (double) nz * nx * ny * fi.bytesPerPixel / (
                    (totalTime + 0.001) * 1000));

        }

        return(imp);
    }


    public ImagePlus read8bitDataCubeFromHdf5(String directory, SerializableFileInfo[] info,
                                              int zs, int ze, int nz, int dz,
                                              int xs, int xe, int ys, int ye)
    {
        long settingTime = 0, readingPixelsTime = 0, readingInitTime = 0, totalTime = 0, allocationTime = 0;

        totalTime = System.currentTimeMillis();

        allocationTime = System.currentTimeMillis();

        SerializableFileInfo fi = info[0];

        int nx = xe - xs + 1;
        int ny = ye - ys + 1;

        if ( logger.isShowDebug() ) {
            logger.info("# readDataCubeFromHdf5");
            logger.info("root directory: " + directory);
            logger.info("fi.directory: " + fi.directory);
            logger.info("fi.filename: " + fi.fileName);
            logger.info("info.length: " + info.length);
            logger.info("zs,dz,ze,nz,xs,xe,ys,ye: " + zs + "," + dz + "," + ze + "," + nz + "," + xs + "," +
                    xe + "," + ys + "," + ye);
        }

        byte[] asFlatArray = null, pixels;
        MDByteArray block = null;
        int imSize = nx * ny;

        // Allocate the stack
        ImageStack stack = ImageStack.create(nx, ny, nz, fi.bytesPerPixel * 8);
        ImagePlus imp = new ImagePlus("cropped", stack);

        long maxSize = (1L << 31) - 1;
        long nPixels = (long) nx * ny * nz;
        boolean readInOneGo = true;
        if (nPixels > maxSize) {
            logger.info("H5 Loader: nPixels > 2^31 => reading plane wise (=> slower!).");
            readInOneGo = false;
        }
        allocationTime = System.currentTimeMillis() - allocationTime;


        readingInitTime = System.currentTimeMillis();
        IHDF5Reader reader = HDF5Factory.openForReading(directory + fi.directory + fi.fileName);
        HDF5DataSetInformation dsInfo = reader.getDataSetInformation(fi.h5DataSet);
        String dsTypeString = hdf5InfoToString(dsInfo);
        readingInitTime = System.currentTimeMillis() - readingInitTime;

        if (dz == 1 && readInOneGo) {

            // read everything in one go
            //

        /*
        String filePath = directory + fi.directory + fi.fileName;
        String[] dsetNames = new String[] {fi.h5DataSet};
        int nFrames = 1;
        int nChannels = 1;
        */

            readingPixelsTime = System.currentTimeMillis();


            if ( dsTypeString.equals("int8") )
            {
                try
                {
                    block = reader.int8().readMDArrayBlockWithOffset(fi.h5DataSet, new int[]{nz, ny, nx}, new long[]{zs, ys, xs});
                }
                catch (Exception e)
                {
                    // 2-d data set
                    block = reader.int8().readMDArrayBlockWithOffset(fi.h5DataSet, new int[]{ny, nx}, new long[]{ys, xs});
                }
            }
            else if ( dsTypeString.equals("uint8") )
            {
                try
                {
                    block = reader.uint8().readMDArrayBlockWithOffset(fi.h5DataSet, new int[]{nz, ny, nx}, new long[]{zs, ys, xs});
                }
                catch (Exception e)
                {
                    // 2-d data set
                    block = reader.uint8().readMDArrayBlockWithOffset(fi.h5DataSet, new int[]{ny, nx}, new long[]{ys, xs});
                }
            }
            else
            {
                logger.error("Data type " + dsTypeString + " is currently not supported");
                return ( null );
            }
            readingPixelsTime = System.currentTimeMillis() - readingPixelsTime;

            // copy pixels plane-wise into stack
            settingTime = System.currentTimeMillis();
            asFlatArray = block.getAsFlatArray();
            for (int z = zs; z <= ze; z++) {
                pixels = (byte[]) imp.getStack().getPixels(z - zs + 1);
                System.arraycopy(asFlatArray, (z - zs) * imSize, pixels, 0, imSize);
            }
            settingTime = System.currentTimeMillis() - settingTime;

        }
        else
        {
            // read plane wise
            // - sub-sampling in z possible
            // - no java indexing issue for the asFlatArray
            int z = zs;
            for (int iz=1; iz<=nz; iz++, z+=dz)
            {
                if ( dsTypeString.equals("int8") )
                {
                    block = reader.int8().readMDArrayBlockWithOffset(
                            fi.h5DataSet, new int[]{ 1, ny, nx }, new long[]{ z,
                                    ys, xs } );
                }
                else if ( dsTypeString.equals("uint8") )
                {
                    block = reader.uint8().readMDArrayBlockWithOffset(
                            fi.h5DataSet, new int[]{ 1, ny, nx }, new long[]{ z,
                                    ys, xs } );
                }
                asFlatArray = block.getAsFlatArray();
                imp.getStack().setPixels(asFlatArray, iz);
            }
        }

        totalTime = (System.currentTimeMillis() - totalTime);

        if( logger.isShowDebug() ) {
            logger.info("h5 allocationTime [ms]: " + allocationTime);
            logger.info("h5 readingInitTime [ms]: " + readingInitTime);
            logger.info("h5 readingPixelsTime [ms]: " + readingPixelsTime);
            logger.info("h5 settingPixelsIntoImageStackTime [ms]: " + settingTime);
            logger.info("h5 totalTime [ms]: " + totalTime);
            logger.info("pixels read: " + asFlatArray.length);
            logger.info("effective reading speed [MB/s]: " + (double) nz * nx * ny * fi.bytesPerPixel / (
                    (totalTime + 0.001) * 1000));

        }

        return(imp);
    }


    public ImagePlus readDataCubeFromTiff(String directory, SerializableFileInfo[] info,
                                          ExecutorService es,
                                          int zs, int ze, int nz, int dz,
                                          int xs, int xe, int ys, int ye)
    {
        long startTime;
        long readingTime = 0;
        long totalTime = 0;
        long threadInitTime = 0;
        //SerializableFileInfo fi;
        File file;

        if (info == null) return null;
        SerializableFileInfo fi = info[zs];

        int nx = xe - xs + 1;
        int ny = ye - ys + 1;

        if( logger.isShowDebug() ) {
              logger.info("# readDataCubeFromTiff");
              logger.info("root directory: " + directory);
              logger.info("info.length: " + info.length);
              logger.info("fi.directory: " + fi.directory);
              logger.info("fi.filename: " + fi.fileName);
              logger.info("fi.compression: " + fi.compression);
              logger.info("fi.intelByteOrder: " + fi.intelByteOrder);
              logger.info("fi.bytesPerPixel: " + fi.bytesPerPixel);
              logger.info("zs,dz,ze,nz,xs,xe,ys,ye: " + zs + "," + dz + "," + ze + "," + nz + "," + xs + "," + xe + "," + ys + "," + ye);
        }

        totalTime = System.currentTimeMillis();

        // initialisation and allocation
        int imByteWidth = fi.width * fi.bytesPerPixel;
        // todo: this is not necessary to allocate new, but could be filled
        ImageStack stack = ImageStack.create(nx, ny, nz, fi.bytesPerPixel * 8);
        byte[][] buffer = new byte[nz][1];


        try {

            if ( nz > 1 )
            {

                if ( null != es ) {
                    // read plane wise, multi-threaded
                    //
                    //ExecutorService es = Executors.newFixedThreadPool( numThreads );
                    List<Future> futures = new ArrayList<>();

                    for (int iz = 0, z = zs; iz < nz; iz++, z += dz) {

                        if (z < 0 || z >= info.length) {
                            logger.error("z = " + z + " is out of range.");
                            return null;
                        }

                        // Read, decompress, rearrange, crop X, and put into stack
                        //
                        futures.add(
                                es.submit(
                                        new readCroppedPlaneFromTiffIntoImageStack(directory, info, stack, buffer,
                                                z, zs, ze, dz, ys, ye, ny, xs, xe, nx, imByteWidth)
                                )
                        );
                    }

                    // wait until all z-planes are read
                    for (Future future : futures) {
                        future.get();
                    }
                    futures = null;
                    //es.shutdown();

                }else                {
                    // read with single thread (invoking no threads at all)
                    for (int iz = 0, z = zs; iz < nz; iz++, z += dz)
                    {

                        if (z < 0 || z >= info.length)
                        {
                            logger.error("z = " + z + " is out of range.");
                            return null;
                        }

                        new readCroppedPlaneFromTiffIntoImageStack(directory, info, stack, buffer,
                                                z, zs, ze, dz, ys, ye, ny, xs, xe, nx, imByteWidth).run();

                    }
                }


            }
            else // don't invoke a thread for just reading a single plane
            {
                int z = zs;
                new readCroppedPlaneFromTiffIntoImageStack(directory, info, stack, buffer,
                        z, zs, ze, dz, ys, ye, ny, xs, xe, nx, imByteWidth).run();
            }

        } catch (Exception e) {
            IJ.handleException(e);
        }

        ImagePlus imp = new ImagePlus("One stream", stack);

        if( logger.isShowDebug() )
        {
              int usefulBytesRead = nz*nx*ny*fi.bytesPerPixel;
              logger.info("readingTime [ms]: " + readingTime);
              logger.info("effective reading speed [MB/s]: " + usefulBytesRead / ((readingTime + 0.001) * 1000));
              logger.info("threadInitTime [ms]: " + threadInitTime);
              logger.info("totalTime [ms]: " + totalTime);
            //info("Processing [ms]: " + processTime);
        }

        return imp;
    }

    static String hdf5InfoToString(HDF5DataSetInformation dsInfo)
    {
        //
        // Code copied from Ronneberger
        //
        HDF5DataTypeInformation dsType = dsInfo.getTypeInformation();
        String typeText = "";

        if (dsType.isSigned() == false) {
            typeText += "u";
        }

        switch( dsType.getDataClass())
        {
            case INTEGER:
                typeText += "int" + 8*dsType.getElementSize();
                break;
            case FLOAT:
                typeText += "float" + 8*dsType.getElementSize();
                break;
            default:
                typeText += dsInfo.toString();
        }
        return typeText;
    }


    /** Decompresses and sorts data into an ImageStack **/
    class readCroppedPlaneFromTiffIntoImageStack implements Runnable
    {
        private Thread t;
        private String threadName;

        // todo: make the compression modes part of the fi object?

        // Compression modes
        public static final int COMPRESSION_UNKNOWN = 0;
        public static final int COMPRESSION_NONE = 1;
        public static final int LZW = 2;
        public static final int LZW_WITH_DIFFERENCING = 3;
        public static final int JPEG = 4;
        public static final int PACK_BITS = 5;
        public static final int ZIP = 6;
        private static final int CLEAR_CODE = 256;
        private static final int EOI_CODE = 257;

        /** 16-bit signed integer (-32768-32767). Imported signed images
         are converted to unsigned by adding 32768. */
        public static final int GRAY16_SIGNED = 1;

        /** 16-bit unsigned integer (0-65535). */
        public static final int GRAY16_UNSIGNED = 2;

        // uncompress
        // byte[][] symbolTable = new byte[4096][1];
        byte[][] symbolTable = new byte[16384][1]; // enlarged to be compatible with larger images

        // input
        ImageStack stack;
        byte[][] buffer;
        SerializableFileInfo[] info;
        SerializableFileInfo fi;
        RandomAccessFile in;
        private String directory;
        int z, zs, ze, dz, ys, ye, ny, xs, xe, nx, imByteWidth;


        readCroppedPlaneFromTiffIntoImageStack(String directory, SerializableFileInfo[] info, ImageStack stack, byte[][] buffer,
                                               int z, int zs, int ze, int dz,
                                               int ys, int ye, int ny,
                                               int xs, int xe, int nx,
                                               int imByteWidth)
        {
            threadName = ""+z;
            this.directory = directory;
            this.info = info;
            this.stack = stack;
            this.buffer = buffer;
            this.z = z;
            this.zs = zs;
            this.ze = ze;
            this.ys = ys;
            this.dz =  dz;
            this.ye = ye;
            this.ny = ny;
            this.xs = xs;
            this.xe = xe;
            this.nx = nx;
            this.imByteWidth = imByteWidth;
            //info("Creating readCroppedPlaneFromTiffIntoImageStack of slice: " +  threadName );
        }

        public void run() {

            RandomAccessFile inputStream = null;

            this.fi = info[z];

            if ( fi == null )
            {
                //logger.info("Missing file; providing pixels with zeros.");
                return; // leave pixels in the stack black
            }

            File file = new File(directory + fi.directory + fi.fileName);
            //File file = new File(directory + fi.fileName);
            try {
                inputStream = new RandomAccessFile(file, "r");

                if (inputStream == null) {
                    logger.error("Could not open file: " + fi.directory + fi.fileName);
                    throw new IllegalArgumentException("could not open file");
                }

                if((fi.compression!=0) && (fi.compression!=1) && (fi.compression!=2) && (fi.compression!=6)) {
                    logger.error("Tiff compression not implemented: fi.compression = " + fi.compression);
                    return;
                }

                //startTime = System.currentTimeMillis();
                buffer[(z-zs)/dz] = readCroppedPlaneFromTiff(fi, inputStream, ys, ye);
                //readingTime += (System.currentTimeMillis() - startTime);
                inputStream.close();

            } catch (Exception e) {
                IJ.handleException(e);
            }

            boolean hasStrips = false;

            if ((fi.stripOffsets != null && fi.stripOffsets.length > 1)) {
                hasStrips = true;
            }



            if(hasStrips) {

                // check what we have read
                int rps = fi.rowsPerStrip;
                int ss = ys / rps; // the int is doing a floor()
                int se = ye / rps;

                if( (fi.compression == COMPRESSION_NONE) ||
                        (fi.compression == 0) )
                {
                    // do nothing
                }
                else if (fi.compression == LZW)
                {

                    // init to hold all data present in the uncompressed strips
                    byte[] unCompressedBuffer = new byte[(se - ss + 1) * rps * imByteWidth];

                    int pos = 0;
                    for (int s = ss; s <= se; s++) {

                        // TODO: multithreading here?

                        int stripLength = (int)fi.stripLengths[s];
                        byte[] strip = new byte[stripLength];

                        // getDownsampledView strip from read data
                        try {
                            System.arraycopy(buffer[(z - zs)/dz], pos, strip, 0, stripLength);
                        } catch (Exception e) {
                              logger.info("" + e.toString());
                              logger.info("------- s [#] : " + s);
                              logger.info("stripLength [bytes] : " + strip.length);
                              logger.info("pos [bytes] : " + pos);
                              logger.info("pos + stripLength [bytes] : " + (pos + stripLength));
                              logger.info("z-zs : " + (z - zs));
                              logger.info("z-zs/dz : " + (z - zs) / dz);
                              logger.info("buffer[z-zs].length : " + buffer[z - zs].length);
                              logger.info("imWidth [bytes] : " + imByteWidth);
                              logger.info("rows per strip [#] : " + rps);
                              logger.info("ny [#] : " + ny);
                              logger.info("(s - ss) * imByteWidth * rps [bytes] : " + ((s - ss) * imByteWidth *
                                      rps));
                              logger.info("unCompressedBuffer.length [bytes] : " + unCompressedBuffer.length);
                        }

                        //info("strip.length " + strip.length);
                        // uncompress strip

                        strip = lzwUncompress(strip, imByteWidth * rps);

                        // put uncompressed strip into large array
                        System.arraycopy(strip, 0, unCompressedBuffer, (s - ss) * imByteWidth * rps, imByteWidth * rps);

                        pos += stripLength;
                    }

                    buffer[(z - zs)/dz] = unCompressedBuffer;

                } else {

                    logger.error("Tiff compression not implemented: fi.compression = " + fi.compression);
                    return;

                }

                ys = ys % rps; // we might have to skip a few rows in the beginning because the strips can hold several rows

            } else { // no strips

                if (fi.compression == ZIP) {

                    /** TIFF Adobe ZIP support contributed by Jason Newton. */
                    ByteArrayOutputStream imageBuffer = new ByteArrayOutputStream();
                    byte[] tmpBuffer = new byte[1024];
                    Inflater decompressor = new Inflater();

                    decompressor.setInput(buffer[(z - zs)/dz]);
                    try {
                        while(!decompressor.finished()) {
                            int rlen = decompressor.inflate(tmpBuffer);
                            imageBuffer.write(tmpBuffer, 0, rlen);
                        }
                    } catch(DataFormatException e){
                        IJ.log(e.toString());
                    }
                    decompressor.end();

                    buffer[(z - zs)/dz] = imageBuffer.toByteArray();

                    //setShortPixelsCropXY((short[]) stack.getPixels((z - zs)/dz + 1), ys, ny, xs, nx, imByteWidth, buffer[(z - zs)/dz]);

                } else {

                    ys = 0; // the buffer contains only the correct y-range
                    //setShortPixelsCropXY((short[]) stack.getPixels((z - zs)/dz + 1), ys, ny, xs, nx, imByteWidth, buffer[(z - zs)/dz]);

                }

                if( logger.isShowDebug() ) {
                      logger.info("z: " + z);
                      logger.info("zs: " + zs);
                      logger.info("dz: " + dz);
                      logger.info("(z - zs)/dz: " + (z - zs) / dz);
                      logger.info("buffer.length : " + buffer.length);
                      logger.info("buffer[z-zs].length : " + buffer[z - zs].length);
                      logger.info("imWidth [bytes] : " + imByteWidth);
                      logger.info("ny [#] : " + ny);
                }


            }

            //
            // Copy (crop of) xy data from buffer into image stack
            //

            if ( fi.bytesPerPixel == 1 )
            {
                setBytePixelsCropXY( (byte[])stack.getPixels((z - zs) / dz + 1), ys, ny, xs, nx, imByteWidth, buffer[(z - zs)/dz]);
            }
            else if ( fi.bytesPerPixel == 2 )
            {
                setShortPixelsCropXY( (short[])stack.getPixels((z - zs)/dz + 1), ys, ny, xs, nx, imByteWidth, buffer[(z - zs)/dz]);
            }
            else
            {
                logger.error("Unsupported bit depth.");
                return;
            }


        }

        public byte[] lzwUncompress(byte[] input, int byteCount) {
            long startTimeGlob = System.nanoTime();
            long totalTimeGlob = 0;
            long startTime0, totalTime0 = 0;
            long startTime1, totalTime1 = 0;
            long startTime2, totalTime2 = 0;
            long startTime3, totalTime3 = 0;
            long startTime4, totalTime4 = 0;
            long startTime5, totalTime5 = 0;
            long startTime6, totalTime6 = 0;
            long startTime7, totalTime7 = 0;
            long startTime8, totalTime8 = 0;
            long startTime9, totalTime9 = 0;

            //startTime1 = System.nanoTime();

            if (input==null || input.length==0)
                return input;

            int bitsToRead = 9;
            int nextSymbol = 258;
            int code;
            int symbolLength, symbolLengthMax=0;
            int oldCode = -1;
            //ByteVector out = new ByteVector(8192);
            byte[] out = new byte[byteCount];
            int iOut = 0, i;
            int k=0;
            BitBuffer bb = new BitBuffer(input);

            byte[] byteBuffer1 = new byte[16];
            byte[] byteBuffer2 = new byte[16];

            // todo: can this be larger?
            //byte[] symbol = new byte[100];

            //totalTime1 = (System.nanoTime() - startTime1);

            //while (out.size()<byteCount) {
            while (iOut<byteCount) {

                //startTime2 = System.nanoTime();

                code = bb.getBits(bitsToRead);

                //totalTime2 += (System.nanoTime() - startTime2);


                if (code==EOI_CODE || code==-1)
                    break;
                if (code==CLEAR_CODE) {
                    //startTime4 = System.nanoTime();
                    // initialize symbol jTableSpots
                    for (i = 0; i < 256; i++)
                        symbolTable[i][0] = (byte)i;
                    nextSymbol = 258;
                    bitsToRead = 9;
                    code = bb.getBits(bitsToRead);
                    if (code==EOI_CODE || code==-1)
                        break;
                    //out.add(symbolTable[code]);
                    System.arraycopy(symbolTable[code], 0, out, iOut, symbolTable[code].length);
                    iOut += symbolTable[code].length;
                    oldCode = code;
                    //totalTime4 += (System.nanoTime() - startTime4);

                } else {
                    if (code<nextSymbol) {
                        //startTime6 = System.nanoTime();
                        // code is in jTableSpots
                        //startTime5 = System.nanoTime();
                        //out.add(symbolTable[code]);
                        symbolLength = symbolTable[code].length;
                        System.arraycopy(symbolTable[code], 0, out, iOut, symbolLength);
                        iOut += symbolLength;
                        //totalTime5 += (System.nanoTime() - startTime5);
                        // add string to jTableSpots

                        //ByteVector symbol = new ByteVector(byteBuffer1);
                        //symbol.add(symbolTable[oldCode]);
                        //symbol.add(symbolTable[code][0]);
                        int lengthOld = symbolTable[oldCode].length;

                        //byte[] newSymbol = new byte[lengthOld+1];
                        symbolTable[nextSymbol] = new byte[lengthOld+1];
                        System.arraycopy(symbolTable[oldCode], 0, symbolTable[nextSymbol], 0, lengthOld);
                        symbolTable[nextSymbol][lengthOld] = symbolTable[code][0];
                        //symbolTable[nextSymbol] = newSymbol;

                        oldCode = code;
                        nextSymbol++;
                        //totalTime6 += (System.nanoTime() - startTime6);

                    } else {

                        //startTime3 = System.nanoTime();
                        // out of jTableSpots
                        ByteVector symbol = new ByteVector(byteBuffer2);
                        symbol.add(symbolTable[oldCode]);
                        symbol.add(symbolTable[oldCode][0]);
                        byte[] outString = symbol.toByteArray();
                        //out.add(outString);
                        System.arraycopy(outString, 0, out, iOut, outString.length);
                        iOut += outString.length;
                        symbolTable[nextSymbol] = outString; //**
                        oldCode = code;
                        nextSymbol++;
                        //totalTime3 += (System.nanoTime() - startTime3);

                    }
                    if (nextSymbol == 511) { bitsToRead = 10; }
                    if (nextSymbol == 1023) { bitsToRead = 11; }
                    if (nextSymbol == 2047) { bitsToRead = 12; }
                    if (nextSymbol == 4095) { bitsToRead = 13; }
                    if (nextSymbol == 8191) { bitsToRead = 14; }
                    if (nextSymbol == 16383) { logger.error("Symbol table of LZW uncompression became too large." +
                            "\nThe next symbol would have been: " + nextSymbol +
                            "\nPlease contact tischitischer@gmail.com"); return null; };
                }

            }

            totalTimeGlob = (System.nanoTime() - startTimeGlob);
        /*
        logger.info("total : "+totalTimeGlob/1000);
        totalTimeGlob = 1000;
        logger.info("fraction1 : "+(double)totalTime1/totalTimeGlob);
        logger.info("fraction2 : "+(double)totalTime2/totalTimeGlob);
        logger.info("fraction3 : "+(double)totalTime3/totalTimeGlob);
        logger.info("fraction4 : "+(double)totalTime4/totalTimeGlob);
        logger.info("fraction5 : "+(double)totalTime5/totalTimeGlob);
        logger.info("fraction6 : "+(double)totalTime6/totalTimeGlob);
        logger.info("fraction7 : "+(double)totalTime7/totalTimeGlob);
        logger.info("fraction8 : "+(double)totalTime8/totalTimeGlob);
        logger.info("fraction9 : "+(double)totalTime9/totalTimeGlob);
        logger.info("symbolLengthMax "+symbolLengthMax);
        */

            return out;
        }

        public void setShortPixelsCropXY(short[] pixels, int ys, int ny, int xs, int nx, int imByteWidth, byte[] buffer) {
            int ip = 0;
            int bs, be;
            if(fi.bytesPerPixel !=2 ) {
                 logger.error("Unsupported bit depth: " + fi.bytesPerPixel * 8);
            }

            for (int y = ys; y < ys + ny; y++) {

                bs = y * imByteWidth + xs * fi.bytesPerPixel;
                be = bs + nx * fi.bytesPerPixel;

                if (fi.intelByteOrder) {
                    if (fi.fileType == GRAY16_SIGNED)
                        for (int j = bs; j < be; j += 2)
                            pixels[ip++] = (short) ((((buffer[j + 1] & 0xff) << 8) | (buffer[j] & 0xff)) + 32768);
                    else
                        for (int j = bs; j < be; j += 2)
                            pixels[ip++] = (short) (((buffer[j + 1] & 0xff) << 8) | (buffer[j] & 0xff));
                } else {
                    if (fi.fileType == GRAY16_SIGNED)
                        for (int j = bs; j < be; j += 2)
                            pixels[ip++] = (short) ((((buffer[j] & 0xff) << 8) | (buffer[j + 1] & 0xff)) + 32768);
                    else
                        for (int j = bs; j < be; j += 2)
                            pixels[ip++] = (short) (((buffer[j] & 0xff) << 8) | (buffer[j + 1] & 0xff));
                }
            }
        }

        public void setBytePixelsCropXY(byte[] pixels, int ys, int ny, int xs, int nx, int imByteWidth, byte[] buffer) {
            int ip = 0;
            int bs, be;

            for (int y = ys; y < ys + ny; y++) {
                bs = y * imByteWidth + xs * fi.bytesPerPixel;
                be = bs + nx * fi.bytesPerPixel;
                for (int j = bs; j < be; j += 1)
                {
                    pixels[ip++] = buffer[j];
                }
            }
        }


        public void start () {
            //info("Starting " +  threadName );
            if (t == null) {
                t = new Thread (this, threadName);
                t.start();
            }
        }

        private byte[] readCroppedPlaneFromTiff(SerializableFileInfo fi, RandomAccessFile in, int ys, int ye)
        {
            boolean hasStrips = false;
            int readLength;
            long readStart;
            byte[] buffer;

            if (fi.stripOffsets != null && fi.stripOffsets.length > 1)
            {
                hasStrips = true;
            }

            if (hasStrips) {
                // convert rows to strips
                int rps = fi.rowsPerStrip;
                int ss = (int) (1.0*ys/rps);
                int se = (int) (1.0*ye/rps);
                readStart = fi.stripOffsets[ss];

                readLength = 0;
                if(se >= fi.stripLengths.length)
                {
                    logger.warning("Strip is out of bounds");
                }
                for (int s = ss; s <= se; s++) {
                    readLength += fi.stripLengths[s];
                }
            }
            else
            {  // none or one strip
                if(fi.compression == ZIP) {
                    // read all data
                    readStart = fi.offset;
                    readLength = (int)fi.stripLengths[0];
                } else {
                    // read subset
                    // convert rows to bytes
                    readStart = fi.offset + ys * fi.width * fi.bytesPerPixel;
                    readLength = ((ye-ys)+1) * fi.width * fi.bytesPerPixel; // ye is -1 sometimes why?
                }
            }

            if ( readLength <= 0 )
            {
                logger.warning("file type: Tiff");
                logger.warning("hasStrips: " + hasStrips);
                logger.warning("read from [bytes]: "+ readStart );
                logger.warning("read to [bytes]: "+ (readStart + readLength - 1) );
                logger.warning("ys: " + ys);
                logger.warning("ye: " + ye);
                logger.warning("fileInfo.compression: " + fi.compression);
                logger.warning("fileInfo.height: " + fi.height);
                logger.error("Error during file reading. See log window for more information");
                return(null);
            }

            buffer = new byte[readLength];

            try
            {
                if ( readStart + readLength - 1 <= in.length() )
                {
                    in.seek(readStart); // TODO: is this really slow??
                    in.readFully(buffer);
                }
                else
                {
                    logger.warning("The requested data exceeds the file length; no data was read.");
                    logger.warning("file type: Tiff");
                    logger.warning("hasStrips: " + hasStrips);
                    logger.warning("file length [bytes]: " + in.length());
                    logger.warning("attempt to read until [bytes]: "+ (readStart + readLength - 1) );
                    logger.warning("ys: " + ys);
                    logger.warning("ye: " + ye);
                    logger.warning("fileInfo.compression: " + fi.compression);
                    logger.warning("fileInfo.height: " + fi.height);
                }
            }
            catch (Exception e)
            {
                logger.warning(e.toString());
            }

            return buffer;

        }



        /** A growable array of bytes. */
        class ByteVector {
            private byte[] data;
            private int size;

            public ByteVector() {
                data = new byte[10];
                size = 0;
            }

            public ByteVector(int initialSize) {
                data = new byte[initialSize];
                size = 0;
            }

            public ByteVector(byte[] byteBuffer) {
                data = byteBuffer;
                size = 0;
            }

            public void add(byte x) {
                if (size>=data.length) {
                    doubleCapacity();
                    add(x);
                } else
                    data[size++] = x;
            }

            public int size() {
                return size;
            }

            public void add(byte[] array) {
                int length = array.length;
                while (data.length-size<length)
                    doubleCapacity();
                System.arraycopy(array, 0, data, size, length);
                size += length;
            }

            void doubleCapacity() {
                //IJ.info("double: "+data.length*2);
                byte[] tmp = new byte[data.length*2 + 1];
                System.arraycopy(data, 0, tmp, 0, data.length);
                data = tmp;
            }

            public void clear() {
                size = 0;
            }

            public byte[] toByteArray() {
                byte[] bytes = new byte[size];
                System.arraycopy(data, 0, bytes, 0, size);
                return bytes;
            }

        }

    }


}

/**
 *
 *
 static int assignHDF5TypeToImagePlusBitdepth( String type, boolean isRGB)
 {
 //
 // Code copied from Ronneberger
 //
 int nBits = 0;
 if (type.equals("uint8")) {
 if( isRGB ) {
 nBits = 24;
 } else {
 nBits = 8;
 }
 } else if (type.equals("uint16") || type.equals("int16")) {
 nBits = 16;
 } else if (type.equals("float32") || type.equals("float64")) {
 nBits = 32;
 } else {
 IJ.error("Type '" + type + "' Not handled yet!");
 }
 return nBits;
 }

 static ImagePlus loadDataSetsToHyperStack( String filename, String[] dsetNames,
                                               int nFrames, int nChannels )
    {
        //
        // Code copied from Ronneberger
        //

        String dsetName = "";
        try
        {
            IHDF5ReaderConfigurator conf = HDF5Factory.configureForReading(filename);
            conf.performNumericConversions();
            IHDF5Reader reader = conf.reader();
            ImagePlus image = null;
            int rank      = 0;
            int nLevels   = 0;
            int nRows     = 0;
            int nCols     = 0;
            boolean isRGB = false;
            int nBits     = 0;
            double maxGray = 1;
            String typeText = "";
            for (int frame = 0; frame < nFrames; ++frame) {
                for (int channel = 0; channel < nChannels; ++channel) {
                    // load data set
                    //
                    dsetName = dsetNames[frame*nChannels+channel];
                    IJ.showStatus( "Loading " + dsetName);
                    IJ.showProgress( frame*nChannels+channel+1, nFrames*nChannels);
                    HDF5DataSetInformation dsInfo = reader.object().getDataSetInformation(dsetName);
                    float[] element_size_um = {1,1,1};
                    try {
                        element_size_um = reader.float32().getArrayAttr(dsetName, "element_size_um");
                    }
                    catch (HDF5Exception err) {
                        IJ.log("Warning: Can't read attribute 'element_size_um' from file '" + filename
                                + "', dataset '" + dsetName + "':\n"
                                + err + "\n"
                                + "Assuming element size of 1 x 1 x 1 um^3");
                    }

                    // in first call create hyperstack
                    //
                    if (image == null) {
                        rank = dsInfo.getRank();
                        typeText = hdf5InfoToString(dsInfo);
                        if (rank == 2) {
                            nLevels = 1;
                            nRows = (int)dsInfo.getDimensions()[0];
                            nCols = (int)dsInfo.getDimensions()[1];
                        } else if (rank == 3) {
                            nLevels = (int)dsInfo.getDimensions()[0];
                            nRows   = (int)dsInfo.getDimensions()[1];
                            nCols   = (int)dsInfo.getDimensions()[2];
                            if( typeText.equals( "uint8") && nCols == 3)
                            {
                                nLevels = 1;
                                nRows = (int)dsInfo.getDimensions()[0];
                                nCols = (int)dsInfo.getDimensions()[1];
                                isRGB = true;
                            }
                        } else if (rank == 4 && typeText.equals( "uint8")) {
                            nLevels = (int)dsInfo.getDimensions()[0];
                            nRows   = (int)dsInfo.getDimensions()[1];
                            nCols   = (int)dsInfo.getDimensions()[2];
                            isRGB   = true;
                        } else {
                            IJ.error( dsetName + ": rank " + rank + " of type " + typeText + " not supported (yet)");
                            return null;
                        }

                        nBits = assignHDF5TypeToImagePlusBitdepth( typeText, isRGB);

                        image = IJ.createHyperStack( filename + ": " + dsetName,
                                nCols, nRows, nChannels, nLevels, nFrames, nBits);
                        image.getCalibration().pixelDepth  = element_size_um[0];
                        image.getCalibration().pixelHeight = element_size_um[1];
                        image.getCalibration().pixelWidth  = element_size_um[2];
                        image.getCalibration().setUnit("micrometer");
                        image.setDisplayRange(0,255);
                    }

                    // take care of data sets with more than 2^31 elements
                    //
                    long   maxLoadBlockSize = (1L<<31) - 1;
                    int[]  loadBlockDimensions = new int[dsInfo.getRank()];
                    long[] loadBlockOffset = new long[dsInfo.getRank()];
                    int    nLoadBlocks = 1;
                    long   levelsPerReadOperation = (int)dsInfo.getDimensions()[0];

                    for( int d = 0; d < dsInfo.getRank(); ++d) {
                        loadBlockDimensions[d] = (int)dsInfo.getDimensions()[d];
                        loadBlockOffset[d] = 0;
                    }

                    if( dsInfo.getNumberOfElements() >= maxLoadBlockSize) {
                        long minBlockSize = 1;
                        for( int d = 1; d < dsInfo.getRank(); ++d) {
                            minBlockSize *= loadBlockDimensions[d];
                        }
                        levelsPerReadOperation = maxLoadBlockSize / minBlockSize;
                        loadBlockDimensions[0] = (int)levelsPerReadOperation;
                        nLoadBlocks = (int)((dsInfo.getDimensions()[0] - 1) / levelsPerReadOperation + 1); // integer version for ceil(a/b)
                        IJ.log("Data set has " + dsInfo.getNumberOfElements() + " elements (more than 2^31). Reading in " + nLoadBlocks + " blocks with maximum of " + levelsPerReadOperation + " levels");
                    }

                    // load data and copy slices to hyperstack
                    //
                    int sliceSize = nCols * nRows;
                    for( int block = 0; block < nLoadBlocks; ++block) {
                        // compute offset and size of next block, that is loaded
                        //
                        loadBlockOffset[0] = (long)block * levelsPerReadOperation;
                        int remainingLevels = (int)(dsInfo.getDimensions()[0] - loadBlockOffset[0]);
                        if( remainingLevels < loadBlockDimensions[0] ) {
                            // last block is smaller
                            loadBlockDimensions[0] = remainingLevels;
                        }
                        // compute target start level in image processor
                        int trgLevel = (int)loadBlockOffset[0];


                        if (typeText.equals( "uint8") && isRGB == false) {
                            MDByteArray rawdata = reader.uint8().readMDArrayBlockWithOffset(dsetName, loadBlockDimensions, loadBlockOffset);
                            for( int lev = 0; lev < loadBlockDimensions[0]; ++lev) {
                                ImageProcessor ip = image.getStack().getProcessor( image.getStackIndex(
                                        channel+1, trgLevel+lev+1, frame+1));
                                System.arraycopy( rawdata.getAsFlatArray(), lev*sliceSize,
                                        (byte[])ip.getPixels(),   0,
                                        sliceSize);
                            }
                            maxGray = 255;

                        }  else if (typeText.equals( "uint8") && isRGB) {  // RGB data
                            MDByteArray rawdata = reader.uint8().readMDArrayBlockWithOffset(dsetName, loadBlockDimensions, loadBlockOffset);
                            byte[] srcArray = rawdata.getAsFlatArray();


                            for( int lev = 0; lev < loadBlockDimensions[0]; ++lev) {
                                ImageProcessor ip = image.getStack().getProcessor( image.getStackIndex(
                                        channel+1, trgLevel+lev+1, frame+1));
                                int[] trgArray = (int[])ip.getPixels();
                                int srcOffset = lev*sliceSize*3;

                                for( int rc = 0; rc < sliceSize; ++rc)
                                {
                                    int red   = srcArray[srcOffset + rc*3] & 0xff;
                                    int green = srcArray[srcOffset + rc*3 + 1] & 0xff;
                                    int blue  = srcArray[srcOffset + rc*3 + 2] & 0xff;
                                    trgArray[rc] = (red<<16) + (green<<8) + blue;
                                }

                            }
                            maxGray = 255;

                        } else if (typeText.equals( "uint16")) {
                            MDShortArray rawdata = reader.uint16().readMDArrayBlockWithOffset(dsetName, loadBlockDimensions, loadBlockOffset);
                            for( int lev = 0; lev < loadBlockDimensions[0]; ++lev) {
                                ImageProcessor ip = image.getStack().getProcessor( image.getStackIndex(
                                        channel+1, trgLevel+lev+1, frame+1));
                                System.arraycopy( rawdata.getAsFlatArray(), lev*sliceSize,
                                        (short[])ip.getPixels(),   0,
                                        sliceSize);
                            }
                            short[] data = rawdata.getAsFlatArray();
                            for (int i = 0; i < data.length; ++i) {
                                if (data[i] > maxGray) maxGray = data[i];
                            }
                        } else if (typeText.equals( "int16")) {
                            MDShortArray rawdata = reader.int16().readMDArrayBlockWithOffset(dsetName, loadBlockDimensions, loadBlockOffset);
                            for( int lev = 0; lev < loadBlockDimensions[0]; ++lev) {
                                ImageProcessor ip = image.getStack().getProcessor( image.getStackIndex(
                                        channel+1, trgLevel+lev+1, frame+1));
                                System.arraycopy( rawdata.getAsFlatArray(), lev*sliceSize,
                                        (short[])ip.getPixels(),   0,
                                        sliceSize);
                            }
                            short[] data = rawdata.getAsFlatArray();
                            for (int i = 0; i < data.length; ++i) {
                                if (data[i] > maxGray) maxGray = data[i];
                            }
                        } else if (typeText.equals( "float32") || typeText.equals( "float64") ) {
                            MDFloatArray rawdata = reader.float32().readMDArrayBlockWithOffset(dsetName, loadBlockDimensions, loadBlockOffset);
                            for( int lev = 0; lev < loadBlockDimensions[0]; ++lev) {
                                ImageProcessor ip = image.getStack().getProcessor( image.getStackIndex(
                                        channel+1, trgLevel+lev+1, frame+1));
                                System.arraycopy( rawdata.getAsFlatArray(), lev*sliceSize,
                                        (float[])ip.getPixels(),   0,
                                        sliceSize);
                            }
                            float[] data = rawdata.getAsFlatArray();
                            for (int i = 0; i < data.length; ++i) {
                                if (data[i] > maxGray) maxGray = data[i];
                            }
                        }
                    }  // end for block
                }  // end for channel
            } // end for frame
            reader.close();

            // aqdjust max gray
            for( int channel = 1; channel <= nChannels; ++channel)
            {
                image.setC(channel);
                image.setDisplayRange(0,maxGray);
            }

            image.setC(1);
            image.show();
            return image;
        }

        catch (HDF5Exception err)
        {
            IJ.error("Error while opening '" + filename
                    + "', dataset '" + dsetName + "':\n"
                    + err);
        }
        catch (Exception err)
        {
            IJ.error("Error while opening '" + filename
                    + "', dataset '" + dsetName + "':\n"
                    + err);
        }
        catch (OutOfMemoryError o)
        {
            IJ.outOfMemory("Load HDF5");
        }
        return null;

    }
*/