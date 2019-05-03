package de.embl.cba.bdp2.saving;

import net.imglib2.RandomAccessibleInterval;

/**
 * Created by tischi on 22/05/17.
 */
public class SavingSettings {

    // TODO: remove the image itself from the settings
    public RandomAccessibleInterval rai;
    public double[] voxelSpacing;
    public String voxelUnit;

    // TODO: also remove the binning
    public String bin;

    public static final String LZW = "LZW";
    public static final String NONE = "None";
    public boolean saveVolume;
    public String filePath;

    public boolean saveProjections;
    public String projectionsFilePath;

    public boolean convertTo8Bit;
    public int mapTo0, mapTo255;
    public boolean convertTo16Bit;
    public boolean gate;
    public int gateMin, gateMax;
    public String fileBaseNameIMARIS;
    public String parentDirectory;
    public FileType fileType;
    public String compression;
    public int compressionLevel;
    public int rowsPerStrip;
    public int nThreads;
    public boolean isotropicProjectionResampling;
    public double isotropicProjectionVoxelSize;

    public enum FileType {
        TIFF_PLANES("Tiff Planes"),
        TIFF_STACKS("Tiff Volumes"),
        HDF5_STACKS("Hdf5 Volumes"),
        IMARIS_STACKS("Partitioned Imaris");

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
        savingSettings.saveVolume = true;
        savingSettings.fileType = FileType.TIFF_PLANES;
        savingSettings.filePath = "src/test/resources/file";
        savingSettings.compression = SavingSettings.NONE;
        savingSettings.isotropicProjectionResampling = false;
        savingSettings.isotropicProjectionVoxelSize = 1.0;

        return savingSettings;
    }
}
