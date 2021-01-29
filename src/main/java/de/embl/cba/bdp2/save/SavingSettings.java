package de.embl.cba.bdp2.save;

import de.embl.cba.bdp2.dialog.DisplaySettings;
import de.embl.cba.bdp2.image.Image;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import java.io.File;
import java.util.List;

/**
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
    public boolean saveProjections;
    public String projectionsFilePathStump;
    public SaveFileType fileType;
    public String compression;
    public int compressionLevel;
    public int rowsPerStrip = -1;
    public int numIOThreads = 1;
    public int numProcessingThreads = 1;
    public String channelNamesInSavedImages = CHANNEL_INDEXING;
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

    public static String createFilePathStump( Image image, String type, String directory )
	{
		return new File( directory, type + File.separator + image.getName() ).toString();
	}

    /**
     * Loads minimum settings.
     * Useful for testing purposes.
     * @return SavingSettings
     */
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
}
