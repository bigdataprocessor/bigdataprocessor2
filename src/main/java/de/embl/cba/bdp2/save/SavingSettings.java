package de.embl.cba.bdp2.save;

import de.embl.cba.bdp2.dialog.DisplaySettings;
import de.embl.cba.bdp2.image.Image;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import ome.units.UNITS;
import ome.units.unit.Unit;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

/**
 * Created by tischi on 22/05/17.
 */
public class SavingSettings < R extends RealType< R > & NativeType< R > > {

    public static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();
    public static final String TIFF_PLANES = "Tiff Planes";
    public static final String TIFF_VOLUMES = "Tiff Volumes";
    public static final String HDF_5_VOLUMES = "Hdf5 Volumes";
    public static final String IMARIS_VOLUMES = "Imaris Volumes";

    public static final String CHANNEL_NAMES = "Channel names";
    public static final String CHANNEL_INDEXING = "Channel index (C00, C01, ...)";

    // TODO: also remove the binning
    public String bin;

    public static final String COMPRESSION_LZW = "LZW";
    public static final String COMPRESSION_ZLIB = "ZLIB";
    public static final String COMPRESSION_NONE = "None";
    public boolean saveVolumes;
    public String volumesFilePathStump;

    public boolean saveProjections;
    public String projectionsFilePathStump;

    public Image< R > image;
    public List< DisplaySettings > displaySettings;
    public boolean convertTo8Bit;
    public int mapTo0, mapTo255;
    public boolean convertTo16Bit;
    public boolean gate;
    public int gateMin, gateMax;
    public SaveFileType fileType;
    public String compression;
    public int compressionLevel;
    public int rowsPerStrip = -1;
    public int numIOThreads = 1;
    public int numProcessingThreads = 1;
    //public String[] channelNames;
    public String channelNamesInSavedImages = CHANNEL_INDEXING;
    public int tStart; // inclusive, zero-based
    public int tEnd; // inclusive
    public R type;

    @NotNull
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
        savingSettings.fileType = SaveFileType.TiffPlanes;
        savingSettings.volumesFilePathStump = "/Users/tischer/Desktop/bdp2-out/image";
        savingSettings.compression = COMPRESSION_NONE;
        savingSettings.numProcessingThreads = AVAILABLE_PROCESSORS; // (int) Math.ceil( Math.sqrt( AVAILABLE_PROCESSORS ) + 1 );
        savingSettings.numIOThreads = 1; //savingSettings.numProcessingThreads;

        return savingSettings;
    }
}
