package de.embl.cba.bigDataTools2.saving;

import net.imglib2.RandomAccessibleInterval;

/**
 * Created by tischi on 22/05/17.
 */
public class SavingSettings {

    public static final String LZW = "LZW";
    public static final String NONE = "None";


    public RandomAccessibleInterval image;
    // public Img image; //TODO: change to RAI --ashis
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
        HDF5("HDF5"),
        HDF5_IMARIS_BDV("IMARIS HDF5"),
        TIFF_as_STACKS("Tiff Stacks");
        //SINGLE_PLANE_TIFF("Single Plane Tiff"); //SERIALIZED_HEADERS("Serialized headers");
        private final String text;
        private FileType(String s)
        {
            text = s;
        }
        @Override
        public String toString() {
            return text;
        }
    }
}
