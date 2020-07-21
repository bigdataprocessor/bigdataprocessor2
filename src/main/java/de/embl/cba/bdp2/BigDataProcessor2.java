package de.embl.cba.bdp2;

import de.embl.cba.bdp2.drift.track.Track;
import de.embl.cba.bdp2.drift.track.TrackApplier;
import de.embl.cba.bdp2.drift.track.Tracks;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.bin.Binner;
import de.embl.cba.bdp2.convert.UnsignedByteTypeConversionDialog;
import de.embl.cba.bdp2.crop.Cropper;
import de.embl.cba.bdp2.open.ChannelSubsetter;
import de.embl.cba.bdp2.open.core.CachedCellImgReader;
import de.embl.cba.bdp2.open.core.FileInfos;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.log.progress.LoggingProgressListener;
import de.embl.cba.bdp2.log.progress.Progress;
import de.embl.cba.bdp2.log.progress.ProgressListener;
import de.embl.cba.bdp2.save.*;
import de.embl.cba.bdp2.dialog.DisplaySettings;
import de.embl.cba.bdp2.align.ChannelShifter;
import de.embl.cba.bdp2.service.BdvService;
import de.embl.cba.bdp2.transform.ImageTransformer;
import de.embl.cba.bdp2.ui.BigDataProcessor2UI;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import loci.common.DebugTools;
import net.imglib2.*;
import net.imglib2.converter.Converters;
import net.imglib2.converter.RealUnsignedByteConverter;
import net.imglib2.interpolation.InterpolatorFactory;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BigDataProcessor2
{
    public static ExecutorService threadPool = Executors.newFixedThreadPool( Runtime.getRuntime().availableProcessors() * 2  );
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

        FileInfos fileInfos = new FileInfos( directory, loadingScheme, filterPattern, hdf5DataSetName, null );

        final Image< R > image = CachedCellImgReader.loadImage( fileInfos );

        return image;
    }

    public static < R extends RealType< R > & NativeType< R > >
    Image< R > openImageFromHdf5(
            String directory,
            String loadingScheme,
            String filterPattern,
            String hdf5DataSetName,
            ChannelSubsetter channelSubsetter )
    {
        DebugTools.setRootLevel( "OFF" ); // Bio-Formats

        FileInfos fileInfos = new FileInfos( directory, loadingScheme, filterPattern, hdf5DataSetName, channelSubsetter );

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
        image.setVoxelSize( doubles );
        image.setVoxelUnit( voxelUnit );
    }

    public static < R extends RealType< R > & NativeType< R > > Image< R > correctChromaticShift( Image< R > image, ArrayList< long[] > shifts )
    {
        final ChannelShifter< R > shifter = new ChannelShifter< >( image.getRai() );
        return image.newImage( shifter.getShiftedRai( shifts ) );
    }

    public static < R extends RealType< R > & NativeType< R > > Image< R > transform( Image< R > image, AffineTransform3D transform3D, InterpolatorFactory interpolatorFactory )
    {
        final ImageTransformer< R > transformer = new ImageTransformer<>( image, transform3D, interpolatorFactory );
        return transformer.transform();
    }

    public static < R extends RealType< R > & NativeType< R > > Image< R > applyTrack( File file, Image< R > image, Boolean centerImage )
	{
		final Track track = Tracks.fromJsonFile( file );
		final TrackApplier< R > trackApplier = new TrackApplier<>( image );
		Image outputImage = trackApplier.applyTrack( track );
		if ( ! centerImage )
			outputImage.setRai( Views.zeroMin( outputImage.getRai() ) );

		return outputImage;
	}

	public static void showUI()
    {
        BigDataProcessor2UI.showUI();
    }

    public static < R extends RealType< R > & NativeType< R > > BdvImageViewer showImageInheritingDisplaySettings( Image< R > image, Image< R > parentImage )
    {
        final BdvImageViewer viewer = showImage( image, false );

        final BdvImageViewer inputImageViewer = BdvService.imageNameToBdvImageViewer.get( parentImage.getName() );
        if ( inputImageViewer != null )
                viewer.setDisplaySettings( inputImageViewer.getDisplaySettings() );

        return viewer;
    }
}
