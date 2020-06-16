package de.embl.cba.bdp2;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.bin.Binner;
import de.embl.cba.bdp2.convert.UnsignedByteTypeConversionDialog;
import de.embl.cba.bdp2.crop.Cropper;
import de.embl.cba.bdp2.open.core.CachedCellImgReader;
import de.embl.cba.bdp2.open.core.FileInfos;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.log.progress.LoggingProgressListener;
import de.embl.cba.bdp2.log.progress.Progress;
import de.embl.cba.bdp2.log.progress.ProgressListener;
import de.embl.cba.bdp2.save.*;
import de.embl.cba.bdp2.dialog.DisplaySettings;
import de.embl.cba.bdp2.align.ChannelShifter;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import loci.common.DebugTools;
import net.imglib2.*;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.converter.Converters;
import net.imglib2.converter.RealUnsignedByteConverter;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.Util;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class BigDataProcessor2
{
    public static ExecutorService generalThreadPool =
            Executors.newFixedThreadPool( Runtime.getRuntime().availableProcessors() * 2  );
    public static ExecutorService trackerThreadPool; // Thread pool for track: TODO: remove?
    public static Map<Integer, Integer> progressTracker = new ConcurrentHashMap<>();
    public static int MAX_THREAD_LIMIT = Runtime.getRuntime().availableProcessors() * 2;
    public static Map<Integer, AtomicBoolean> saveTracker = new ConcurrentHashMap<>();

    // TODO: do we ever need the constructor, maybe only static methods are more convenient?!
    public BigDataProcessor2() {
        //TODO: have separate shutdown for the executorService. It will not shutdown when dialog exeService is shut. --ashis (DONE but needs testing)
        //Ref: https://stackoverflow.com/questions/23684189/java-how-to-make-an-executorservice-running-inside-another-executorservice-not
        // kickOffThreadPack( Runtime.getRuntime().availableProcessors() * 2 );
    }

    public static< R extends RealType< R > & NativeType< R > >
    void saveImageAndWaitUntilDone(
            Image< R > image, SavingSettings savingSettings )
    {
        final LoggingProgressListener progressListener = new LoggingProgressListener( "Frames saved" );
        saveImage( image, savingSettings, progressListener );
        Logger.log( "Saving: " + savingSettings.volumesFilePathStump );
        Progress.waitUntilDone( progressListener, 1000 );
        Logger.log("Saving: Done." );
    }

    public static < R extends RealType< R > & NativeType< R > >
    Image< R > bin( Image<R> image, long[] spanXYZCT )
    {
        return Binner.bin( image, spanXYZCT );
    }

    private static void kickOffThreadPack( int numThreads ) {
        if ( generalThreadPool == null)
            generalThreadPool = Executors.newFixedThreadPool( numThreads );
    }

    public static < R extends RealType< R > & NativeType< R > >
    Image< R > openImage(
            String directory,
            String namingScheme,
            String filterPattern )
    {
        FileInfos fileInfos = new FileInfos( directory, namingScheme, filterPattern );

        final Image< R > image = CachedCellImgReader.loadImage( fileInfos );

        return image;
    }

    public static < R extends RealType< R > & NativeType< R > >
    Image< R > crop( Image< R > image, Interval intervalXYZCT )
    {
        return Cropper.crop5D( image, intervalXYZCT );
    }

    public static < R extends RealType< R > & NativeType< R > >
    Image< R > openImageFromHdf5(
            String directory,
            String loadingScheme,
            String filterPattern,
            String hdf5DataSetName )
    {
        DebugTools.setRootLevel( "OFF" ); // Bio-Formats

        FileInfos fileInfos = new FileInfos( directory, loadingScheme, filterPattern, hdf5DataSetName );

        final Image< R > image = CachedCellImgReader.loadImage( fileInfos );

        return image;
    }

    public static < R extends RealType< R > & NativeType< R > >
    BdvImageViewer showImage( Image< R > image, boolean autoContrast, boolean enableArbitraryPlaneSlicing )
    {
        return new BdvImageViewer( image, autoContrast, enableArbitraryPlaneSlicing );
    }

    public static < R extends RealType< R > & NativeType< R > >
    BdvImageViewer showImage( Image< R > image, boolean autoContrast )
    {
        return new BdvImageViewer( image, autoContrast, BdvImageViewer.enableArbitraryPlaneSlicing );
    }

    public static < R extends RealType< R > & NativeType< R > >
    BdvImageViewer showImage( Image< R > image )
    {
        return new BdvImageViewer( image, true, BdvImageViewer.enableArbitraryPlaneSlicing );
    }

    // TODO: Return futures from the image saver
    public static < R extends RealType< R > & NativeType< R > >
    ImgSaver saveImage(
            Image< R > image,
            SavingSettings savingSettings,
            ProgressListener progressListener )
    {
        // TODO: refactor into a class
        int nIOThread = Math.max( 1, Math.min( savingSettings.numIOThreads, MAX_THREAD_LIMIT ));
        Logger.info( "Saving started; I/O threads: " + nIOThread );

        ExecutorService saveExecutorService = Executors.newFixedThreadPool( nIOThread );

        if ( ! savingSettings.fileType.equals( SavingSettings.FileType.TIFF_PLANES ) )
        {
            // TODO: this makes no sense for cropped images => only fully load the cropped region!
            // TODO: this makes no sense for image where the input data is Tiff planes
            Logger.info( "Saving: Configuring volume reader..." );

            // TODO: The cell dimensions should be such that only the cropped region is loaded in one go, not the whole image!
            final CachedCellImg< R, ? > volumeCachedCellImg
                    = CachedCellImgReader.getVolumeCachedCellImg( image.getFileInfos() );
            final RandomAccessibleInterval< R > volumeLoadedRAI =
                    new CachedCellImgReplacer( image.getRai(), volumeCachedCellImg ).get();
            savingSettings.rai = volumeLoadedRAI;
        }
        else
        {
            savingSettings.rai = image.getRai();
        }

        savingSettings.voxelSpacing = image.getVoxelSpacing();
        savingSettings.voxelUnit = image.getVoxelUnit();
        ImgSaverFactory factory = new ImgSaverFactory();

        if ( savingSettings.saveVolumes )
            Utils.createFilePathParentDirectories( savingSettings.volumesFilePathStump );

        if ( savingSettings.saveProjections )
            Utils.createFilePathParentDirectories( savingSettings.projectionsFilePathStump );

        AbstractImgSaver saver = factory.getSaver( savingSettings, saveExecutorService );
        saver.addProgressListener( progressListener );
        saver.startSave();

        return saver;
    }

    public static < R extends RealType< R > & NativeType< R > >
    Image< R > convert( Image< R > image, double mapTo0, double mapTo255 )
    {
        return UnsignedByteTypeConversionDialog.convert( image, mapTo0, mapTo255 );
    }

    public static <T extends RealType<T>> RandomAccessibleInterval
    unsignedByteTypeConverter(
            RandomAccessibleInterval rai,
            DisplaySettings displaySettings)
    {
        RandomAccessibleInterval<UnsignedByteType> newRai;
        if (!(Util.getTypeFromInterval(rai) instanceof UnsignedByteType)){
            newRai = Converters.convert(
                    rai,
                    new RealUnsignedByteConverter<T>(
                            displaySettings.getDisplayRangeMin(),
                            displaySettings.getDisplayRangeMax()),
                    new UnsignedByteType());
        }else{
            newRai = rai;
        }
        return newRai;
    }

    public static void calibrate( Image image, double[] doubles, String voxelUnit )
    {
        image.setVoxelSpacing( doubles );
        image.setVoxelUnit( voxelUnit );
    }

    public static < R extends RealType< R > & NativeType< R > > Image correctChromaticShift( Image crop, ArrayList< long[] > shifts )
    {
        final ChannelShifter< R > shifter = new ChannelShifter< >( crop.getRai() );
        return crop.newImage( shifter.getShiftedRai( shifts ) );
    }
}
