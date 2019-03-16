package de.embl.cba.bdp2.saving;

import de.embl.cba.bdp2.fileinfosource.FileInfoConstants;
import de.embl.cba.bdp2.utils.DimensionOrder;
import de.embl.cba.imaris.H5DataCubeWriter;
import de.embl.cba.imaris.ImarisDataSet;
import de.embl.cba.bdp2.logging.IJLazySwingLogger;
import de.embl.cba.bdp2.logging.Logger;
import de.embl.cba.bdp2.utils.Utils;
import ij.ImagePlus;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.ImgView;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

import java.util.concurrent.atomic.AtomicInteger;

public class SaveImgAsIMARIS<T extends RealType<T> & NativeType<T>> implements Runnable {
    private int current_t;
    private final int nFrames;
    private final int nChannels;
    private final ImgPlus<T> image;
    private AtomicInteger counter;
    private SavingSettings savingSettings;
    private final long startTime;
    private ImarisDataSet imarisDataSetProperties;
    private final T nativeType;
    private Logger logger = new IJLazySwingLogger();

    public SaveImgAsIMARIS(
            SavingSettings savingSettings,
            ImarisDataSet imarisDS,
            int time,
            AtomicInteger counter,
            long startTime)
    {
        this.nativeType = (T)Util.getTypeFromInterval(savingSettings.image);
        Img imgTemp = ImgView.wrap(savingSettings.image,new CellImgFactory<>(nativeType));
        this.image = new ImgPlus<>(imgTemp, "", FileInfoConstants.AXES_ORDER);

        if (this.image.dimensionIndex(Axes.TIME) >= 0) {
            this.nFrames = Math.toIntExact(image.dimension(this.image.dimensionIndex(Axes.TIME)));
        } else {
            this.nFrames = 1;
        }
        if (this.image.dimensionIndex(Axes.CHANNEL) >= 0) {
            this.nChannels = Math.toIntExact(image.dimension(this.image.dimensionIndex(Axes.CHANNEL)));
        } else {
            this.nChannels = 1;
        }
        this.savingSettings = savingSettings;
        this.current_t = time;
        this.counter = counter;
        this.startTime = startTime;
        this.imarisDataSetProperties = imarisDS;
    }

    @Override
    public void run() {

        // TODO:
        // - check whether enough RAM is available to execute current thread
        // - if not, run GC and wait until there is enough
        // - estimate 3x more RAM then actually necessary
        // - if waiting takes to long somehow terminate in a nice way

//        long freeMemoryInBytes = IJ.maxMemory() - IJ.currentMemory();
//        long numBytesOfImage = image.dimension(FileInfoConstants.X) *
//                image.dimension(FileInfoConstants.Y) *
//                image.dimension(FileInfoConstants.Z) *
//                image.dimension(FileInfoConstants.C) *
//                image.dimension(FileInfoConstants.T) *
//                fileinfosource.bitDepth / 8;
//
//        if (numBytesOfImage > 1.5 * freeMemoryInBytes) {
//            // TODO: do something...
//        }

        final long totalSlices = nFrames * nChannels;
        RandomAccessibleInterval image = savingSettings.image;
        for (int c = 0; c < this.nChannels; c++) {
            if (SaveCentral.interruptSavingThreads) {
                savingSettings.saveVolume = false;
                logger.progress("Stopped saving thread: ", "" + this.current_t);
                return;
            }

            // Load
            //   ImagePlus impChannelTime = getDataCube( c );  May be faster???

            long[] minInterval = new long[]{
                    image.min( DimensionOrder.X ),
                    image.min( DimensionOrder.Y ),
                    image.min( DimensionOrder.Z ),
                    c,
                    this.current_t};
            long[] maxInterval = new long[]{
                    image.max( DimensionOrder.X ),
                    image.max( DimensionOrder.Y ),
                    image.max( DimensionOrder.Z ),
                    c,
                    this.current_t};

            RandomAccessibleInterval newRai = Views.interval(image, minInterval, maxInterval);
            newRai = SaveImgHelper.convertor( newRai, this.savingSettings );
            Img<T> imgChannelTime = null;
            imgChannelTime = ImgView.wrap( newRai, new CellImgFactory(nativeType) );

            // TODO: remove all those binnings!
            // Bin, project and save
            //
            String[] binnings = savingSettings.bin.split(";");
            for (String binning : binnings) {
                if (SaveCentral.interruptSavingThreads) {
                    savingSettings.saveVolume = false;
                    savingSettings.saveProjection = false;
                    logger.progress("Stopped saving thread: ", "" + current_t);
                    return;
                }
                String newPath = savingSettings.filePath;
                Img<T> imgBinned = imgChannelTime;
                ImgPlus<T> imgPlusBinned = new ImgPlus<>(imgBinned, "", FileInfoConstants.AXES_ORDER);
                int[] binningA = Utils.delimitedStringToIntegerArray(binning, ",");

                // TODO: remove this binning!
                if (binningA[0] > 1 || binningA[1] > 1 || binningA[2] > 1)
                {
                    newPath = SaveImgHelper.doBinning(imgPlusBinned, binningA, newPath, null);
                }

                String sC = String.format("%1$02d", c);
                String sT = String.format("%1$05d", current_t);
                newPath = newPath + "--C" + sC + "--T" + sT + ".h5";
                ImagePlus imagePlus =
                        Utils.wrapToCalibratedImagePlus(
                        imgPlusBinned,
                        savingSettings.voxelSize,
                        savingSettings.unit,
                        "BinnedWrapped");


                // Save volume
                if (savingSettings.saveVolume) {
                    H5DataCubeWriter writer = new H5DataCubeWriter();
                    writer.writeImarisCompatibleResolutionPyramid(
                            imagePlus, imarisDataSetProperties, c, this.current_t);
                }

                // Save projections
                // TODO: save into one single file
                if (savingSettings.saveProjection) {
                    SaveImgAsTIFFStacks.saveAsTiffXYZMaxProjection(
                            imagePlus, c, this.current_t, newPath);
                }
            }
            if (!SaveCentral.interruptSavingThreads) {
                SaveImgHelper.documentProgress(totalSlices, counter, logger, startTime);
            }
        }
    }


}
