package de.embl.cba.bdp2.saving;

import net.imglib2.RandomAccessibleInterval;

/**
 * Created by tischi on 22/05/17.
 */
public class SavingSettings {

    public static final String LZW = "LZW";
    public static final String NONE = "None";

    public RandomAccessibleInterval image;
    public double[] voxelSize;
    public String unit;
    public String bin;
    public boolean saveVolume;
    public boolean saveProjection;
    public boolean convertTo8Bit;
    public int mapTo0, mapTo255;
    public boolean convertTo16Bit;
    public boolean gate;
    public int gateMin, gateMax;
    public String filePath;
    public String fileBaseNameIMARIS;
    public String parentDirectory;
    public FileType fileType;
    public String compression;
    public int compressionLevel;
    public int rowsPerStrip;
    public int nThreads;

    public enum FileType {
        TIFF_as_PLANES("Tiff Planes"),
        HDF5_STACKS("Hdf5 Stacks"),
        HDF5_IMARIS_BDV("Imaris"),
        TIFF_as_STACKS("Tiff Stacks");
        //SINGLE_PLANE_TIFF("Single Plane Tiff"); //SERIALIZED_HEADERS("Serialized headers");
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
        savingSettings.voxelSize = new double[]{1,1,1};
        savingSettings.unit = "pixel";
        savingSettings.saveVolume = true;
        savingSettings.fileType = FileType.TIFF_as_PLANES;
        savingSettings.filePath = "src/test/resources/file";
        savingSettings.compression = SavingSettings.NONE;
        return savingSettings;
    }
}
