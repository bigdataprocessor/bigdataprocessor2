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

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BigDataProcessor2
{
    public static ExecutorService generalThreadPool = Executors.newFixedThreadPool( Runtime.getRuntime().availableProcessors() * 2  );
    public static int MAX_THREAD_LIMIT = Runtime.getRuntime().availableProcessors() * 2;

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

    public static < R extends RealType< R > & NativeType< R > >
    Image< R > openImage(
            File directory,
            String namingScheme,
            String filterPattern )
    {
        return openImage( directory.getAbsolutePath(), namingScheme, filterPattern);
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

    public static < R extends RealType< R > & NativeType< R > > ImageSaver saveImage( Image< R > image, SavingSettings savingSettings, ProgressListener progressListener )
    {
        final ImageSaver saver = new ImageSaverCreator<>( image, savingSettings, progressListener ).getSaver();
        saver.startSave();
        return saver;
    }

    public static < R extends RealType< R > & NativeType< R > > Image< R > convert( Image< R > image, double mapTo0, double mapTo255 )
    {
        return UnsignedByteTypeConversionDialog.convert( image, mapTo0, mapTo255 );
    }

    public static <T extends RealType<T>> RandomAccessibleInterval unsignedByteTypeConverter( RandomAccessibleInterval rai, DisplaySettings displaySettings)
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
