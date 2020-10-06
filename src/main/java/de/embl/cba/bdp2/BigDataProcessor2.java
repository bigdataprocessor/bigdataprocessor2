package de.embl.cba.bdp2;

import de.embl.cba.bdp2.process.convert.MultiChannelUnsignedByteTypeConverter;
import de.embl.cba.bdp2.process.track.Track;
import de.embl.cba.bdp2.process.track.TrackApplier;
import de.embl.cba.bdp2.process.track.Tracks;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.process.bin.Binner;
import de.embl.cba.bdp2.process.crop.Cropper;
import de.embl.cba.bdp2.open.ChannelSubsetter;
import de.embl.cba.bdp2.open.core.CachedCellImgCreator;
import de.embl.cba.bdp2.open.core.FileInfos;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.log.progress.LoggingProgressListener;
import de.embl.cba.bdp2.log.progress.Progress;
import de.embl.cba.bdp2.log.progress.ProgressListener;
import de.embl.cba.bdp2.save.*;
import de.embl.cba.bdp2.process.align.channelshift.ChannelShifter;
import de.embl.cba.bdp2.service.ImageViewerService;
import de.embl.cba.bdp2.process.transform.ImageTransformer;
import de.embl.cba.bdp2.viewers.ImageViewer;
import loci.common.DebugTools;
import net.imglib2.*;
import net.imglib2.interpolation.InterpolatorFactory;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BigDataProcessor2
{
    public static ExecutorService threadPool = Executors.newFixedThreadPool( Runtime.getRuntime().availableProcessors() * 2  );
    public static int MAX_THREAD_LIMIT = Runtime.getRuntime().availableProcessors() * 2;

    public static< R extends RealType< R > & NativeType< R > >
    void saveImageAndWaitUntilDone( Image< R > image, SavingSettings savingSettings )
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

        final Image< R > image = CachedCellImgCreator.loadImage( fileInfos );

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

        final Image< R > image = CachedCellImgCreator.loadImage( fileInfos );

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

        final Image< R > image = CachedCellImgCreator.loadImage( fileInfos );
        return image;
    }

    public static < R extends RealType< R > & NativeType< R > >
    ImageViewer showImage( Image< R > image, boolean autoContrast, boolean enableArbitraryPlaneSlicing )
    {
        return new ImageViewer( image, autoContrast, enableArbitraryPlaneSlicing );
    }

    public static < R extends RealType< R > & NativeType< R > >
    ImageViewer showImage( Image< R > image, boolean autoContrast )
    {
        return new ImageViewer( image, autoContrast, ImageViewer.enableArbitraryPlaneSlicing );
    }

    public static < R extends RealType< R > & NativeType< R > >
    ImageViewer showImage( Image< R > image )
    {
        return new ImageViewer( image, true, ImageViewer.enableArbitraryPlaneSlicing );
    }

    public static < R extends RealType< R > & NativeType< R > > ImageSaver saveImage( Image< R > image, SavingSettings savingSettings, ProgressListener progressListener )
    {
        final ImageSaver saver = new ImageSaverCreator<>( image, savingSettings, progressListener ).getSaver();
        saver.startSave();
        return saver;
    }

    public static < R extends RealType< R > & NativeType< R > > Image< R > convertToUnsignedByteType( Image< R > image, List< double[] > contrastLimits )
    {
        MultiChannelUnsignedByteTypeConverter< R > converter = new MultiChannelUnsignedByteTypeConverter<>( image, contrastLimits );

        return converter.getConvertedImage();
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
        BigDataProcessor2UserInterface.showUI();
    }

    public static < R extends RealType< R > & NativeType< R > > ImageViewer showImageInheritingDisplaySettings( Image< R > image, Image< R > parentImage )
    {
        final ImageViewer viewer = showImage( image, false );

        final ImageViewer inputImageViewer = ImageViewerService.imageNameToBdvImageViewer.get( parentImage.getName() );
        if ( inputImageViewer != null )
                viewer.setDisplaySettings( inputImageViewer.getDisplaySettings() );

        return viewer;
    }
}
