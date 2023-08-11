/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2023 EMBL
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
package de.embl.cba.bdp2.save;

import de.embl.cba.bdp2.image.Image;

import java.io.File;

/*
 * Created by tischi on 22/05/17.
 */
public class SavingSettings  {

    public static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();
    public static final String CHANNEL_NAMES = "Channel names";
    public static final String CHANNEL_INDEXING = "Channel index (C00, C01, ...)";
    public static final String COMPRESSION_LZW = "LZW";
    public static final String COMPRESSION_ZLIB = "ZLIB";
    public static final String COMPRESSION_NONE = "None";

    public boolean saveVolumes;
    public String volumesFilePathStump;
    public boolean saveProjections = false;
    public String projectionsFilePathStump;
    public SaveFileType fileType;
    public String compression;
    public int compressionLevel;
    public int rowsPerStrip = -1;
    public int numIOThreads = 1;
    public int numProcessingThreads = 1;
    public String channelNames = CHANNEL_INDEXING;
    public int tStart; // inclusive, zero-based
    public int tEnd; // inclusive

    @Deprecated
    public boolean convertTo8Bit;
    @Deprecated
    public int mapTo0, mapTo255;
    @Deprecated
    public boolean convertTo16Bit;
    @Deprecated
    public boolean gate;
    @Deprecated
    public int gateMin, gateMax;
    @Deprecated
    public String bin;

    public String projectionMode = Projector.MAX;

	public static String createFilePathStump( Image image, String type, String directory )
	{
		return new File( directory, type + File.separator + image.getName() ).toString();
	}

    // Loads minimum settings. Useful for testing purposes.
    public static SavingSettings getDefaults()
    {
        SavingSettings savingSettings = new SavingSettings();
        savingSettings.saveVolumes = true;
        savingSettings.saveProjections = false;
        savingSettings.fileType = SaveFileType.TIFFVolumes;
        savingSettings.volumesFilePathStump = "/Users/tischer/Desktop/bdp2-out/image";
        savingSettings.compression = COMPRESSION_NONE;
        savingSettings.numProcessingThreads = AVAILABLE_PROCESSORS; // (int) Math.ceil( Math.sqrt( AVAILABLE_PROCESSORS ) + 1 );
        savingSettings.numIOThreads = 1; //savingSettings.numProcessingThreads;

        return savingSettings;
    }

    public static String getChannelName( int c, SavingSettings settings, Image image )
    {
        if ( settings.channelNames.equals( CHANNEL_INDEXING ) )
            return String.format( "C%1$02d", c );
        else if ( settings.channelNames.equals( CHANNEL_NAMES ) )
            return image.getChannelNames()[ c ];
        else
            return String.format( "C%1$02d", c );
    }

}
