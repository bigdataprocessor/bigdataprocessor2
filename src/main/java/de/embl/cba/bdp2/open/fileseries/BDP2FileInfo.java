/*
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2025 EMBL
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

package de.embl.cba.bdp2.open.fileseries;

import ij.io.FileInfo;

import java.io.Serializable;

/*
 * TODO: Clean this up as it still contains a lot of things from BDP1
 */
public class BDP2FileInfo implements Cloneable, Serializable {

    /* File format (TIFF, GIF_OR_JPG, BMP, etc.). Used by the File/Revert run */
    public int fileFormat;

    /* File type (GRAY8, GRAY_16_UNSIGNED, RGB, etc.) */
    public int fileType;

    public String fileName;
    public String directory;
    public String url;
    public int width;
    public int height;
    public boolean intelByteOrder;
    public int compression;
    public long[] stripOffsets = null;
    public long offset;
    public long[] stripLengths = null;
    public int rowsPerStrip = 0;

    public double pixelWidth = Double.NaN;
    public double pixelHeight = Double.NaN;
    public double pixelDepth = Double.NaN;
    public double frameInterval;

    public int nImages;
    public boolean whiteIsZero;
    public int samplesPerPixel;
    public String unit;
    public String description;

    public int gapBetweenImages;
    public String info;

    // add to copyVolumeRAI:
    public int calibrationFunction;
    public String valueUnit;
    public double[] coefficients;
    public int[] metaDataTypes; // must be < 0xffffff
    public byte[][] metaData;
    public String[] sliceLabels;
    public double[] displayRanges;
    public byte[][] channelLuts;
    public byte[] roi;
    public byte[][] overlay;
    public int lutSize;
    public byte[] reds;
    public byte[] greens;
    public byte[] blues;

    // own stuff
    public int bytesPerPixel;
    public String h5DataSet;
    public String fileTypeString;
    public boolean isCropped = false;
    public int[] pCropOffset = new int[3];
    public int[] pCropSize = new int[3];

    // File formats
    public static final int TIFF = 2;


    // TODO: there is a lot of duplicated information that would only be needed once
    public BDP2FileInfo() {

    }

    public BDP2FileInfo( FileInfo info ) {
        this.fileFormat = info.fileFormat;
        this.fileName = info.fileName;
        this.directory = info.directory;
        this.fileType = info.fileType;
        this.url = info.url;
        this.width = info.width;
        this.height = info.height;
        this.intelByteOrder = info.intelByteOrder;
        this.compression = info.compression;
        //this.stripOffsets = info.stripOffsets;
        //this.stripLengths = info.stripLengths;
        this.rowsPerStrip = info.rowsPerStrip;
        this.pixelWidth = info.pixelWidth;
        this.pixelHeight = info.pixelHeight;
        this.pixelDepth = info.pixelDepth;
        this.frameInterval = info.frameInterval;
        this.bytesPerPixel = info.getBytesPerPixel();
    }

    public BDP2FileInfo( BDP2FileInfo info ) {
        this.fileFormat = info.fileFormat;
        this.fileName = info.fileName;
        this.directory = info.directory;
        this.fileType = info.fileType;
        this.url = info.url;
        this.width = info.width;
        this.height = info.height;
        this.offset = info.offset;
        this.intelByteOrder = info.intelByteOrder;
        this.compression = info.compression;
        this.stripOffsets = info.stripOffsets;
        this.stripLengths = info.stripLengths;
        this.rowsPerStrip = info.rowsPerStrip;
        this.pixelWidth = info.pixelWidth;
        this.pixelHeight = info.pixelHeight;
        this.pixelDepth = info.pixelDepth;
        this.frameInterval = info.frameInterval;
        this.nImages = info.nImages;
        this.bytesPerPixel = info.bytesPerPixel;
        this.h5DataSet = info.h5DataSet;
        this.fileTypeString = info.fileTypeString;
        this.isCropped = info.isCropped;
        this.pCropOffset = info.pCropOffset.clone();
        this.pCropSize = info.pCropSize.clone();
        //for ( int i = 0; i < 3; i++)
        //{
        //    this.pCropOffset[i] = info.pCropOffset[i];
        //    this.pCropSize[i] = info.pCropSize[i];
        //}
        this.whiteIsZero = info.whiteIsZero;
        this.samplesPerPixel = info.samplesPerPixel;
        this.unit = info.unit;
        this.gapBetweenImages = info.gapBetweenImages;
        this.info = info.info;
        this.description = info.description;
    }


    public synchronized Object clone() {
        try {return super.clone();}
        catch (CloneNotSupportedException e) {return null;}
    }

}
