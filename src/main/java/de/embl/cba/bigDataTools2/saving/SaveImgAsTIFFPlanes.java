package de.embl.cba.bigDataTools2.saving;

import de.embl.cba.bigDataTools2.fileInfoSource.FileInfoConstants;
import de.embl.cba.bigDataTools2.utils.Utils;
import ij.IJ;
import ij.ImagePlus;
import ij.io.FileSaver;
import ij.plugin.Binner;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.view.Views;

public class SaveImgAsTIFFPlanes implements Runnable {

    private final int c;
    private final int t;
    private final int z;
    private final SavingSettings savingSettings;

    public SaveImgAsTIFFPlanes(int c,
                               int t,
                               int z,
                               SavingSettings savingSettings) {
        this.c = c;
        this.z = z;
        this.t = t;
        this.savingSettings = savingSettings;
    }

    @Override
    public void run() {

        if (SaveCentral.interruptSavingThreads) {
            savingSettings.saveVolume = true;
            return;
        }
        RandomAccessibleInterval imgStack = savingSettings.image;
        long[] minInterval = new long[]{
                imgStack.min(FileInfoConstants.X ),
                imgStack.min(FileInfoConstants.Y ),
                z,
                c,
                t};
        long[] maxInterval = new long[]{
                imgStack.max(FileInfoConstants.X ),
                imgStack.max(FileInfoConstants.Y ),
                z,
                c,
                t};

        RandomAccessibleInterval newRai = Views.interval(imgStack, minInterval, maxInterval);

        @SuppressWarnings("unchecked")
        ImagePlus impCTZ = ImageJFunctions.wrap( newRai, "slice", null); // TODO : check if threads can be given for this operation in place of null --ashis
        impCTZ.setTitle("slice");
        impCTZ.setDimensions(1, 1, 1);

        // Convert
        //
        if (savingSettings.convertTo8Bit) {
            IJ.setMinAndMax(impCTZ, savingSettings.mapTo0, savingSettings.mapTo255);
            IJ.run(impCTZ, "8-bit", "");
        }

        if (savingSettings.convertTo16Bit) {
            IJ.run(impCTZ, "16-bit", "");
        }
        // Bin and save
        //
        String[] binnings = savingSettings.bin.split(";");

        for (String binning : binnings) {

            if (SaveCentral.interruptSavingThreads) {
                return;
            }

            String newPath = savingSettings.filePath;

            // Binning
            ImagePlus impBinned = (ImagePlus) impCTZ.clone();

            int[] binningA = Utils.delimitedStringToIntegerArray(binning, ",");

            if (binningA[0] > 1 || binningA[1] > 1 || binningA[2] > 1) {
                Binner binner = new Binner();
                impBinned = binner.shrink(impCTZ, binningA[0], binningA[1], binningA[2], Binner.AVERAGE);
                newPath = savingSettings.filePath + "--bin-" + binningA[0] + "-" + binningA[1] + "-" + binningA[2];
            }

            FileSaver fileSaver = new FileSaver(impBinned);

            String sC = String.format("%1$02d", c);
            String sT = String.format("%1$05d", t);
            String sZ = String.format("%1$05d", z);
            String pathCTZ;

            if (imgStack.dimension(FileInfoConstants.C ) > 1 || imgStack.dimension(FileInfoConstants.T ) > 1) {
                pathCTZ = newPath + "--C" + sC + "--T" + sT + "--Z" + sZ + ".tif";
            } else {
                pathCTZ = newPath + "--Z" + sZ + ".tif";
            }
            fileSaver.saveAsTiff(pathCTZ);
        }
    }
}
