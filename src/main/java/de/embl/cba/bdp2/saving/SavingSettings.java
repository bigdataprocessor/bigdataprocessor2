package de.embl.cba.bdp2.saving;

import net.imglib2.RandomAccessibleInterval;

/**
 * Created by tischi on 22/05/17.
 */
public class SavingSettings {

    public static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();

    // TODO: remove the image itself from the settings
    public RandomAccessibleInterval rai;
    public double[] voxelSpacing;
    public String voxelUnit;

    // TODO: also remove the binning
    public String bin;

    public static final String COMPRESSION_LZW = "LZW";
    public static final String COMPRESSION_ZLIB = "ZLIB";
    public static final String COMPRESSION_NONE = "None";
    public boolean saveVolumes;
    public String volumesFilePath;

    public boolean saveProjections;
    public String projectionsFilePath;

    public boolean convertTo8Bit;
    public int mapTo0, mapTo255;
    public boolean convertTo16Bit;
    public boolean gate;
    public int gateMin, gateMax;
    public String parentDirectory;
    public FileType fileType;
    public String compression;
    public int compressionLevel;
    public int rowsPerStrip;
    public int numIOThreads = 1;
    public int numProcessingThreads = 1;

	public enum FileType {
        TIFF_PLANES("Tiff Planes"),
        TIFF_STACKS("Tiff Volumes"),
        HDF5_STACKS("Hdf5 Volumes"),
        IMARIS_STACKS("Imaris Volumes");

        private final String text;
        FileType(String s) {
            text = s;
        }
        @Override
        public String toString() {
            return text;
        }
    }

    /**
     * Loads minimum settings for saving TIFF as Planes.
     * Useful for testing purposes.
     * @return SavingSettings
     */
    public static SavingSettings getDefaults() {
        SavingSettings savingSettings = new SavingSettings();
        savingSettings.bin = "1,1,1";
        savingSettings.voxelSpacing = new double[]{1,1,1};
        savingSettings.voxelUnit = "pixel";
        savingSettings.saveVolumes = true;
        savingSettings.fileType = FileType.TIFF_PLANES;
        savingSettings.volumesFilePath = "/Users/tischer/Desktop/bdp2-out/image";
        savingSettings.compression = SavingSettings.COMPRESSION_NONE;
        savingSettings.numProcessingThreads = (int) Math.ceil( Math.sqrt( AVAILABLE_PROCESSORS ) + 1 );
        savingSettings.numIOThreads = savingSettings.numProcessingThreads;


        return savingSettings;
    }
}
