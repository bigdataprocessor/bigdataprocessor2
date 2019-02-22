/*
 * #%L
 * Data streaming, tracking and cropping tools
 * %%
 * Copyright (C) 2017 Christian Tischer
 *
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


package de.embl.cba.bdp2;


import de.embl.cba.bdp2.fileinfosource.SerializableFileInfo;
import de.embl.cba.bdp2.logging.IJLazySwingLogger;
import de.embl.cba.bdp2.logging.Logger;
import ij.IJ;
import ij.io.FileInfo;
import ij.io.RandomAccessStream;
import ij.util.Tools;

import java.io.*;
import java.util.ArrayList;
import java.util.Properties;

/**
 Decodes single and multi-image TIFF files. The LZW decompression
 code was contributed by Curtis Rueden.
 */

public class FastTiffDecoder {

    Logger logger = new IJLazySwingLogger();

    private boolean readingStrips;
    // tags
    public static final int NEW_SUBFILE_TYPE = 254;
    public static final int IMAGE_WIDTH = 256;
    public static final int IMAGE_LENGTH = 257;
    public static final int BITS_PER_SAMPLE = 258;
    public static final int COMPRESSION = 259;
    public static final int PHOTO_INTERP = 262;
    public static final int IMAGE_DESCRIPTION = 270;
    public static final int STRIP_OFFSETS = 273;
    public static final int ORIENTATION = 274;
    public static final int SAMPLES_PER_PIXEL = 277;
    public static final int ROWS_PER_STRIP = 278;
    public static final int STRIP_BYTE_COUNT = 279;
    public static final int X_RESOLUTION = 282;
    public static final int Y_RESOLUTION = 283;
    public static final int PLANAR_CONFIGURATION = 284;
    public static final int RESOLUTION_UNIT = 296;
    public static final int SOFTWARE = 305;
    public static final int DATE_TIME = 306;
    public static final int ARTEST = 315;
    public static final int HOST_COMPUTER = 316;
    public static final int PREDICTOR = 317;
    public static final int COLOR_MAP = 320;
    public static final int TILE_WIDTH = 322;
    public static final int SAMPLE_FORMAT = 339;
    public static final int JPEG_TABLES = 347;
    public static final int METAMORPH1 = 33628;
    public static final int METAMORPH2 = 33629;
    public static final int IPLAB = 34122;
    public static final int NIH_IMAGE_HDR = 43314; // IMAGEJ HEADER
    public static final int META_DATA_BYTE_COUNTS = 50838; // private tag registered with Adobe
    public static final int META_DATA = 50839; // private tag registered with Adobe

    //constants
    static final int UNSIGNED = 1;
    static final int SIGNED = 2;
    static final int FLOATING_POINT = 3;

    //field types
    static final int SHORT = 3;
    static final int LONG = 4;
    static final int RATIONALE = 5;
    static final int LONG8 = 16;

    // metadata types
    static final int MAGIC_NUMBER = 0x494a494a;  // "IJIJ"
    static final int INFO = 0x696e666f;  // "info" (Info image property)
    static final int LABELS = 0x6c61626c;  // "labl" (slice labels)
    static final int RANGES = 0x72616e67;  // "rang" (display ranges)
    static final int LUTS = 0x6c757473;  // "luts" (channel LUTs)
    static final int ROI = 0x726f6920;  // "roi " (ROI)
    static final int OVERLAY = 0x6f766572;  // "over" (overlay)

    private String directory;
    private String name;
    private String url;
    protected RandomAccessStream in;
    protected boolean debugMode;
    private boolean littleEndian;
    private String dInfo;
    private int ifdCount;
    private int[] metaDataCounts;
    private String tiffMetadata;
    private int photoInterp;

    private boolean isBigTiff = false;

    private int check = 0;

    //
    long startTimeTotal = 0;
    long startTimeStrips = 0;
    long stripTime = 0;
    long totalTime = 0;


    public FastTiffDecoder( String directory, String name ) {
        this.directory = directory;
        this.name = name;
    }


    // FileReader

    // IFD parser

    // class IFD:
    // input bytearrray
    // getMethods for getting the info

    /*public FastTiffDecoder(InputStream in, String name) {
        directory = "";
        this.name = name;
        url = "";
        this.in = new RandomAccessStream(in);
    }*/

    final int getInt() throws IOException {
        int b1 = in.read();
        int b2 = in.read();
        int b3 = in.read();
        int b4 = in.read();
        if (littleEndian)
            return ((b4 << 24) | (b3 << 16) | (b2 << 8) | (b1 << 0));
        else
            return ((b1 << 24) | (b2 << 16) | (b3 << 8) | b4);
    }

    final void convertToInt(long[] ints, byte[] bytes) {
        if (littleEndian) {
            for (int i = 0, j = 0; i < bytes.length; i += 4, j++) {
                ints[j] = (((bytes[i+3]&0xff) << 24) |
                           ((bytes[i+2]&0xff) << 16) |
                           ((bytes[i+1]&0xff) << 8) |
                           ((bytes[i+0]&0xff) << 0));
            }
        } else {
            for (int i = 0, j = 0; i < bytes.length; i += 4, j++) {
                ints[j] = (((bytes[i]&0xff) << 24) | ((bytes[i+1]&0xff) << 16) | ((bytes[i+2]&0xff) << 8) | (bytes[i+3]&0xff));
            }
        }
    }

    final void convertToLong(long[] longs, byte[] bytes) {
        // this is the weird Tiff Long with only 4 bytes
        if (littleEndian) {
            for (int i = 0, j = 0; i < bytes.length; i += 4, j++) {
                longs[j] = (((bytes[i+3]&0xFFL) << 24) + ((bytes[i+2]&0xFFL) << 16) + ((bytes[i+1]&0xFFL) << 8) + ((bytes[i]&0xFFL) << 0));
            }
        } else {
            for (int i = 0, j = 0; i < bytes.length; i += 4, j++) {
                longs[j] = (((bytes[i]&0xFFL) << 24) + ((bytes[i+1]&0xFFL) << 16) + ((bytes[i+2]&0xFFL) << 8) + (bytes[i+3]&0xFFL));
            }
        }
    }

    final void convertToLong8(long[] longs, byte[] bytes) {
        if (littleEndian) {
            for (int i = 0, j = 0; i < bytes.length; i += 8, j++) {
                longs[j] = (((bytes[i+7]&0xFFL) << 56) |
                            ((bytes[i+6]&0xFFL) << 48) |
                            ((bytes[i+5]&0xFFL) << 40) |
                            ((bytes[i+4]&0xFFL) << 32) |
                            ((bytes[i+3]&0xFFL) << 24) |
                            ((bytes[i+2]&0xFFL) << 16) |
                            ((bytes[i+1]&0xFFL) <<  8) |
                            ((bytes[i+0]&0xFFL) <<  0));
            }
        } else {
            for (int i = 0, j = 0; i < bytes.length; i += 8, j++) {
                longs[j] = (((bytes[i+0]&0xFFL) << 56) |
                            ((bytes[i+1]&0xFFL) << 48) |
                            ((bytes[i+2]&0xFFL) << 40) |
                            ((bytes[i+3]&0xFFL) << 32) |
                            ((bytes[i+4]&0xFFL) << 24) |
                            ((bytes[i+5]&0xFFL) << 16) |
                            ((bytes[i+6]&0xFFL) << 8) |
                             (bytes[i+7]&0xFFL) );
            }
        }
    }

    final void convertToShort(long[] ints, byte[] bytes) {
        if (littleEndian) {
            for (int i = 0, j = 0; i < bytes.length; i += 2, j++) {
                ints[j] = (((bytes[i+1]&0xff) << 8) | ((bytes[i]&0xff) << 0));
            }
        } else {
            for (int i = 0, j = 0; i < bytes.length; i += 2, j++) {
                ints[j] = (((bytes[i+2]&0xff) << 8) | (bytes[i+3]&0xff));
            }

        }
    }

    final long getUnsignedIntAsLong() throws IOException {
        return (long)getInt()&0xffffffffL;
    }

    final int getShort() throws IOException {
        int b1 = in.read();
        int b2 = in.read();
        if (littleEndian)
            return ((b2<<8) | b1);
        else
            return ((b1<<8) | b2);
    }

    final long getLong() throws IOException {
        if (littleEndian)
        {
            return ((long)getInt()&0xffffffffL) + ((long)getInt()<<32);
        }
        else
        {
            return ((long) getInt() << 32) + ((long) getInt() & 0xffffffffL);
        }
        //return in.read()+(in.read()<<8)+(in.read()<<16)+(in.read()<<24)+(in.read()<<32)+(in.read()<<40)+(in.read()<<48)+(in.read()<<56);
    }

    final double readDouble() throws IOException {
        return Double.longBitsToDouble(getLong());
    }

    long OpenImageFileHeader() throws IOException {
        // Open 8-byte Image File Header at start of file.
        // Returns the offset in bytes to the first IFD or -1
        // if this is not a valid tiff file.
        // BigTiff: http://www.awaresystems.be/imaging/tiff/bigtiff.html

        int byteOrder = in.readShort();
        if (byteOrder==0x4949) // "II"
            littleEndian = true;
        else if (byteOrder==0x4d4d) // "MM"
            littleEndian = false;
        else {
            in.close();
            return -1;
        }
        int magicNumber = getShort(); // 42 or Tiff; 43 for BigTiff
        if (magicNumber == 42 )
        {
            isBigTiff = false;
        }
        else if (magicNumber == 43 )
        {
            isBigTiff = true;
        }
        else
        {
            IJ.showMessage("Unsupported Tiff Format; magic number = "+magicNumber);
        }
        long offset = 0L;
        if (isBigTiff)
        {
            int shouldBe8 = getShort();
            int shouldBe0 = getShort();
            offset = getLong();  // reads an 8-byte Int
        }
        else
        {
            offset = ((long) getInt()) & 0xffffffffL;
        }
        return offset;
    }

    long getValue( int fieldType, long count ) throws IOException {
        long value = 0;

        if ( count == 1 )
        {
            if ( fieldType == SHORT  )
            {
                value = getShort(); // 2
                int skip = getShort(); // 2
                if (isBigTiff) skip = getInt(); // 4
            }
            else if ( fieldType == LONG )
            {
                value = isBigTiff ? getLong() : getInt(); // 8 : 4
            }
            else if ( fieldType == RATIONALE )
            {
                value = getInt();
            }
            else
            {
                int a = 1;
            }
        }
        else  // if count > 1 do not return the actual value but just a pointer to the values
        {
            value = isBigTiff ? getLong() : getInt() & 0xffffffffL;
        }

        return value;
    }

    void getColorMap(long offset, SerializableFileInfo fi) throws IOException {
        byte[] colorTable16 = new byte[768*2];
        long saveLoc = in.getLongFilePointer();
        in.seek(offset);
        in.readFully(colorTable16);
        in.seek(saveLoc);
        fi.lutSize = 256;
        fi.reds = new byte[256];
        fi.greens = new byte[256];
        fi.blues = new byte[256];
        int j = 0;
        if (littleEndian) j++;
        int sum = 0;
        for (int i=0; i<256; i++) {
            fi.reds[i] = colorTable16[j];
            sum += fi.reds[i];
            fi.greens[i] = colorTable16[512+j];
            sum += fi.greens[i];
            fi.blues[i] = colorTable16[1024+j];
            sum += fi.blues[i];
            j += 2;
        }
        if (sum!=0 && fi.fileType== FileInfo.GRAY8)
            fi.fileType = FileInfo.COLOR8;
    }

    byte[] getString(int count, long offset) throws IOException {
        count--; // skip null byte at end of string
        if (count<=3)
            return null;
        byte[] bytes = new byte[count];
        long saveLoc = in.getLongFilePointer();
        in.seek(offset);
        in.readFully(bytes);
        in.seek(saveLoc);
        return bytes;
    }

    /** Save the image description in the specified FileInfo. ImageJ
     saves spatial and density calibration data in this string. For
     stacks, it also saves the number of images to avoid having to
     decode an IFD for each image. */
    public void saveImageDescription(byte[] description, SerializableFileInfo fi) {
        String id = new String(description);
        if (!id.startsWith("ImageJ"))
            saveMetadata(getName(IMAGE_DESCRIPTION), id);
        if (id.length()<7) return;
        fi.description = id;
        int index1 = id.indexOf("images=");
        if (index1>0) {
            int index2 = id.indexOf("\n", index1);
            if (index2>0) {
                String images = id.substring(index1+7,index2);
                int n = (int) Tools.parseDouble(images, 0.0);
                if (n>1) fi.nImages = n;
            }
        }
    }

    public void saveMetadata(String name, String data) {
        if (data==null) return;
        String str = name+": "+data+"\n";
        if (tiffMetadata==null)
            tiffMetadata = str;
        else
            tiffMetadata += str;
    }

    void decodeNIHImageHeader(int offset, SerializableFileInfo fi) throws IOException {
        long saveLoc = in.getLongFilePointer();

        in.seek(offset+12);
        int version = in.readShort();

        in.seek(offset+160);
        double scale = in.readDouble();
        if (version>106 && scale!=0.0) {
            fi.pixelWidth = 1.0/scale;
            fi.pixelHeight = fi.pixelWidth;
        }

        // spatial calibration
        in.seek(offset+172);
        int units = in.readShort();
        if (version<=153) units += 5;
        switch (units) {
            case 5: fi.unit = "nanometer"; break;
            case 6: fi.unit = "micrometer"; break;
            case 7: fi.unit = "mm"; break;
            case 8: fi.unit = "cm"; break;
            case 9: fi.unit = "meter"; break;
            case 10: fi.unit = "km"; break;
            case 11: fi.unit = "inch"; break;
            case 12: fi.unit = "ft"; break;
            case 13: fi.unit = "mi"; break;
        }


        // density calibration

        in.seek(offset+182);
        int fitType = in.read();
        int unused = in.read();
        int nCoefficients = in.readShort();
        if (fitType==11) {
            fi.calibrationFunction = 21; //Calibration.UNCALIBRATED_OD
            fi.valueUnit = "U. OD";
        } else if (fitType>=0 && fitType<=8 && nCoefficients>=1 && nCoefficients<=5) {
            switch (fitType) {
                case 0: fi.calibrationFunction = 0; break; //Calibration.STRAIGHT_LINE
                case 1: fi.calibrationFunction = 1; break; //Calibration.POLY2
                case 2: fi.calibrationFunction = 2; break; //Calibration.POLY3
                case 3: fi.calibrationFunction = 3; break; //Calibration.POLY4
                case 5: fi.calibrationFunction = 4; break; //Calibration.EXPONENTIAL
                case 6: fi.calibrationFunction = 5; break; //Calibration.POWER
                case 7: fi.calibrationFunction = 6; break; //Calibration.LOG
                case 8: fi.calibrationFunction = 10; break; //Calibration.RODBARD2 (NIH Image)
            }
            fi.coefficients = new double[nCoefficients];
            for (int i=0; i<nCoefficients; i++) {
                fi.coefficients[i] = in.readDouble();
            }
            in.seek(offset+234);
            int size = in.read();
            StringBuffer sb = new StringBuffer();
            if (size>=1 && size<=16) {
                for (int i=0; i<size; i++)
                    sb.append((char)(in.read()));
                fi.valueUnit = new String(sb);
            } else
                fi.valueUnit = " ";
        }

        in.seek(offset+260);
        int nImages = in.readShort();
        if(nImages>=2 && (fi.fileType== FileInfo.GRAY8||fi.fileType== FileInfo.COLOR8)) {
            fi.nImages = nImages;
            fi.pixelDepth = in.readFloat(); //SliceSpacing
            int skip = in.readShort();      //CurrentSlice
            fi.frameInterval = in.readFloat();
            //ij.IJ.writeHeaderFile("fi.pixelDepth: "+fi.pixelDepth);
        }

        in.seek(offset+272);
        float aspectRatio = in.readFloat();
        if (version>140 && aspectRatio!=0.0)
            fi.pixelHeight = fi.pixelWidth/aspectRatio;

        in.seek(saveLoc);
    }

    void dumpTag(int tag, int count, int value, SerializableFileInfo fi) {
        long lvalue = ((long)value)&0xffffffffL;
        String name = getName(tag);
        String cs = (count==1)?"":", count=" + count;
        dInfo += "    " + tag + ", \"" + name + "\", value=" + lvalue + cs + "\n";
        //ij.IJ.info(tag + ", \"" + name + "\", value=" + value + cs + "\n");
    }

    String getName(int tag) {
        String name;
        switch (tag) {
            case NEW_SUBFILE_TYPE: name="NewSubfileType"; break;
            case IMAGE_WIDTH: name="ImageWidth"; break;
            case IMAGE_LENGTH: name="ImageLength"; break;
            case STRIP_OFFSETS: name="StripOffsets"; break;
            case ORIENTATION: name="Orientation"; break;
            case PHOTO_INTERP: name="PhotoInterp"; break;
            case IMAGE_DESCRIPTION: name="ImageDescription"; break;
            case BITS_PER_SAMPLE: name="BitsPerSample"; break;
            case SAMPLES_PER_PIXEL: name="SamplesPerPixel"; break;
            case ROWS_PER_STRIP: name="RowsPerStrip"; break;
            case STRIP_BYTE_COUNT: name="StripByteCount"; break;
            case X_RESOLUTION: name="XResolution"; break;
            case Y_RESOLUTION: name="YResolution"; break;
            case RESOLUTION_UNIT: name="ResolutionUnit"; break;
            case SOFTWARE: name="Software"; break;
            case DATE_TIME: name="DateTime"; break;
            case ARTEST: name="Artest"; break;
            case HOST_COMPUTER: name="HostComputer"; break;
            case PLANAR_CONFIGURATION: name="PlanarConfiguration"; break;
            case COMPRESSION: name="Compression"; break;
            case PREDICTOR: name="Predictor"; break;
            case COLOR_MAP: name="ColorMap"; break;
            case SAMPLE_FORMAT: name="SampleFormat"; break;
            case JPEG_TABLES: name="JPEGTables"; break;
            case NIH_IMAGE_HDR: name="NIHImageHeader"; break;
            case META_DATA_BYTE_COUNTS: name="MetaDataByteCounts"; break;
            case META_DATA: name="MetaData"; break;
            default: name="???"; break;
        }
        return name;
    }

    double getRational(long loc) throws IOException {
        long saveLoc = in.getLongFilePointer();
        in.seek(loc);
        double numerator = getUnsignedIntAsLong();
        double denominator = getUnsignedIntAsLong();
        in.seek(saveLoc);
        if (denominator!=0.0)
            return numerator/denominator;
        else
            return 0.0;
    }

    void getMetaData(int loc, SerializableFileInfo fi) throws IOException {
        if (metaDataCounts==null || metaDataCounts.length==0)
            return;
        int maxTypes = 10;
        long saveLoc = in.getLongFilePointer();
        in.seek(loc);
        int n = metaDataCounts.length;
        int hdrSize = metaDataCounts[0];
        if (hdrSize<12 || hdrSize>804)
        {in.seek(saveLoc); return;}
        int magicNumber = getInt();
        if (magicNumber!=MAGIC_NUMBER)  // "IJIJ"
        {in.seek(saveLoc); return;}
        int nTypes = (hdrSize-4)/8;
        int[] types = new int[nTypes];
        int[] counts = new int[nTypes];

        if (debugMode) dInfo += "Metadata:\n";
        int extraMetaDataEntries = 0;
        for (int i=0; i<nTypes; i++) {
            types[i] = getInt();
            counts[i] = getInt();
            if (types[i]<0xffffff)
                extraMetaDataEntries += counts[i];
            if (debugMode) {
                String id = "";
                if (types[i]==INFO) id = " (Info property)";
                if (types[i]==LABELS) id = " (slice labels)";
                if (types[i]==RANGES) id = " (display ranges)";
                if (types[i]==LUTS) id = " (luts)";
                if (types[i]==ROI) id = " (roi)";
                if (types[i]==OVERLAY) id = " (overlay)";
                dInfo += "   "+i+" "+Integer.toHexString(types[i])+" "+counts[i]+id+"\n";
            }
        }
        fi.metaDataTypes = new int[extraMetaDataEntries];
        fi.metaData = new byte[extraMetaDataEntries][];
        int start = 1;
        int eMDindex = 0;
        for (int i=0; i<nTypes; i++) {
            if (types[i]==INFO)
                getInfoProperty(start, fi);
            else if (types[i]==LABELS)
                getSliceLabels(start, start+counts[i]-1, fi);
            else if (types[i]==RANGES)
                getDisplayRanges(start, fi);
            else if (types[i]==LUTS)
                getLuts(start, start+counts[i]-1, fi);
            else if (types[i]==ROI)
                getRoi(start, fi);
            else if (types[i]==OVERLAY)
                getOverlay(start, start+counts[i]-1, fi);
            else if (types[i]<0xffffff) {
                for (int j=start; j<start+counts[i]; j++) {
                    int len = metaDataCounts[j];
                    fi.metaData[eMDindex] = new byte[len];
                    in.readFully(fi.metaData[eMDindex], len);
                    fi.metaDataTypes[eMDindex] = types[i];
                    eMDindex++;
                }
            } else
                skipUnknownType(start, start+counts[i]-1);
            start += counts[i];
        }
        in.seek(saveLoc);
    }

    void getInfoProperty(int first, SerializableFileInfo fi) throws IOException {
        int len = metaDataCounts[first];
        byte[] buffer = new byte[len];
        in.readFully(buffer, len);
        len /= 2;
        char[] chars = new char[len];
        if (littleEndian) {
            for (int j=0, k=0; j<len; j++)
                chars[j] = (char)(buffer[k++]&255 + ((buffer[k++]&255)<<8));
        } else {
            for (int j=0, k=0; j<len; j++)
                chars[j] = (char)(((buffer[k++]&255)<<8) + buffer[k++]&255);
        }
        fi.info = new String(chars);
    }

    void getSliceLabels(int first, int last, SerializableFileInfo fi) throws IOException {
        fi.sliceLabels = new String[last-first+1];
        int index = 0;
        byte[] buffer = new byte[metaDataCounts[first]];
        for (int i=first; i<=last; i++) {
            int len = metaDataCounts[i];
            if (len>0) {
                if (len>buffer.length)
                    buffer = new byte[len];
                in.readFully(buffer, len);
                len /= 2;
                char[] chars = new char[len];
                if (littleEndian) {
                    for (int j=0, k=0; j<len; j++)
                        chars[j] = (char)(buffer[k++]&255 + ((buffer[k++]&255)<<8));
                } else {
                    for (int j=0, k=0; j<len; j++)
                        chars[j] = (char)(((buffer[k++]&255)<<8) + buffer[k++]&255);
                }
                fi.sliceLabels[index++] = new String(chars);
                //ij.IJ.info(i+"  "+fi.sliceLabels[i-1]+"  "+len);
            } else
                fi.sliceLabels[index++] = null;
        }
    }

    void getDisplayRanges(int first, SerializableFileInfo fi) throws IOException {
        int n = metaDataCounts[first]/8;
        fi.displayRanges = new double[n];
        for (int i=0; i<n; i++)
            fi.displayRanges[i] = readDouble();
    }

    void getLuts(int first, int last, SerializableFileInfo fi) throws IOException {
        fi.channelLuts = new byte[last-first+1][];
        int index = 0;
        for (int i=first; i<=last; i++) {
            int len = metaDataCounts[i];
            fi.channelLuts[index] = new byte[len];
            in.readFully(fi.channelLuts[index], len);
            index++;
        }
    }

    void getRoi(int first, SerializableFileInfo fi) throws IOException {
        int len = metaDataCounts[first];
        fi.roi = new byte[len];
        in.readFully(fi.roi, len);
    }

    void getOverlay(int first, int last, SerializableFileInfo fi) throws IOException {
        fi.overlay = new byte[last-first+1][];
        int index = 0;
        for (int i=first; i<=last; i++) {
            int len = metaDataCounts[i];
            fi.overlay[index] = new byte[len];
            in.readFully(fi.overlay[index], len);
            index++;
        }
    }

    void error(String message) throws IOException {
        if (in!=null) in.close();
        throw new IOException(message);
    }

    void skipUnknownType(int first, int last) throws IOException {
        byte[] buffer = new byte[metaDataCounts[first]];
        for (int i=first; i<=last; i++) {
            int len = metaDataCounts[i];
            if (len>buffer.length)
                buffer = new byte[len];
            in.readFully(buffer, len);
        }
    }

    public void enableDebugging() {
        debugMode = true;
    }

    SerializableFileInfo fullyReadIFD( long[] relativeStripInfoLocations ) throws IOException {

        long ifdLoc = in.getFilePointer();

        // Get Image File Directory data
        int tag, fieldType;
        long value, count, nEntries;
        nEntries = isBigTiff ? getLong() : getShort();

        if (nEntries<1 || nEntries>1000)
        {
            return null;
        }

        ifdCount++;

        if ((ifdCount%50)==0 && ifdCount>0)
            ij.IJ.showStatus("Opening IFDs: "+ifdCount);

        SerializableFileInfo fi = new SerializableFileInfo();
        fi.fileType = FileInfo.BITMAP;  //BitsPerSample defaults to 1

        for (int i=0; i<nEntries; i++) {

            tag = getShort();

            if ( tag == STRIP_OFFSETS )
            {
                relativeStripInfoLocations[0] = in.getFilePointer() - ifdLoc - 2; // store relative position of the StripOffsets Infos
            }
            if ( tag == STRIP_BYTE_COUNT )
            {
                relativeStripInfoLocations[1] = in.getFilePointer() - ifdLoc - 2; // store relative position of the StripOffsets Infos
            }

            fieldType = getShort();
            count = isBigTiff ? getLong() : getInt();
            value = getValue( fieldType, count ) & 0xffffffffL;

            if (debugMode && ifdCount<10) dumpTag(tag, (int)count, (int)value, fi);
            //ij.IJ.writeHeaderFile(i+"/"+nEntries+" "+tag + ", count=" + count + ", value=" + value);
            //if (tag==0) return null;
            switch (tag) {
                case IMAGE_WIDTH:
                    fi.width = (int)value;
                    fi.intelByteOrder = littleEndian;
                    break;
                case IMAGE_LENGTH:
                    fi.height = (int)value;
                    break;
                case STRIP_OFFSETS:
                    startTimeStrips = System.nanoTime();
                    int byteCount = isBigTiff ? 8 : 4;
                    // either is the address of the stripOffset array (count > 1)
                    // or the location of the image data (count == 1)
                    if ( count==1 )
                        fi.stripOffsets = new long[] {(int)value};
                    else {
                        long saveLoc = in.getLongFilePointer();  // where the IFD currently is
                        in.seek(value);
                        fi.stripOffsets = new long[(int)count];
                        byte[] buffer = new byte[(int)count*byteCount];
                        //int pos = in.getFilePointer();
                        //readingStrips = true;
                        in.readFully(buffer);
                        //for (int channel=0; channel<count; channel++)
                        //    fi.stripOffsets[channel] = getInt();
                        if ( isBigTiff )
                        {
                            convertToLong8(fi.stripOffsets, buffer);
                        }
                        else
                        {
                            convertToInt(fi.stripOffsets, buffer);
                        }
                        //readingStrips = false;
                        //if(ifdCount == 1) logger.info("Strip offset 10:" + fi.stripOffsets[10]); //76728  //76427
                        //info(""+(in.getFilePointer()-pos));
                        //info(""+(count*4));
                        in.seek(saveLoc); // go back to IFD
                    }

                    fi.offset = count>0?fi.stripOffsets[0]:(int)value;
                    if (count>1 && (((long)fi.stripOffsets[(int)count-1])&0xffffffffL)<(((long)fi.stripOffsets[0])&0xffffffffL))
                        fi.offset = fi.stripOffsets[(int)count-1];
                    //info("fi.offset "+fi.offset);
                    stripTime += (System.nanoTime() - startTimeStrips);

                    break;

                case STRIP_BYTE_COUNT:
                    startTimeStrips = System.nanoTime();
                    if (count==1)
                    {
                        fi.stripLengths = new long[]{(int) value};
                    }
                    else
                    {
                        long saveLoc = in.getLongFilePointer();
                        in.seek(value);
                        fi.stripLengths = new long[(int)count];
                        if (fieldType==SHORT) {
                            byte[] buffer = new byte[(int)count*2];
                            in.readFully(buffer);
                            convertToShort(fi.stripLengths, buffer);
                        } else if (fieldType==LONG) {
                            byte[] buffer = new byte[(int)count*4];
                            in.readFully(buffer);
                            convertToInt(fi.stripLengths, buffer);
                        } else if (fieldType==LONG8) {
                            byte[] buffer = new byte[(int)count*8];
                            in.readFully(buffer);
                            convertToLong8(fi.stripLengths, buffer);
                        }
                        /*for (int channel=0; channel<count; channel++) {
                            if (fieldType==SHORT)
                                fi.stripLengths[channel] = getShort();
                            else
                                fi.stripLengths[channel] = getInt();
                        }*/

                        in.seek(saveLoc); // go back to IFD
                    }
                    stripTime += (System.nanoTime() - startTimeStrips);

                    break;
                case PHOTO_INTERP:
                    photoInterp = (int)value;
                    fi.whiteIsZero = (int)value==0;
                    break;
                case BITS_PER_SAMPLE:
                    if (count==1) {
                        if (value==8)
                        {
                            fi.fileType = FileInfo.GRAY8;
                            fi.bytesPerPixel = 1;
                        }
                        else if (value==16)
                        {
                            fi.fileType = FileInfo.GRAY16_UNSIGNED;
                            fi.bytesPerPixel = 2;
                        }
                        else if (value==32)
                        {
                            logger.error("Unsupported FileType: " + value);
                            fi.fileType = FileInfo.GRAY32_INT;
                        }
                        else if (value==12)
                        {
                            fi.fileType = FileInfo.GRAY12_UNSIGNED;
                            logger.error("Unsupported FileType: " + value);
                        }
                        else if (value==1)
                        {
                            logger.error("Unsupported FileType: " + value);
                            fi.fileType = FileInfo.BITMAP;
                        }
                        else
                            logger.error("Unsupported FileType: " + value);

                    }
                    else if (count>1)
                    {
                        logger.error("Unsupported FileType: " + value);
                        long saveLoc = in.getLongFilePointer();
                        in.seek(value);
                        int bitDepth = getShort();
                        if (bitDepth==8)
                            fi.fileType = FileInfo.GRAY8;
                        else if (bitDepth==16)
                            fi.fileType = FileInfo.GRAY16_UNSIGNED;
                        else
                            error("ImageJ can only open 8 and 16 bit/channel images ("+bitDepth+")");
                        in.seek(saveLoc);
                    }
                    break;
                case SAMPLES_PER_PIXEL:
                    fi.samplesPerPixel = (int)value;
                    if (value==3 && fi.fileType== FileInfo.GRAY8)
                        fi.fileType = FileInfo.RGB;
                    else if (value==3 && fi.fileType== FileInfo.GRAY16_UNSIGNED)
                        fi.fileType = FileInfo.RGB48;
                    else if (value==4 && fi.fileType== FileInfo.GRAY8)
                        fi.fileType = photoInterp==5? FileInfo.CMYK: FileInfo.ARGB;
                    else if (value==4 && fi.fileType== FileInfo.GRAY16_UNSIGNED) {
                        fi.fileType = FileInfo.RGB48;
                        if (photoInterp==5)  //assume cmyk
                            fi.whiteIsZero = true;
                    }
                    break;
                case ROWS_PER_STRIP:
                    fi.rowsPerStrip = (int)value;
                    break;
                case X_RESOLUTION:
                    double xScale = getRational(value);
                    if (xScale!=0.0) fi.pixelWidth = 1.0/xScale;
                    break;
                case Y_RESOLUTION:
                    double yScale = getRational(value);
                    if (yScale!=0.0) fi.pixelHeight = 1.0/yScale;
                    break;
                case RESOLUTION_UNIT:
                    if (value==1&&fi.unit==null)
                        fi.unit = " ";
                    else if (value==2) {
                        if (fi.pixelWidth==1.0/72.0) {
                            fi.pixelWidth = 1.0;
                            fi.pixelHeight = 1.0;
                        } else
                            fi.unit = "inch";
                    } else if (value==3)
                        fi.unit = "cm";
                    break;
                case PLANAR_CONFIGURATION:  // 1=chunky, 2=planar
                    if (value==2 && fi.fileType== FileInfo.RGB48)
                        fi.fileType = FileInfo.RGB48_PLANAR;
                    else if (value==2 && fi.fileType== FileInfo.RGB)
                        fi.fileType = FileInfo.RGB_PLANAR;
                    else if (value!=2 && !(fi.samplesPerPixel==1||fi.samplesPerPixel==3||fi.samplesPerPixel==4)) {
                        String msg = "Unsupported SamplesPerPixel: " + fi.samplesPerPixel;
                        error(msg);
                    }
                    break;
                case COMPRESSION:
                    if (value==5)  {// LZW compression
                        fi.compression = FileInfo.LZW;
                        if (fi.fileType== FileInfo.GRAY12_UNSIGNED)
                            error("ImageJ cannot open 12-bit LZW-compressed TIFFs");
                    } else if (value==32773)  // PackBits compression
                        fi.compression = FileInfo.PACK_BITS;
                    else if (value==32946 || value==8)
                        fi.compression = FileInfo.ZIP;
                    else if (value!=1 && value!=0 && !(value==7&&fi.width<500)) {
                        // don't abort with Spot camera compressed (7) thumbnails
                        // otherwise, this is an unknown compression type
                        fi.compression = FileInfo.COMPRESSION_UNKNOWN;
                        error("ImageJ cannot open TIFF files " +
                                "compressed in this fashion ("+value+")");
                    }
                    break;
                case SOFTWARE: case DATE_TIME: case HOST_COMPUTER: case ARTEST:
                    if (ifdCount==1) {
                        byte[] bytes = getString((int)count, value);
                        String s = bytes!=null?new String(bytes):null;
                        saveMetadata(getName(tag), s);
                    }
                    break;
                case PREDICTOR:
                    if (value==2 && fi.compression== FileInfo.LZW)
                        fi.compression = FileInfo.LZW_WITH_DIFFERENCING;
                    break;
                case COLOR_MAP:
                    if (count==768)
                        getColorMap(value, fi);
                    break;
                case TILE_WIDTH:
                    error("ImageJ cannot open tiled TIFFs.\nTry using the Bio-Formats plugin.");
                    break;
                case SAMPLE_FORMAT:
                    if (fi.fileType== FileInfo.GRAY32_INT && value==FLOATING_POINT)
                        fi.fileType = FileInfo.GRAY32_FLOAT;
                    if (fi.fileType== FileInfo.GRAY16_UNSIGNED) {
                        if (value==SIGNED)
                            fi.fileType = FileInfo.GRAY16_SIGNED;
                        if (value==FLOATING_POINT)
                            error("ImageJ cannot open 16-bit float TIFFs");
                    }
                    break;
                case JPEG_TABLES:
                    if (fi.compression== FileInfo.JPEG)
                        error("Cannot open JPEG-compressed TIFFs with separate tables");
                    break;
                case IMAGE_DESCRIPTION:
                    if (ifdCount==1) {
                        byte[] s = getString((int)count, value);
                        if (s!=null) saveImageDescription(s,fi);
                    }
                    break;
                case ORIENTATION:
                    fi.nImages = 0; // file not created by ImageJ so look at all the IFDs
                    break;
                case METAMORPH1: case METAMORPH2:
                    if ((name.indexOf(".STK")>0||name.indexOf(".stk")>0) && fi.compression== FileInfo.COMPRESSION_NONE) {
                        if (tag==METAMORPH2)
                            fi.nImages=(int)count;
                        else
                            fi.nImages=9999;
                    }
                    break;
                case IPLAB:
                    fi.nImages=(int)value;
                    break;
                case NIH_IMAGE_HDR:
                    if (count==256)
                        decodeNIHImageHeader((int)value, fi);
                    break;
                case META_DATA_BYTE_COUNTS:
                    long saveLoc = in.getLongFilePointer();
                    in.seek(value);
                    metaDataCounts = new int[(int)count];
                    for (int c=0; c<count; c++)
                        metaDataCounts[c] = getInt();
                    in.seek(saveLoc);
                    break;
                case META_DATA:
                    getMetaData((int)value, fi);
                    break;
                default:
                    if (tag>10000 && tag<32768 && ifdCount>1)
                        return null;
            } // tag switch

        }  // loop through entries

        fi.fileFormat = fi.TIFF;
        fi.fileName = name;
        fi.directory = directory;
        if (url!=null)
            fi.url = url;

        // store how long the IFD was in total
        relativeStripInfoLocations[2] = in.getFilePointer() - ifdLoc;
        return fi;
    }

    SerializableFileInfo onlyReadStripsFromIFD(long[] relativeStripInfoLocations , Boolean fastParsingWorked ) throws IOException {

        SerializableFileInfo fi = new SerializableFileInfo();
        long startLoc = in.getLongFilePointer();
        long stripLoc;
        byte[] buffer;
        int tag, fieldType;
        long value, count;

        in.seek(startLoc + relativeStripInfoLocations[0]);

        tag = getShort();

        if ( tag != STRIP_OFFSETS )
        {
            fastParsingWorked = false;
            return( null );
        }

        fieldType = getShort();
        count = isBigTiff ? getLong() : getInt();
        value = getValue(fieldType, count);

        //
        // Initialise
        //
        fi.stripOffsets = new long[(int)count];
        fi.stripLengths = new long[(int)count];

        if(count==1)
        {
            // value is the location where the image is stored
            fi.stripOffsets[0] = value;
        }
        else
        {
            // is the location where the strips are stored
            in.seek(value);

            // and read the values
            if ( fieldType == SHORT )
            {
                buffer = new byte[(int)count * 2];
                in.readFully(buffer);
                convertToShort(fi.stripOffsets, buffer);
            }
            else if ( fieldType == LONG || fieldType == RATIONALE )
            {
                buffer = new byte[(int)count * 4];
                in.readFully(buffer);
                convertToInt(fi.stripOffsets, buffer);
            }
            else if ( fieldType == LONG8 )
            {
                buffer = new byte[(int)count * 8];
                in.readFully(buffer);
                convertToLong8(fi.stripOffsets, buffer);
            }

        }

        fi.offset = fi.stripOffsets[0];
        // TODO: I don't understand below line
        if (count > 1 && ( fi.stripOffsets[(int) count - 1] < fi.stripOffsets[0] ))
        {
            fi.offset = fi.stripOffsets[(int) count - 1];
            logger.warning("Weird line... " + name);
        }

        //
        // stripLengths
        //

        long stripLengthLoc = startLoc + relativeStripInfoLocations[1];
        in.seek(stripLengthLoc);
        tag = getShort();
        if ( tag != STRIP_BYTE_COUNT )
        {
            logger.warning("Fast IFD strip parsing failed. Falling back on full parsing.");
            return(null);
        }
        fieldType = getShort();
        count = isBigTiff ? getLong() : getInt();
        value = getValue(fieldType, count); // & 0xffffffffL;

        if(count==1)
        {
            fi.stripLengths[0] = value;
        }
        else
        {
            // go to where the actual strips are stored
            in.seek(value);

            // and read the values
            if ( fieldType == SHORT )
            {
                buffer = new byte[(int)count * 2];
                in.readFully(buffer);
                convertToShort(fi.stripLengths, buffer);
            }
            else if ( fieldType == LONG || fieldType == RATIONALE)
            {
                buffer = new byte[(int)count * 4];
                in.readFully(buffer);
                convertToInt(fi.stripLengths, buffer);
            }
            else if ( fieldType == LONG8 )
            {
                buffer = new byte[(int)count * 8];
                in.readFully(buffer);
                convertToLong8(fi.stripLengths, buffer);
            }

        }

        //
        // go to end of this IFD
        // - this is important since there is the address to the next IFD
        //

        /*
        if ( value > 4294967294L )
        {
            int a = 1;
        }
        if ( fi.stripLengths[0] > 10000 )
        {
            int a = 1;
        }
        */

        in.seek(startLoc+relativeStripInfoLocations[2]);

        fastParsingWorked = true;

        return fi;
    }

    public SerializableFileInfo[] getTiffInfo() throws IOException {
        if( logger.isShowDebug() ) {
              logger.info("# getTiffInfo");
        }

        startTimeTotal = System.currentTimeMillis();

        long[] relativeStripInfoLocations = new long[3];

        long ifdOffset;
        ArrayList listIFDs = new ArrayList();
        SerializableFileInfo fi = null;
        if (in==null) {
            in = new RandomAccessStream(new RandomAccessFile(new File(directory, name), "r"));
        }
        ifdOffset = OpenImageFileHeader();
        if (ifdOffset<0L) {
            in.close();
            return null;
        }
        if (debugMode) dInfo = "\n  " + name + ": opening\n";

        Boolean fastParsingWorked = true;
        while (ifdOffset>0L)
        {
            in.seek(ifdOffset);
            if( listIFDs.size() < 3 ) // somehow the first ones are sometimes different...
            {
                fi = fullyReadIFD( relativeStripInfoLocations );
				decodeDescriptionString( fi );
            }
            else
            {
                fi = onlyReadStripsFromIFD( relativeStripInfoLocations, fastParsingWorked );
                if ( ! fastParsingWorked )
                {
                    logger.warning(name + ", IFD "+listIFDs.size()+
                            ": Fast IFD strip parsing failed! " +
                            "Maybe something wrong with this file?");
                    fi = fullyReadIFD( relativeStripInfoLocations );
                }
            }
            if( logger.isShowDebug() ) {
                  logger.info("IFD " + listIFDs.size() + " at " + ifdOffset);
                  logger.info("fi.nImages: " + fi.nImages);
                  logger.info("fi.offset: " + fi.offset);
                  logger.info("fi.stripLengths.length: " + fi.stripLengths.length);
                  logger.info("fi.stripOffsets.length: " + fi.stripOffsets.length);
            }
            if (fi != null)
            {
                // add the IFD to the fileInfoSer list
                listIFDs.add(fi);
                // and determine where the next IFD is stored
                ifdOffset = isBigTiff ? getLong() : getUnsignedIntAsLong();
            }
            else
            {
                // reached end of IFDs
                // exit while loop
                ifdOffset = 0L;
            }
            if (debugMode && ifdCount<10) dInfo += "  nextIFD=" + ifdOffset + "\n";
            if (fi!=null && fi.nImages > 1) {
                // set offsets of the following IFDs
                long size = fi.width*fi.height*fi.bytesPerPixel;
                for (int n=1; n < fi.nImages; n++) {
                    SerializableFileInfo fi2 = new SerializableFileInfo();
                    fi2.offset = fi.offset + (n-1)*(size+fi.gapBetweenImages);
                    fi2.nImages = 1;
                    listIFDs.add(fi2);
                }
                ifdOffset = 0L;   // exit while loop
            }


        } // loop through IFDs

        if (listIFDs.size()==0) {
            in.close();
            return null;
        } else {
            SerializableFileInfo[] info = (SerializableFileInfo[])listIFDs.toArray(new SerializableFileInfo[listIFDs.size()]);
            if (url!=null) {
                in.seek(0);
                //info[0].inputStream = in;
            } else
                in.close();
            if (info[0].info==null)
                info[0].info = tiffMetadata;
            fi = info[0];
            //if (fi.fileType==FileInfo.GRAY16_UNSIGNED && fi.description==null)
            //    fi.lutSize = 0; // ignore troublesome non-ImageJ 16-bit LUTs
            return info;
        }
    }

    String getGapInfo(FileInfo[] fi) {
        if (fi.length<2) return "0";
        long minGap = Long.MAX_VALUE;
        long maxGap = -Long.MAX_VALUE;
        for (int i=1; i<fi.length; i++) {
            long gap = fi[i].getOffset()-fi[i-1].getOffset();
            if (gap<minGap) minGap = gap;
            if (gap>maxGap) maxGap = gap;
        }
        long imageSize = fi[0].width*fi[0].height*fi[0].getBytesPerPixel();
        minGap -= imageSize;
        maxGap -= imageSize;
        if (minGap==maxGap)
            return ""+minGap;
        else
            return "varies ("+minGap+" to "+maxGap+")";
    }

	public Properties decodeDescriptionString( SerializableFileInfo fi ) {
		if (fi.description==null || fi.description.length()<7)
			return null;
		if (IJ.debugMode)
			IJ.log("Image Description: " + new String(fi.description).replace('\n',' '));
		if (!fi.description.startsWith("ImageJ"))
			return null;
		Properties props = new Properties();
		InputStream is = new ByteArrayInputStream(fi.description.getBytes());
		try {props.load(is); is.close();}
		catch (IOException e) {return null;}
		String dsUnit = props.getProperty("unit","");
		if ("cm".equals(fi.unit) && "um".equals(dsUnit)) {
			fi.pixelWidth *= 10000;
			fi.pixelHeight *= 10000;
		}
		fi.unit = dsUnit;
		Double n = getNumber(props,"cf");
		if (n!=null) fi.calibrationFunction = n.intValue();
		double c[] = new double[5];
		int count = 0;
		for (int i=0; i<5; i++) {
			n = getNumber(props,"c"+i);
			if (n==null) break;
			c[i] = n.doubleValue();
			count++;
		}
		if (count>=2) {
			fi.coefficients = new double[count];
			for (int i=0; i<count; i++)
				fi.coefficients[i] = c[i];
		}
		fi.valueUnit = props.getProperty("vunit");
		n = getNumber(props,"images");
		if (n!=null && n.doubleValue()>1.0)
			fi.nImages = (int)n.doubleValue();
		n = getNumber(props, "spacing");
		if (n!=null) {
			double spacing = n.doubleValue();
			if (spacing<0) spacing = -spacing;
			fi.pixelDepth = spacing;
		}
		String name = props.getProperty("name");
		if (name!=null)
			fi.fileName = name;
		return props;
	}

	private Double getNumber(Properties props, String key) {
		String s = props.getProperty(key);
		if (s!=null) {
			try {
				return Double.valueOf(s);
			} catch (NumberFormatException e) {}
		}
		return null;
	}

	private double getDouble(Properties props, String key) {
		Double n = getNumber(props, key);
		return n!=null?n.doubleValue():0.0;
	}

	private boolean getBoolean(Properties props, String key) {
		String s = props.getProperty(key);
		return s!=null&&s.equals("true")?true:false;
	}

}
