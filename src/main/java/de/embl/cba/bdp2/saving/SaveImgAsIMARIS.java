package de.embl.cba.bdp2.saving;

import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.logging.Logger;
import de.embl.cba.bdp2.utils.DimensionOrder;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.imaris.H5DataCubeWriter;
import de.embl.cba.imaris.ImarisDataSet;
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

import java.util.concurrent.atomic.AtomicBoolean;
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
    private final AtomicBoolean stop;

    public SaveImgAsIMARIS(
            SavingSettings savingSettings,
            ImarisDataSet imarisDS,
            int timePoint,
            AtomicInteger counter,
            long startTime,
            AtomicBoolean stop)
    {
        this.nativeType = (T)Util.getTypeFromInterval(savingSettings.rai );
        Img imgTemp = ImgView.wrap(savingSettings.rai,new CellImgFactory<>(nativeType));
        this.image = new ImgPlus<>(imgTemp, "", FileInfos.AXES_ORDER);

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
        this.current_t = timePoint;
        this.counter = counter;
        this.startTime = startTime;
        this.imarisDataSetProperties = imarisDS;
        this.stop = stop;
    }

    @Override
    public void run() {

        // TODO:
        // - check whether enough RAM is available to execute current thread
        // - if not, merge GC and wait until there is enough
        // - estimate 3x more RAM then actually necessary
        // - if waiting takes to long somehow terminate in a nice way

//        long freeMemoryInBytes = IJ.maxMemory() - IJ.currentMemory();
//        long numBytesOfImage = image.dimension(FileInfoConstants.X) *
//                image.dimension(FileInfoConstants.Y) *
//                image.dimension(FileInfoConstants.Z) *
//                image.dimension(FileInfoConstants.C) *
//                image.dimension(FileInfoConstants.T) *
//                files.bitDepth / 8;
//
//        if (numBytesOfImage > 1.5 * freeMemoryInBytes) {
//            // TODO: do something...
//        }

        final long totalSlices = nFrames * nChannels;
        RandomAccessibleInterval image = savingSettings.rai;
        for (int c = 0; c < this.nChannels; c++)
        {
            if (stop.get()) {
                savingSettings.saveVolume = false;
                Logger.progress("Stopped saving thread: ", "" + this.current_t);
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
            Img<T> imgChannelTime = ImgView.wrap( newRai, new CellImgFactory(nativeType) );

            if (stop.get()) {
                savingSettings.saveVolume = false;
                savingSettings.saveProjection = false;
                Logger.progress("Stopped saving thread: ", "" + current_t);
                return;
            }

            String newPath = savingSettings.filePath;

            String sC = String.format("%1$02d", c);
            String sT = String.format("%1$05d", current_t);
            newPath = newPath + "--C" + sC + "--T" + sT + ".h5";

            ImagePlus imagePlus =
                    Utils.wrapToCalibratedImagePlus(
                            imgChannelTime,
                            savingSettings.voxelSpacing,
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

        if (!stop.get()) {
            SaveImgHelper.documentProgress(totalSlices, counter, startTime);
        }

    }


}
