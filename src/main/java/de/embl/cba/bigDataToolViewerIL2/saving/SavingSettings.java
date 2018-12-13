package de.embl.cba.bigDataToolViewerIL2.saving;

import de.embl.cba.bigDataToolViewerIL2.fileInfoSource.FileInfoConstants;
import net.imglib2.RandomAccessibleInterval;

/**
 * Created by tischi on 22/05/17.
 */
public class SavingSettings {

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
    public FileInfoConstants.FileType fileType;
    public String compression;
    public int compressionLevel;
    public int rowsPerStrip;
    public int nThreads;
}
